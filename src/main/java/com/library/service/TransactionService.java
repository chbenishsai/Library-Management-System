package com.library.service;

import com.library.exception.BookNotAvailableException;
import com.library.exception.BookNotFoundException;
import com.library.exception.DatabaseOperationException;
import com.library.exception.MemberNotFoundException;
import com.library.exception.TransactionNotFoundException;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Transaction;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;
import com.library.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic layer for the Issue / Return workflow, including fine
 * calculation using the {@code java.time} API.
 */
public class TransactionService {

    /** Loan period in days from the issue date. */
    private static final int LOAN_PERIOD_DAYS = 14;

    /** Fine charged per day overdue, in INR. */
    private static final BigDecimal FINE_PER_DAY = new BigDecimal("10.00");

    private final TransactionRepository transactionRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public TransactionService() {
        this.transactionRepository = new TransactionRepository();
        this.bookRepository = new BookRepository();
        this.memberRepository = new MemberRepository();
    }

    public TransactionService(TransactionRepository transactionRepository,
                               BookRepository bookRepository,
                               MemberRepository memberRepository) {
        this.transactionRepository = transactionRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * Issues a book to a member: validates existence of both, checks
     * availability, decrements the available copy count, and records a
     * new transaction with a due date 14 days from today.
     */
    public Transaction issueBook(Long bookId, Long memberId)
            throws BookNotFoundException, MemberNotFoundException,
            BookNotAvailableException, DatabaseOperationException {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("No book found with ID: " + bookId));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("No member found with ID: " + memberId));

        if (!book.isAvailable()) {
            throw new BookNotAvailableException(
                    "'" + book.getTitle() + "' has no available copies right now.");
        }

        boolean decremented = bookRepository.decrementAvailableCopies(bookId);
        if (!decremented) {
            // Guards against a race condition where availability changed
            // between the check above and this update.
            throw new BookNotAvailableException(
                    "'" + book.getTitle() + "' was just checked out by someone else. Please try again.");
        }

        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(LOAN_PERIOD_DAYS);

        Transaction transaction = new Transaction(bookId, memberId, issueDate, dueDate);
        return transactionRepository.save(transaction);
    }

    /**
     * Returns a book on behalf of a member: locates the active transaction,
     * calculates any overdue fine using java.time, increments the
     * available copy count, and marks the transaction RETURNED.
     */
    public Transaction returnBook(Long bookId, Long memberId)
            throws TransactionNotFoundException, DatabaseOperationException {

        Transaction transaction = transactionRepository.findActiveIssueByBookAndMember(bookId, memberId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        "No active issue record found for book ID " + bookId + " and member ID " + memberId));

        LocalDate returnDate = LocalDate.now();
        BigDecimal fine = calculateFine(transaction.getDueDate(), returnDate);

        transactionRepository.markReturned(transaction.getTransactionId(), returnDate, fine);
        bookRepository.incrementAvailableCopies(bookId);

        transaction.setReturnDate(returnDate);
        transaction.setFineAmount(fine);
        transaction.setStatus(Transaction.Status.RETURNED);
        return transaction;
    }

    /**
     * Calculates the fine owed for a return, given the due date and the
     * actual return date. Uses ChronoUnit.DAYS.between (java.time) rather
     * than manual date math. No fine is owed if returned on or before the
     * due date.
     */
    public BigDecimal calculateFine(LocalDate dueDate, LocalDate returnDate) {
        long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
        if (overdueDays <= 0) {
            return BigDecimal.ZERO;
        }
        return FINE_PER_DAY.multiply(BigDecimal.valueOf(overdueDays));
    }

    /**
     * Projects what the fine would be *today* for a currently-issued book,
     * useful for displaying a running total before the member actually
     * returns it.
     */
    public BigDecimal projectCurrentFine(Transaction transaction) {
        if (transaction.getStatus() == Transaction.Status.RETURNED) {
            return transaction.getFineAmount();
        }
        return calculateFine(transaction.getDueDate(), LocalDate.now());
    }

    public List<Transaction> getAllTransactions() throws DatabaseOperationException {
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactionsForMember(Long memberId) throws DatabaseOperationException {
        return transactionRepository.findByMemberId(memberId);
    }

    /**
     * Returns all currently overdue transactions, sorted by how many days
     * overdue they are (most overdue first) using a Stream + Comparator.
     */
    public List<Transaction> getOverdueTransactions() throws DatabaseOperationException {
        return transactionRepository.findOverdue().stream()
                .sorted((t1, t2) -> t1.getDueDate().compareTo(t2.getDueDate()))
                .collect(Collectors.toList());
    }
}

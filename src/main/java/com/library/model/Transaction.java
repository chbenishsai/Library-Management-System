package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Plain Old Java Object representing an Issue/Return Transaction entity.
 */
public class Transaction {

    public enum Status {
        ISSUED,
        RETURNED
    }

    private Long transactionId;
    private Long bookId;
    private Long memberId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BigDecimal fineAmount;
    private Status status;

    // Optional denormalized fields populated by joined queries for display purposes.
    private String bookTitle;
    private String memberName;

    public Transaction() {
    }

    /**
     * Constructor used when issuing a brand-new transaction (before it has an ID).
     */
    public Transaction(Long bookId, Long memberId, LocalDate issueDate, LocalDate dueDate) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.fineAmount = BigDecimal.ZERO;
        this.status = Status.ISSUED;
    }

    /**
     * Full constructor used when hydrating a Transaction from the database.
     */
    public Transaction(Long transactionId, Long bookId, Long memberId, LocalDate issueDate,
                        LocalDate dueDate, LocalDate returnDate, BigDecimal fineAmount, Status status) {
        this.transactionId = transactionId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fineAmount = fineAmount;
        this.status = status;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return String.format(
                "[TXN: %-4s] Book: %-4s | Member: %-4s | Issued: %s | Due: %s | Returned: %-10s | Fine: Rs.%s | Status: %s",
                transactionId, bookId, memberId, issueDate, dueDate,
                returnDate == null ? "-" : returnDate, fineAmount, status);
    }
}

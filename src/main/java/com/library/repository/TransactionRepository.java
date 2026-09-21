package com.library.repository;

import com.library.exception.DatabaseOperationException;
import com.library.model.Transaction;
import com.library.util.DatabaseConnectionManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for the {@code transactions} table, which records
 * every book issue and return event.
 */
public class TransactionRepository {

    private final DatabaseConnectionManager connectionManager;

    public TransactionRepository() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public Transaction save(Transaction transaction) throws DatabaseOperationException {
        String sql = "INSERT INTO transactions (book_id, member_id, issue_date, due_date, "
                + "return_date, fine_amount, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, transaction.getBookId());
            ps.setLong(2, transaction.getMemberId());
            ps.setDate(3, Date.valueOf(transaction.getIssueDate()));
            ps.setDate(4, Date.valueOf(transaction.getDueDate()));
            ps.setDate(5, transaction.getReturnDate() != null ? Date.valueOf(transaction.getReturnDate()) : null);
            ps.setBigDecimal(6, transaction.getFineAmount());
            ps.setString(7, transaction.getStatus().name());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transaction.setTransactionId(generatedKeys.getLong(1));
                }
            }
            return transaction;

        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Failed to save transaction for book id " + transaction.getBookId(), e);
        }
    }

    /**
     * Marks a transaction as RETURNED, stamping the return date and fine.
     */
    public boolean markReturned(Long transactionId, java.time.LocalDate returnDate, BigDecimal fineAmount)
            throws DatabaseOperationException {
        String sql = "UPDATE transactions SET return_date = ?, fine_amount = ?, status = 'RETURNED' "
                + "WHERE transaction_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(returnDate));
            ps.setBigDecimal(2, fineAmount);
            ps.setLong(3, transactionId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to mark transaction returned: " + transactionId, e);
        }
    }

    /**
     * Finds the active (ISSUED) transaction for a given book and member pair.
     * Used to locate the correct record when a member returns a book.
     */
    public Optional<Transaction> findActiveIssueByBookAndMember(Long bookId, Long memberId)
            throws DatabaseOperationException {
        String sql = "SELECT * FROM transactions WHERE book_id = ? AND member_id = ? "
                + "AND status = 'ISSUED' ORDER BY issue_date DESC LIMIT 1";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, bookId);
            ps.setLong(2, memberId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Failed to find active transaction for book " + bookId + " / member " + memberId, e);
        }
    }

    public Optional<Transaction> findById(Long transactionId) throws DatabaseOperationException {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, transactionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find transaction by id: " + transactionId, e);
        }
    }

    public List<Transaction> findAll() throws DatabaseOperationException {
        String sql = "SELECT t.*, b.title AS book_title, m.name AS member_name "
                + "FROM transactions t "
                + "JOIN books b ON t.book_id = b.book_id "
                + "JOIN members m ON t.member_id = m.member_id "
                + "ORDER BY t.transaction_id";

        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Transaction t = mapRow(rs);
                t.setBookTitle(rs.getString("book_title"));
                t.setMemberName(rs.getString("member_name"));
                transactions.add(t);
            }
            return transactions;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch all transactions", e);
        }
    }

    public List<Transaction> findByMemberId(Long memberId) throws DatabaseOperationException {
        String sql = "SELECT t.*, b.title AS book_title, m.name AS member_name "
                + "FROM transactions t "
                + "JOIN books b ON t.book_id = b.book_id "
                + "JOIN members m ON t.member_id = m.member_id "
                + "WHERE t.member_id = ? ORDER BY t.transaction_id";

        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, memberId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction t = mapRow(rs);
                    t.setBookTitle(rs.getString("book_title"));
                    t.setMemberName(rs.getString("member_name"));
                    transactions.add(t);
                }
            }
            return transactions;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch transactions for member id: " + memberId, e);
        }
    }

    public List<Transaction> findOverdue() throws DatabaseOperationException {
        String sql = "SELECT t.*, b.title AS book_title, m.name AS member_name "
                + "FROM transactions t "
                + "JOIN books b ON t.book_id = b.book_id "
                + "JOIN members m ON t.member_id = m.member_id "
                + "WHERE t.status = 'ISSUED' AND t.due_date < CURDATE() "
                + "ORDER BY t.due_date";

        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Transaction t = mapRow(rs);
                t.setBookTitle(rs.getString("book_title"));
                t.setMemberName(rs.getString("member_name"));
                transactions.add(t);
            }
            return transactions;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch overdue transactions", e);
        }
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Date returnDateSql = rs.getDate("return_date");

        return new Transaction(
                rs.getLong("transaction_id"),
                rs.getLong("book_id"),
                rs.getLong("member_id"),
                rs.getDate("issue_date").toLocalDate(),
                rs.getDate("due_date").toLocalDate(),
                returnDateSql != null ? returnDateSql.toLocalDate() : null,
                rs.getBigDecimal("fine_amount"),
                Transaction.Status.valueOf(rs.getString("status"))
        );
    }
}

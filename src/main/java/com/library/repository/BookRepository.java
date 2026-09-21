package com.library.repository;

import com.library.exception.DatabaseOperationException;
import com.library.model.Book;
import com.library.util.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for the {@code books} table. This is the only
 * layer permitted to contain raw SQL. All queries use
 * {@link PreparedStatement} to prevent SQL injection, and all
 * JDBC resources are managed via try-with-resources.
 */
public class BookRepository {

    private final DatabaseConnectionManager connectionManager;

    public BookRepository() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    /**
     * Inserts a new book and returns it with its generated ID populated.
     */
    public Book save(Book book) throws DatabaseOperationException {
        String sql = "INSERT INTO books (isbn, title, author, genre, total_copies, available_copies) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getGenre());
            ps.setInt(5, book.getTotalCopies());
            ps.setInt(6, book.getAvailableCopies());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    book.setBookId(generatedKeys.getLong(1));
                }
            }
            return book;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save book: " + book.getTitle(), e);
        }
    }

    public Optional<Book> findById(Long bookId) throws DatabaseOperationException {
        String sql = "SELECT * FROM books WHERE book_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, bookId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find book by id: " + bookId, e);
        }
    }

    public Optional<Book> findByIsbn(String isbn) throws DatabaseOperationException {
        String sql = "SELECT * FROM books WHERE isbn = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, isbn);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find book by isbn: " + isbn, e);
        }
    }

    public List<Book> findAll() throws DatabaseOperationException {
        String sql = "SELECT * FROM books ORDER BY book_id";
        List<Book> books = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                books.add(mapRow(rs));
            }
            return books;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch all books", e);
        }
    }

    /**
     * Searches books whose title OR author contains the given keyword
     * (case-insensitive, partial match).
     */
    public List<Book> searchByTitleOrAuthor(String keyword) throws DatabaseOperationException {
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? ORDER BY title";
        List<Book> books = new ArrayList<>();
        String likePattern = "%" + keyword.toLowerCase() + "%";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, likePattern);
            ps.setString(2, likePattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
            }
            return books;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to search books by keyword: " + keyword, e);
        }
    }

    /**
     * Updates the mutable fields of a book (title, author, genre, copies).
     */
    public boolean update(Book book) throws DatabaseOperationException {
        String sql = "UPDATE books SET title = ?, author = ?, genre = ?, total_copies = ?, "
                + "available_copies = ? WHERE book_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getGenre());
            ps.setInt(4, book.getTotalCopies());
            ps.setInt(5, book.getAvailableCopies());
            ps.setLong(6, book.getBookId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update book id: " + book.getBookId(), e);
        }
    }

    /**
     * Atomically decrements available_copies by 1, but only if a copy is
     * actually available. Returns true if the decrement succeeded.
     * The WHERE clause guards against race conditions between concurrent
     * issue requests without requiring an explicit application-level lock.
     */
    public boolean decrementAvailableCopies(Long bookId) throws DatabaseOperationException {
        String sql = "UPDATE books SET available_copies = available_copies - 1 "
                + "WHERE book_id = ? AND available_copies > 0";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, bookId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to decrement available copies for book id: " + bookId, e);
        }
    }

    /**
     * Atomically increments available_copies by 1, capped at total_copies.
     */
    public boolean incrementAvailableCopies(Long bookId) throws DatabaseOperationException {
        String sql = "UPDATE books SET available_copies = available_copies + 1 "
                + "WHERE book_id = ? AND available_copies < total_copies";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, bookId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to increment available copies for book id: " + bookId, e);
        }
    }

    public boolean deleteById(Long bookId) throws DatabaseOperationException {
        String sql = "DELETE FROM books WHERE book_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, bookId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete book id: " + bookId, e);
        }
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        LocalDateTime createdAt = rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime()
                : null;

        return new Book(
                rs.getLong("book_id"),
                rs.getString("isbn"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("genre"),
                rs.getInt("total_copies"),
                rs.getInt("available_copies"),
                createdAt
        );
    }
}

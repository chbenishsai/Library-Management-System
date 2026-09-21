package com.library.service;

import com.library.exception.BookNotFoundException;
import com.library.exception.DatabaseOperationException;
import com.library.exception.DuplicateEntryException;
import com.library.exception.InvalidInputException;
import com.library.model.Book;
import com.library.repository.BookRepository;
import com.library.util.ValidationUtil;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic layer for Book management: validation, duplicate
 * checking, and orchestration of repository calls. No SQL lives here.
 */
public class BookService {

    private final BookRepository bookRepository;

    public BookService() {
        this.bookRepository = new BookRepository();
    }

    // Constructor injection overload - useful for testing with mocks.
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book addBook(String isbn, String title, String author, String genre, int totalCopies)
            throws InvalidInputException, DuplicateEntryException, DatabaseOperationException {

        if (ValidationUtil.isBlank(isbn)) {
            throw new InvalidInputException("ISBN cannot be blank.");
        }
        if (ValidationUtil.isBlank(title)) {
            throw new InvalidInputException("Title cannot be blank.");
        }
        if (ValidationUtil.isBlank(author)) {
            throw new InvalidInputException("Author cannot be blank.");
        }
        if (totalCopies <= 0) {
            throw new InvalidInputException("Total copies must be a positive number.");
        }

        if (bookRepository.findByIsbn(isbn.trim()).isPresent()) {
            throw new DuplicateEntryException("A book with ISBN " + isbn + " already exists.");
        }

        Book book = new Book(isbn.trim(), title.trim(), author.trim(),
                genre == null ? null : genre.trim(), totalCopies);
        return bookRepository.save(book);
    }

    public List<Book> getAllBooks() throws DatabaseOperationException {
        return bookRepository.findAll().stream()
                .sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public List<Book> searchBooks(String keyword) throws InvalidInputException, DatabaseOperationException {
        if (ValidationUtil.isBlank(keyword)) {
            throw new InvalidInputException("Search keyword cannot be blank.");
        }
        return bookRepository.searchByTitleOrAuthor(keyword.trim());
    }

    public Book getBookById(Long bookId) throws BookNotFoundException, DatabaseOperationException {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("No book found with ID: " + bookId));
    }

    public void deleteBook(Long bookId) throws BookNotFoundException, DatabaseOperationException {
        Book book = getBookById(bookId);
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            // Some copies are currently issued out; still allow deletion at
            // the repository level would violate referential integrity via
            // the foreign key, so we surface a clear, actionable error here.
            throw new IllegalStateException(
                    "Cannot delete '" + book.getTitle() + "' - some copies are currently issued to members. "
                            + "All copies must be returned before deletion.");
        }
        boolean deleted = bookRepository.deleteById(bookId);
        if (!deleted) {
            throw new BookNotFoundException("No book found with ID: " + bookId);
        }
    }

    /**
     * Returns total count of distinct titles and aggregate copy statistics,
     * demonstrating use of the Streams API for simple analytics.
     */
    public long countDistinctAuthors() throws DatabaseOperationException {
        return bookRepository.findAll().stream()
                .map(Book::getAuthor)
                .distinct()
                .count();
    }

    public long countTotalAvailableCopies() throws DatabaseOperationException {
        return bookRepository.findAll().stream()
                .mapToInt(Book::getAvailableCopies)
                .sum();
    }
}

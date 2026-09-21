package com.library.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Plain Old Java Object representing a Book entity.
 */
public class Book {

    private Long bookId;
    private String isbn;
    private String title;
    private String author;
    private String genre;
    private int totalCopies;
    private int availableCopies;
    private LocalDateTime createdAt;

    public Book() {
    }

    /**
     * Constructor used when creating a brand-new book (before it has an ID).
     */
    public Book(String isbn, String title, String author, String genre, int totalCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    /**
     * Full constructor used when hydrating a Book from the database.
     */
    public Book(Long bookId, String isbn, String title, String author, String genre,
                int totalCopies, int availableCopies, LocalDateTime createdAt) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.createdAt = createdAt;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return Objects.equals(bookId, book.bookId) && Objects.equals(isbn, book.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId, isbn);
    }

    @Override
    public String toString() {
        return String.format(
                "[ID: %-4s] %-30s by %-20s | Genre: %-15s | Copies: %d/%d available",
                bookId, title, author, genre == null ? "N/A" : genre, availableCopies, totalCopies);
    }
}

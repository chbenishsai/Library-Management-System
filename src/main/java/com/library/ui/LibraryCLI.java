package com.library.ui;

import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Transaction;
import com.library.service.BookService;
import com.library.service.MemberService;
import com.library.service.TransactionService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * Command-line interface for the Library Management System.
 * Responsible only for I/O (reading input, printing output) and
 * delegating all business logic to the service layer.
 */
public class LibraryCLI {

    private final Scanner scanner;
    private final BookService bookService;
    private final MemberService memberService;
    private final TransactionService transactionService;

    public LibraryCLI() {
        this.scanner = new Scanner(System.in);
        this.bookService = new BookService();
        this.memberService = new MemberService();
        this.transactionService = new TransactionService();
    }

    public void start() {
        System.out.println("=================================================");
        System.out.println("   Welcome to the Library Management System");
        System.out.println("=================================================");

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> handleBookMenu();
                case "2" -> handleMemberMenu();
                case "3" -> handleIssueBook();
                case "4" -> handleReturnBook();
                case "5" -> handleViewOverdue();
                case "0" -> {
                    running = false;
                    System.out.println("Goodbye!");
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
        scanner.close();
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("---------------- MAIN MENU ----------------");
        System.out.println("1. Book Management");
        System.out.println("2. Member Management");
        System.out.println("3. Issue a Book");
        System.out.println("4. Return a Book");
        System.out.println("5. View Overdue Books");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    // ------------------------------------------------------------------
    // Book Management
    // ------------------------------------------------------------------

    private void handleBookMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("------------- BOOK MANAGEMENT -------------");
            System.out.println("1. Add New Book");
            System.out.println("2. View All Books");
            System.out.println("3. Search Books (title/author)");
            System.out.println("4. Delete a Book");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addBook();
                case "2" -> viewAllBooks();
                case "3" -> searchBooks();
                case "4" -> deleteBook();
                case "0" -> back = true;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void addBook() {
        try {
            System.out.print("Enter ISBN: ");
            String isbn = scanner.nextLine().trim();
            System.out.print("Enter Title: ");
            String title = scanner.nextLine().trim();
            System.out.print("Enter Author: ");
            String author = scanner.nextLine().trim();
            System.out.print("Enter Genre (optional): ");
            String genre = scanner.nextLine().trim();
            System.out.print("Enter Total Copies: ");
            int totalCopies = readInt();

            Book book = bookService.addBook(isbn, title, author, genre, totalCopies);
            System.out.println("Book added successfully -> " + book);

        } catch (NumberFormatException e) {
            System.out.println("Error: Total copies must be a valid whole number.");
        } catch (LibraryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewAllBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            if (books.isEmpty()) {
                System.out.println("No books found in the library.");
                return;
            }
            System.out.println("\n--- All Books (" + books.size() + ") ---");
            books.forEach(System.out::println);

        } catch (LibraryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void searchBooks() {
        try {
            System.out.print("Enter search keyword (title or author): ");
            String keyword = scanner.nextLine().trim();
            List<Book> results = bookService.searchBooks(keyword);

            if (results.isEmpty()) {
                System.out.println("No books matched '" + keyword + "'.");
                return;
            }
            System.out.println("\n--- Search Results (" + results.size() + ") ---");
            results.forEach(System.out::println);

        } catch (LibraryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void deleteBook() {
        try {
            System.out.print("Enter Book ID to delete: ");
            Long bookId = readLong();
            bookService.deleteBook(bookId);
            System.out.println("Book deleted successfully.");

        } catch (NumberFormatException e) {
            System.out.println("Error: Book ID must be a valid number.");
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (LibraryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Member Management
    // ------------------------------------------------------------------

    private void handleMemberMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("------------ MEMBER MANAGEMENT ------------");
            System.out.println("1. Register New Member");
            System.out.println("2. View All Members");
            System.out.println("3. Delete a Member");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> registerMember();
                case "2" -> viewAllMembers();
                case "3" -> deleteMember();
                case "0" -> back = true;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void registerMember() {
        try {
            System.out.print("Enter Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Enter Phone (10 digits): ");
            String phone = scanner.nextLine().trim();
            System.out.print("Enter Address (optional): ");
            String address = scanner.nextLine().trim();

            Member member = memberService.registerMember(name, email, phone, address);
            System.out.println("Member registered successfully -> " + member);

        } catch (LibraryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewAllMembers() {
        try {
            List<Member> members = memberService.getAllMembers();
            if (members.isEmpty()) {
                System.out.println("No members registered yet.");
                return;
            }
            System.out.println("\n--- All Members (" + members.size() + ") ---");
            members.forEach(System.out::println);

        } catch (LibraryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void deleteMember() {
        try {
            System.out.print("Enter Member ID to delete: ");
            Long memberId = readLong();
            memberService.deleteMember(memberId);
            System.out.println("Member deleted successfully.");

        } catch (NumberFormatException e) {
            System.out.println("Error: Member ID must be a valid number.");
        } catch (LibraryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Issue / Return Workflow
    // ------------------------------------------------------------------

    private void handleIssueBook() {
        try {
            System.out.print("Enter Book ID to issue: ");
            Long bookId = readLong();
            System.out.print("Enter Member ID: ");
            Long memberId = readLong();

            Transaction transaction = transactionService.issueBook(bookId, memberId);
            System.out.println("Book issued successfully!");
            System.out.println("Issue Date: " + transaction.getIssueDate());
            System.out.println("Due Date:   " + transaction.getDueDate());
            System.out.println("Transaction ID: " + transaction.getTransactionId());

        } catch (NumberFormatException e) {
            System.out.println("Error: IDs must be valid numbers.");
        } catch (LibraryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleReturnBook() {
        try {
            System.out.print("Enter Book ID being returned: ");
            Long bookId = readLong();
            System.out.print("Enter Member ID: ");
            Long memberId = readLong();

            Transaction transaction = transactionService.returnBook(bookId, memberId);
            BigDecimal fine = transaction.getFineAmount();

            System.out.println("Book returned successfully!");
            System.out.println("Return Date: " + transaction.getReturnDate());
            if (fine.compareTo(BigDecimal.ZERO) > 0) {
                System.out.println("This book was returned late. Fine due: Rs. " + fine);
            } else {
                System.out.println("Returned on time. No fine due.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Error: IDs must be valid numbers.");
        } catch (LibraryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleViewOverdue() {
        try {
            List<Transaction> overdue = transactionService.getOverdueTransactions();
            if (overdue.isEmpty()) {
                System.out.println("No overdue books. Everything is on schedule!");
                return;
            }
            System.out.println("\n--- Overdue Books (" + overdue.size() + ") ---");
            for (Transaction t : overdue) {
                BigDecimal projectedFine = transactionService.projectCurrentFine(t);
                System.out.printf("Book: %-30s | Member: %-20s | Due: %s | Fine so far: Rs. %s%n",
                        t.getBookTitle(), t.getMemberName(), t.getDueDate(), projectedFine);
            }

        } catch (LibraryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Input helpers
    // ------------------------------------------------------------------

    private int readInt() {
        return Integer.parseInt(scanner.nextLine().trim());
    }

    private Long readLong() {
        return Long.parseLong(scanner.nextLine().trim());
    }
}

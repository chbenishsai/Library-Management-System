-- ============================================================
-- Library Management System - MySQL Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS library_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE library_db;

-- ------------------------------------------------------------
-- Table: books
-- ------------------------------------------------------------
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS members;

CREATE TABLE books (
    book_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn            VARCHAR(20)  NOT NULL UNIQUE,
    title           VARCHAR(255) NOT NULL,
    author          VARCHAR(255) NOT NULL,
    genre           VARCHAR(100),
    total_copies    INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_total_copies_nonneg CHECK (total_copies >= 0),
    CONSTRAINT chk_available_copies_nonneg CHECK (available_copies >= 0),
    CONSTRAINT chk_available_leq_total CHECK (available_copies <= total_copies)
) ENGINE=InnoDB;

CREATE INDEX idx_books_title  ON books (title);
CREATE INDEX idx_books_author ON books (author);

-- ------------------------------------------------------------
-- Table: members
-- ------------------------------------------------------------
CREATE TABLE members (
    member_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    phone           VARCHAR(10)  NOT NULL,
    address         VARCHAR(255),
    registered_on   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_phone_10_digits CHECK (phone REGEXP '^[0-9]{10}$')
) ENGINE=InnoDB;

CREATE INDEX idx_members_email ON members (email);

-- ------------------------------------------------------------
-- Table: transactions
-- ------------------------------------------------------------
CREATE TABLE transactions (
    transaction_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id         BIGINT NOT NULL,
    member_id       BIGINT NOT NULL,
    issue_date      DATE NOT NULL,
    due_date        DATE NOT NULL,
    return_date     DATE NULL,
    fine_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status          ENUM('ISSUED', 'RETURNED') NOT NULL DEFAULT 'ISSUED',

    CONSTRAINT fk_transactions_book
        FOREIGN KEY (book_id) REFERENCES books(book_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT fk_transactions_member
        FOREIGN KEY (member_id) REFERENCES members(member_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_transactions_book   ON transactions (book_id);
CREATE INDEX idx_transactions_member ON transactions (member_id);
CREATE INDEX idx_transactions_status ON transactions (status);

-- ------------------------------------------------------------
-- Sample seed data (optional - comment out if not needed)
-- ------------------------------------------------------------
INSERT INTO books (isbn, title, author, genre, total_copies, available_copies) VALUES
('9780132350884', 'Clean Code', 'Robert C. Martin', 'Software Engineering', 3, 3),
('9780201633610', 'Design Patterns', 'Erich Gamma', 'Software Engineering', 2, 2),
('9780134685991', 'Effective Java', 'Joshua Bloch', 'Java', 4, 4);

INSERT INTO members (name, email, phone, address) VALUES
('Benish Kumar', 'benish@example.com', '9876543210', 'Vellore, TN');

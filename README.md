# Library-Management-System
# Library Management System (JDBC + MySQL)

A console-based Library Management System built in Java, using JDBC
against a MySQL database, structured around a strict layered
Repository-Service architecture. Built to practice production-style
backend design: clean separation of concerns, custom exception
handling, and safe database access patterns — before moving on to
Spring Boot and REST APIs.

## Features

- Add, view, search, and delete books (title/author search, copy tracking)
- Register members with email and phone validation
- Issue books with automatic availability checks
- Return books with automatic overdue fine calculation
- Full transaction history (issue date, due date, return date, fine)
- View all currently overdue books

## Tech Stack

- Java 21
- JDBC (raw, no ORM)
- MySQL
- Maven
- Core Java: Collections, Streams, Lambdas, java.time API, OOP, Exception Handling

## Architecture

Strict multi-layered Repository-Service design:

package com.library.ui;

/**
 * Application entry point. Boots the CLI-based Library Management System.
 */
public class Main {

    public static void main(String[] args) {
        LibraryCLI cli = new LibraryCLI();
        cli.start();
    }
}

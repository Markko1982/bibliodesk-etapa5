package com.bibliodesk.app;

import com.bibliodesk.dao.BookDAO;
import com.bibliodesk.dao.LoanDAO;
import com.bibliodesk.dao.UserDAO;
import com.bibliodesk.model.Book;
import com.bibliodesk.model.Loan;
import com.bibliodesk.model.Profile;
import com.bibliodesk.model.User;
import com.bibliodesk.util.PasswordHasher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class Database {

    private final UserDAO userDAO = new UserDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final LoanDAO loanDAO = new LoanDAO();

    public Database() {
        // sem seed em memória: tudo vem do MySQL
    }

    // ---------- AUTH ----------
    public Optional<User> authenticate(String login, String rawPassword) {
        String hash = PasswordHasher.sha256(rawPassword);
        return userDAO.authenticate(login, hash);
    }

    // ---------- USERS ----------
    public List<User> getUsers() {
        return userDAO.findAll();
    }

    public User addUser(String name, String login, String rawPassword, Profile profile, boolean active) {
        String hash = PasswordHasher.sha256(rawPassword);
        return userDAO.insert(name, login, hash, profile, active);
    }

    public void updateUser(int id, String name, String login, String rawPasswordOrNull, Profile profile, boolean active) {
        if (rawPasswordOrNull != null && !rawPasswordOrNull.isBlank()) {
            String hash = PasswordHasher.sha256(rawPasswordOrNull);
            userDAO.updateWithPassword(id, name, login, hash, profile, active);
        } else {
            userDAO.updateWithoutPassword(id, name, login, profile, active);
        }
    }

    public void setUserActive(int id, boolean active) {
        userDAO.setActive(id, active);
    }

    public void deleteUser(int id) {
        userDAO.deleteById(id);
    }

    // ---------- BOOKS ----------
    public List<Book> getBooks() {
        return bookDAO.findAll();
    }

    public Book addBook(String title, String author, String isbn, int year, int totalQty) {
        return addBook(title, author, isbn, year, totalQty, totalQty);
    }

    public Book addBook(String title, String author, String isbn, int year, int totalQty, int availableQty) {
        return bookDAO.insert(title, author, isbn, year, totalQty, availableQty);
    }

    public void updateBook(int id, String title, String author, String isbn, int year, int totalQty, int availableQty) {
        bookDAO.update(id, title, author, isbn, year, totalQty, availableQty);
    }

    public void deleteBook(int id) {
        bookDAO.deleteById(id);
    }

    public Optional<Book> findBookById(int id) {
        return bookDAO.findById(id);
    }

    // ---------- LOANS ----------
    public List<Loan> getLoans() {
        return loanDAO.findAll();
    }

    public Loan addLoan(User user, Book book, LocalDate date) {
        return loanDAO.createLoan(user, book, date);
    }

    public void returnLoan(Loan loan, LocalDate returnDate) {
        loanDAO.registerReturn(loan.getId(), loan.getBook().getId(), returnDate);
        loan.setReturnDate(returnDate); // só pra UI não ficar estranha antes do refresh
    }
}

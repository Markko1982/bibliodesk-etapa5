package com.bibliodesk.model;

import java.time.LocalDate;

public class Loan {
    private int id;
    private User user;
    private Book book;
    private LocalDate loanDate;
    private LocalDate returnDate;

    public Loan(int id, User user, Book book, LocalDate loanDate) {
        this.id = id;
        this.user = user;
        this.book = book;
        this.loanDate = loanDate;
    }

    public int getId() { return id; }
    public User getUser() { return user; }
    public Book getBook() { return book; }
    public LocalDate getLoanDate() { return loanDate; }
    public LocalDate getReturnDate() { return returnDate; }

    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public boolean isReturned() {
        return returnDate != null;
    }
}

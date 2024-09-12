package com.fmi.bookzz.entity;

import java.util.List;

public class MyBook {
    private Long id;
    private String status;
    private String dateOfRead;
    private Long bookId;
    private String title;
    private String bookImage;
    private List<String> authors;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateOfRead() {
        return dateOfRead;
    }

    public void setDateOfRead(String dateOfRead) {
        this.dateOfRead = dateOfRead;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBookImage() {
        return bookImage;
    }

    public void setBookImage(String bookImage) {
        this.bookImage = bookImage;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }
}

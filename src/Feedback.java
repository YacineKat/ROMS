package com.restaurant.roms;

public class Feedback {
    private int id;
    private String customerName;
    private String phone;
    private String comment;
    private int rating;
    private String date;

    public Feedback() {}

    public Feedback(int id, String customerName, String phone, String comment, int rating, String date) {
        this.id = id;
        this.customerName = customerName;
        this.phone = phone;
        this.comment = comment;
        this.rating = rating;
        this.date = date;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
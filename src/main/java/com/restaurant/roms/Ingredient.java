package com.restaurant.roms;

public class Ingredient {
    private int id;
    private String name;
    private double quantity;
    private double minQuantity;
    private String unit;
    private String category;

    public Ingredient(int id, String name, double quantity, double minQuantity, String unit, String category) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.minQuantity = minQuantity;
        this.unit = unit;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(double minQuantity) {
        this.minQuantity = minQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isLowStock() {
        return quantity <= minQuantity;
    }

    public void decreaseQuantity(double amount) {
        double newQuantity = quantity - amount;
        if (newQuantity < 0) {
            newQuantity = 0;
        }
        quantity = newQuantity;
    }

    public void increaseQuantity(double amount) {
        quantity += amount;
    }
} 
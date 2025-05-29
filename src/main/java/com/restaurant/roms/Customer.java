package com.restaurant.roms;

public class Customer {
    private int customerId;
    private String address;
    private Integer tableNumber;
    private boolean toDeliver;

    public Customer() {
    }

    public Customer(String address, Integer tableNumber, boolean toDeliver) {
        this.address = address;
        this.tableNumber = tableNumber;
        this.toDeliver = toDeliver;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(Integer tableNumber) {
        this.tableNumber = tableNumber;
    }

    public boolean isToDeliver() {
        return toDeliver;
    }

    public void setToDeliver(boolean toDeliver) {
        this.toDeliver = toDeliver;
    }
} 
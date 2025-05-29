package com.restaurant.roms;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class Order {
    private int orderId;
    private OrderStatus status;
    private Date date;
    private int customerId;
    private int staffId;
    private int kitchenId;
    private String managerId;
    private String notes;
    private List<OrderItem> items;
    private double total;
    private String deliveryPartner;

    public Order() {
        this.items = new ArrayList<>();
        this.status = OrderStatus.QUEUED;
        this.date = new Date(System.currentTimeMillis());
    }

    public Order(List<CartItem> cartItems, double total, String deliveryPartner, String notes) {
        this();
        this.total = total;
        this.deliveryPartner = deliveryPartner;
        this.notes = notes;
        this.items = cartItems.stream()
            .map(item -> new OrderItem(item.getMenuItem(), item.getQuantity()))
            .collect(Collectors.toList());
    }

    public enum OrderStatus {
        QUEUED("Queued"),
        IN_PROGRESS("In Progress"),
        READY("Ready"),
        DELIVERED("Delivered"),
        CANCELLED("Cancelled");

        private final String displayName;

        OrderStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static class OrderItem {
        private MenuItem menuItem;
        private int quantity;

        public OrderItem(MenuItem menuItem, int quantity) {
            this.menuItem = menuItem;
            this.quantity = quantity;
        }

        public MenuItem getMenuItem() {
            return menuItem;
        }

        public void setMenuItem(MenuItem menuItem) {
            this.menuItem = menuItem;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public int getKitchenId() {
        return kitchenId;
    }

    public void setKitchenId(int kitchenId) {
        this.kitchenId = kitchenId;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public double getTotal() {
        return items.stream()
                .mapToDouble(item -> item.getMenuItem().getPrice() * item.getQuantity())
                .sum();
    }

    public String getItemsSummary() {
        StringBuilder summary = new StringBuilder();
        for (OrderItem item : items) {
            if (summary.length() > 0) {
                summary.append(", ");
            }
            summary.append(item.getQuantity())
                  .append("x ")
                  .append(item.getMenuItem().getTitle());
        }
        return summary.toString();
    }
}

class OrderItem {
    private int orderId;
    private MenuItem menuItem;
    private int quantity;

    public OrderItem(int orderId, MenuItem menuItem, int quantity) {
        this.orderId = orderId;
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    public int getOrderId() {
        return orderId;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
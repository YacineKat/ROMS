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
    private String date;
    private int customerId;
    private String staffId;
    private int kitchenId;
    private String notes;
    private List<OrderItem> items;
    private double total;
    private String managerId;

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

    public Order() {
        this.items = new ArrayList<>();
    }

    public Order(List<CartItem> items, double total, String deliveryPartner, String notes) {
        this.items = new ArrayList<>();
        for (CartItem cartItem : items) {
            this.items.add(new OrderItem(cartItem.getMenuItem(), cartItem.getQuantity()));
        }
        this.total = total;
        this.managerId = deliveryPartner;
        this.notes = notes;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public int getKitchenId() {
        return kitchenId;
    }

    public void setKitchenId(int kitchenId) {
        this.kitchenId = kitchenId;
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
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getItemsSummary() {
        StringBuilder summary = new StringBuilder();
        for (OrderItem item : items) {
            if (summary.length() > 0) {
                summary.append(", ");
            }
            summary.append(item.getMenuItem().getName())
                  .append(" (")
                  .append(item.getQuantity())
                  .append(")");
        }
        return summary.toString();
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
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
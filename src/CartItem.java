package com.restaurant.roms;

public class CartItem {
    private MenuItem menuItem;
    private int quantity;
    private double total;

    public CartItem(MenuItem menuItem, int quantity) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.total = menuItem.getPrice() * quantity;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
        updateTotal();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        updateTotal();
    }

    private void updateTotal() {
        if (menuItem != null) {
            this.total = menuItem.getPrice() * quantity;
        } else {
            this.total = 0;
        }
    }

    public double getTotal() {
        return total;
    }
}
package com.restaurant.roms;

public class User {
    private int id;
    private String username;
    private String password;
    private String name;
    private String email;
    private boolean isAdmin;
    private boolean isManager;
    
    public User() {}
    
    public User(int id, String username, String password, String name, String email, boolean isAdmin, boolean isManager) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.isAdmin = isAdmin;
        this.isManager = isManager;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public boolean isAdmin() {
        return isAdmin;
    }
    
    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
    
    public boolean isManager() {
        return isManager;
    }
    
    public void setManager(boolean isManager) {
        this.isManager = isManager;
    }
} 
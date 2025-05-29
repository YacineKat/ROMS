package com.restaurant.roms;

import java.sql.Timestamp;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class User {
    private static final Logger logger = LoggerFactory.getLogger(User.class);
    private static final int SALT_LENGTH = 16;
    
    private int id;
    private String username;
    private String password;
    private String salt;
    private String role;
    private int createdBy;
    private UserProfile profile;
    private UserDAO userDAO;
    private Timestamp lastLogin;

    public User(int id, String username, String password, String role, int createdBy) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.createdBy = createdBy;
        this.userDAO = new UserDAO();
        if (password != null) {
            setPassword(password);
        }
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return getId();
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

    public void setPassword(String plainPassword) {
        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] saltBytes = new byte[SALT_LENGTH];
            random.nextBytes(saltBytes);
            this.salt = Base64.getEncoder().encodeToString(saltBytes);

            // Hash the password with the salt
            this.password = hashPassword(plainPassword, saltBytes);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error hashing password", e);
            throw new RuntimeException("Error setting password", e);
        }
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    public boolean isManager() {
        return "manager".equalsIgnoreCase(role);
    }

    public boolean hasPermission(String permission) {
        return userDAO.hasPermission(id, permission);
    }

    public boolean canAddManager() {
        return isAdmin() && hasPermission("add_manager");
    }

    public boolean canDeleteManager() {
        return isAdmin() && hasPermission("delete_manager");
    }

    public boolean canAddItem() {
        return (isAdmin() || isManager()) && hasPermission("add_item");
    }

    public boolean canDeleteItem() {
        return (isAdmin() || isManager()) && hasPermission("delete_item");
    }

    public boolean canDeleteComment() {
        return (isAdmin() || isManager()) && hasPermission("delete_comment");
    }

    public boolean verifyPassword(String plainPassword) {
        try {
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            String hashedInput = hashPassword(plainPassword, saltBytes);
            return hashedInput.equals(password);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error verifying password", e);
            return false;
        }
    }

    private String hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        byte[] hashedPassword = md.digest(password.getBytes());
        return Base64.getEncoder().encodeToString(hashedPassword);
    }

    public void updateLastLogin() {
        this.lastLogin = new Timestamp(System.currentTimeMillis());
        userDAO.updateLastLogin(this);
    }
} 
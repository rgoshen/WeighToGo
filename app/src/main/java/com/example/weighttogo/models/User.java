package com.example.weighttogo.models;

/**
 * Model class representing a user account.
 */
public class User {

    private long userId;
    private String username;
    private String passwordHash;
    private String salt;
    private String createdAt;
    private String lastLogin;

    public User() {
        // Default constructor
    }

    public User(long userId, String username, String passwordHash, String salt,
                String createdAt, String lastLogin) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", lastLogin='" + lastLogin + '\'' +
                '}';
    }
}

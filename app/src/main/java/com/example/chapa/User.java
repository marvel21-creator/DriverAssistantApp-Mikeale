package com.example.chapa;

public class User {
    private String name;
    private String username;
    private String contactInfo;
    private String password;

    public User() {
        // Firestore requires a no-argument constructor
    }

    public User(String name, String username, String contactInfo, String password) {
        this.name = name;
        this.username = username;
        this.contactInfo = contactInfo;
        this.password = password; // In production, store hashed passwords!
    }

    // Getters and Setters (optional)
}
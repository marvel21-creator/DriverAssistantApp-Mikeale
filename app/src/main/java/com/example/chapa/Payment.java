package com.example.chapa;

import java.io.Serializable;

public class Payment implements Serializable {
    private int amount;
    private String phoneNumber;
    private String paymentMethod;
    private String status; // You can use this to store payment status or any other info

    // Constructor
    public Payment(int amount, String phoneNumber, String paymentMethod, String status) {
        this.amount = amount;
        this.phoneNumber = phoneNumber;
        this.paymentMethod = paymentMethod;
        this.status = status;
    }

    // Getters
    public int getAmount() {
        return amount;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    // Optionally, you can add setters if needed
    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
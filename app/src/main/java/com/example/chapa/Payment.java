package com.example.chapa;

import java.io.Serializable;

public class Payment implements Serializable {
    private int amount; // Amount in the smallest currency unit (e.g., cents)
    private String phoneNumber; // User's phone number
    private String paymentMethod; // Payment method (e.g., Stripe, Credit Card, etc.)
    private String status; // Payment status (e.g., "Success", "Failed", "Pending")
    private String transactionId; // Unique transaction ID
    private long timestamp; // Timestamp of the payment

    // Constructor
    public Payment(int amount, String phoneNumber, String paymentMethod, String status, String transactionId, long timestamp) {
        this.amount = amount;
        this.phoneNumber = phoneNumber;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.transactionId = transactionId;
        this.timestamp = timestamp;
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

    public String getTransactionId() {
        return transactionId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setters
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

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "amount=" + amount +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", status='" + status + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
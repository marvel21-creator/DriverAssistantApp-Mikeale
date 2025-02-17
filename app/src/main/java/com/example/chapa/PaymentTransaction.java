package com.example.chapa;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class PaymentTransaction implements Serializable {
    private String transactionId;
    private int amount;
    private String phoneNumber;
    private String paymentMethod;
    private Timestamp timestamp; // Keep this as Timestamp
    private String status;

    public PaymentTransaction(String transactionId, int amount, String phoneNumber,
                              String paymentMethod, Timestamp timestamp, String status) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.phoneNumber = phoneNumber;
        this.paymentMethod = paymentMethod;
        this.timestamp = timestamp; // Store directly as Timestamp
        this.status = status;
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Timestamp getTimestamp() { // Return Timestamp
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) { // Accept Timestamp
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
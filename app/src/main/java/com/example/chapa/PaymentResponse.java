package com.example.chapa;

import java.io.Serializable;

public class PaymentResponse implements Serializable {
    // Define fields based on your expected response structure
    private String status;
    private String message;

    public PaymentResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
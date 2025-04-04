package com.example.chapa;

public class Transaction {
    private String id;
    private String name;
    private String amount;
    private String time;
    private String phone;

    public Transaction(String id, String name, String amount, String time, String phone) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.time = time;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAmount() {
        return amount;
    }

    public String getTime() {
        return time;
    }

    public String getPhone() {
        return phone;
    }
}
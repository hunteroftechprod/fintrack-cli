package com.ddzhuk.fintrack.core;

import java.time.LocalDateTime;

public class Transaction {
    public enum Type { INCOME, EXPENSE, TRANSFER_IN, TRANSFER_OUT }

    private Type type;
    private String category;
    private double amount;
    private String note;
    private LocalDateTime timestamp;

    public Transaction() {}

    public Transaction(Type type, String category, double amount, String note) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.note = note;
        this.timestamp = LocalDateTime.now();
    }

    public Type getType() { return type; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getNote() { return note; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

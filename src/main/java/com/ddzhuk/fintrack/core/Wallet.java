package com.ddzhuk.fintrack.core;

import java.util.*;
import java.util.stream.Collectors;

public class Wallet {
    private String ownerLogin;
    private Map<String, Double> budgets = new HashMap<>();
    private List<Transaction> transactions = new ArrayList<>();

    public Wallet() {}

    public Wallet(String ownerLogin) {
        this.ownerLogin = ownerLogin;
    }

    public String getOwnerLogin() { return ownerLogin; }

    public Map<String, Double> getBudgets() {
        return budgets;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setBudget(String category, double amount) {
        budgets.put(category, amount);
    }

    public void removeBudget(String category) {
        budgets.remove(category);
    }

    public double totalIncome() {
        return transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.INCOME || t.getType() == Transaction.Type.TRANSFER_IN)
                .mapToDouble(Transaction::getAmount).sum();
    }

    public double totalExpense() {
        return transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.EXPENSE || t.getType() == Transaction.Type.TRANSFER_OUT)
                .mapToDouble(Transaction::getAmount).sum();
    }

    public double balance() {
        return totalIncome() - totalExpense();
    }

    public Map<String, Double> expensesByCategory() {
        Map<String, Double> map = new HashMap<>();
        for (Transaction t : transactions) {
            if (t.getType() == Transaction.Type.EXPENSE || t.getType() == Transaction.Type.TRANSFER_OUT) {
                map.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        return map;
    }

    public Map<String, Double> incomeByCategory() {
        Map<String, Double> map = new HashMap<>();
        for (Transaction t : transactions) {
            if (t.getType() == Transaction.Type.INCOME || t.getType() == Transaction.Type.TRANSFER_IN) {
                map.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        return map;
    }

    public Map<String, Double> expensesByCategories(Collection<String> cats) {
        Set<String> set = new HashSet<>(cats);
        Map<String, Double> res = new HashMap<>();
        for (Map.Entry<String, Double> e : expensesByCategory().entrySet()) {
            if (set.contains(e.getKey())) res.put(e.getKey(), e.getValue());
        }
        return res;
    }

    public Map<String, Double> incomeByCategories(Collection<String> cats) {
        Set<String> set = new HashSet<>(cats);
        Map<String, Double> res = new HashMap<>();
        for (Map.Entry<String, Double> e : incomeByCategory().entrySet()) {
            if (set.contains(e.getKey())) res.put(e.getKey(), e.getValue());
        }
        return res;
    }
}

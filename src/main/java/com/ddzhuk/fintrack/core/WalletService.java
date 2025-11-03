package com.ddzhuk.fintrack.core;

import com.ddzhuk.fintrack.infra.FileStorage;
import java.io.IOException;
import java.util.*;

public class WalletService {
    private final FileStorage storage;
    private final Map<String, Wallet> inMemory = new HashMap<>();

    public WalletService(FileStorage storage) {
        this.storage = storage;
    }

    public Wallet load(String login) {
        try {
            Wallet w = storage.loadWallet(login);
            inMemory.put(login, w);
            return w;
        } catch (IOException e) {
            Wallet w = new Wallet(login);
            inMemory.put(login, w);
            return w;
        }
    }

    public void save(String login) throws IOException {
        Wallet w = inMemory.get(login);
        if (w != null) storage.saveWallet(w);
    }

    public Wallet get(String login) {
        return inMemory.get(login);
    }

    public String addIncome(Wallet w, String category, double amount, String note) {
        String val = validateAmount(amount);
        if (val != null) return val;
        w.getTransactions().add(new Transaction(Transaction.Type.INCOME, category, amount, note));
        return alertAfterChange(w);
    }

    public String addExpense(Wallet w, String category, double amount, String note) {
        String val = validateAmount(amount);
        if (val != null) return val;
        w.getTransactions().add(new Transaction(Transaction.Type.EXPENSE, category, amount, note));
        return checkBudgetAlerts(w, category) + alertAfterChange(w);
    }

    public String setBudget(Wallet w, String category, double amount) {
        String val = validateAmount(amount);
        if (val != null) return val;
        w.setBudget(category, amount);
        return "Бюджет установлен: " + category + " = " + amount;
    }

    public String editBudget(Wallet w, String category, Double amount) {
        if (amount == null) {
            w.removeBudget(category);
            return "Бюджет удалён для категории: " + category;
        }
        return setBudget(w, category, amount);
    }

    public String transfer(Wallet from, Wallet to, double amount, String note) {
        String val = validateAmount(amount);
        if (val != null) return val;
        from.getTransactions().add(new Transaction(Transaction.Type.TRANSFER_OUT, "Перевод", amount, note));
        to.getTransactions().add(new Transaction(Transaction.Type.TRANSFER_IN, "Перевод", amount, note));
        return alertAfterChange(from);
    }

    private String validateAmount(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            return "Ошибка: сумма должна быть положительным числом.";
        }
        return null;
    }

    private String checkBudgetAlerts(Wallet w, String category) {
        Double limit = w.getBudgets().get(category);
        if (limit == null) return "";
        double spent = w.expensesByCategory().getOrDefault(category, 0.0);
        StringBuilder sb = new StringBuilder();
        double ratio = spent / limit;
        if (ratio >= 1.0) {
            sb.append("⚠ Превышение бюджета по категории '").append(category).append("': потрачено ")
              .append(spent).append(" из ").append(limit).append(".\n");
        } else if (ratio >= 0.8) {
            sb.append("ℹ Предупреждение: израсходовано ")
              .append(Math.round(ratio * 100)).append("% бюджета категории '").append(category).append("'.\n");
        }
        return sb.toString();
    }

    private String alertAfterChange(Wallet w) {
        if (w.totalExpense() > w.totalIncome()) {
            return "⚠ Расходы превышают доходы. Баланс = " + w.balance() + "\n";
        }
        if (w.balance() == 0) {
            return "ℹ Баланс нулевой.\n";
        }
        return "";
    }

    public String summary(Wallet w) {
        StringBuilder sb = new StringBuilder();
        sb.append("Общий доход: ").append(w.totalIncome()).append("\n");
        Map<String, Double> inc = w.incomeByCategory();
        if (!inc.isEmpty()) {
            sb.append("Доходы по категориям:\n");
            for (Map.Entry<String, Double> e : inc.entrySet()) {
                sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
            }
        }
        sb.append("Общие расходы: ").append(w.totalExpense()).append("\n");
        sb.append("Бюджет по категориям:\n");
        for (Map.Entry<String, Double> e : w.getBudgets().entrySet()) {
            double spent = w.expensesByCategory().getOrDefault(e.getKey(), 0.0);
            double left = e.getValue() - spent;
            sb.append("  ").append(e.getKey()).append(": ").append(e.getValue())
              .append(", Оставшийся бюджет: ").append(left).append("\n");
        }
        return sb.toString();
    }

    public String summaryByCategories(Wallet w, List<String> cats) {
        Map<String, Double> exp = w.expensesByCategories(cats);
        Map<String, Double> inc = w.incomeByCategories(cats);
        if (exp.isEmpty() && inc.isEmpty()) {
            return "Нет данных по указанным категориям: " + String.join(", ", cats);
        }
        StringBuilder sb = new StringBuilder();
        if (!inc.isEmpty()) {
            sb.append("Доходы по выбранным категориям:\n");
            for (Map.Entry<String, Double> e : inc.entrySet()) {
                sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
            }
        }
        if (!exp.isEmpty()) {
            sb.append("Расходы по выбранным категориям:\n");
            for (Map.Entry<String, Double> e : exp.entrySet()) {
                sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
            }
        }
        return sb.toString();
    }

    public String exportJson(Wallet w, String path) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper().registerModule(
                    new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.writerWithDefaultPrettyPrinter().writeValue(new java.io.File(path), w);
            return "Экспортировано в " + path;
        } catch (Exception e) {
            return "Ошибка экспорта: " + e.getMessage();
        }
    }

    public String exportCsv(Wallet w, String path) {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(path)) {
            pw.println("type,category,amount,note,timestamp");
            for (Transaction t : w.getTransactions()) {
                pw.printf("%s,%s,%.2f,%s,%s%n",
                    t.getType(), t.getCategory(), t.getAmount(),
                    t.getNote() == null ? "" : t.getNote().replace(",", " "),
                    t.getTimestamp().toString());
            }
            return "Экспортировано в " + path;
        } catch (Exception e) {
            return "Ошибка экспорта: " + e.getMessage();
        }
    }

    public String importJson(String path) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper().registerModule(
                    new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            Wallet w = mapper.readValue(new java.io.File(path), Wallet.class);
            inMemory.put(w.getOwnerLogin(), w);
            return "Импортирован кошелёк пользователя: " + w.getOwnerLogin();
        } catch (Exception e) {
            return "Ошибка импорта: " + e.getMessage();
        }
    }
}

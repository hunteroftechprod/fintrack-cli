package com.ddzhuk.fintrack;

import com.ddzhuk.fintrack.core.Wallet;
import com.ddzhuk.fintrack.core.WalletService;
import com.ddzhuk.fintrack.infra.FileStorage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BudgetTest {
    @Test
    void testBudgetLimitExceeded() {
        WalletService s = new WalletService(new FileStorage());
        Wallet w = s.load("t_budget_test");

        s.setBudget(w, "Развлечения", 1000);
        s.addExpense(w, "Развлечения", 1200, "");

        double budget = w.getBudgets().get("Развлечения");
        double spent  = w.expensesByCategory().getOrDefault("Развлечения", 0.0);
        double remaining = budget - spent;

        assertEquals(-200.0, remaining, 1e-6);
    }
}

package com.ddzhuk.fintrack;

import com.ddzhuk.fintrack.core.Wallet;
import com.ddzhuk.fintrack.core.WalletService;
import com.ddzhuk.fintrack.infra.FileStorage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class WalletCalculationsTest {

    @Test
    void totalsIncomeExpense() {
        WalletService s = new WalletService(new FileStorage());
        Wallet w = s.load("t_totals");
        s.addIncome(w, "Зарплата", 2000, "");
        s.addIncome(w, "Бонус", 500, "");
        s.addExpense(w, "Еда", 300, "");
        assertEquals(2500.0, w.totalIncome(), 1e-6);
        assertEquals(300.0, w.totalExpense(), 1e-6);
        assertEquals(2200.0, w.balance(), 1e-6);
    }

    @Test
    void byCategoryAndSelected() {
        WalletService s = new WalletService(new FileStorage());
        Wallet w = s.load("t_cats");
        s.addIncome(w, "Зарплата", 1000, "");
        s.addIncome(w, "Продажи", 500, "");
        s.addExpense(w, "Еда", 200, "");
        s.addExpense(w, "Еда", 100, "");
        s.addExpense(w, "Такси", 50, "");
        Map<String, Double> exp = w.expensesByCategory();
        assertEquals(300.0, exp.get("Еда"), 1e-6);
        assertEquals(50.0, exp.get("Такси"), 1e-6);

        String subset = s.summaryByCategories(w, List.of("Еда","Зарплата"));
        assertTrue(subset.contains("Еда: 300.0"));
        assertTrue(subset.contains("Зарплата: 1000.0"));
    }

    @Test
    void unknownSelectedCategoriesHandled() {
        WalletService s = new WalletService(new FileStorage());
        Wallet w = s.load("t_unknown_cats");
        String msg = s.summaryByCategories(w, List.of("НетТаких"));
        assertTrue(msg.contains("Нет данных"));
    }
}

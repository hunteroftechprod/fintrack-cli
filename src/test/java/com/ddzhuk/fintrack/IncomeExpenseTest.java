package com.ddzhuk.fintrack;

import com.ddzhuk.fintrack.core.Wallet;
import com.ddzhuk.fintrack.core.WalletService;
import com.ddzhuk.fintrack.infra.FileStorage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class IncomeExpenseTest {
    @Test
    void testAddIncomeAndExpense() {
        WalletService s = new WalletService(new FileStorage());
        Wallet w = s.load("t_income_test"); // создаст новый, если файла нет

        s.addIncome(w, "Зарплата", 1000, "");
        s.addExpense(w, "Еда", 300, "");

        assertEquals(700.0, w.balance(), 1e-6);
        assertEquals(1000.0, w.totalIncome(), 1e-6);
        assertEquals(300.0, w.totalExpense(), 1e-6);
    }
}

package com.ddzhuk.fintrack;

import static org.junit.jupiter.api.Assertions.*;

import com.ddzhuk.fintrack.core.Wallet;
import com.ddzhuk.fintrack.core.WalletService;
import com.ddzhuk.fintrack.infra.FileStorage;
import org.junit.jupiter.api.Test;

public class WalletServiceTest {

    @Test
    void budgetsAndAlerts() {
        WalletService s = new WalletService(new FileStorage());
        Wallet w = s.load("tester");
        s.setBudget(w, "Еда", 1000);
        String msg1 = s.addExpense(w, "Еда", 850, "продукты");
        assertTrue(msg1.contains("Предупреждение") || msg1.isEmpty());
        String msg2 = s.addExpense(w, "Еда", 200, "кафе");
        assertTrue(msg2.contains("Превышение бюджета"));
    }

    @Test
    void incomeVsExpenseBalance() {
        WalletService s = new WalletService(new FileStorage());
        Wallet w = s.load("balanceUser");
        s.addIncome(w, "Зарплата", 1000, "");
        String msg = s.addExpense(w, "Коммунальные услуги", 1200, "");
        assertTrue(msg.contains("Расходы превышают доходы"));
    }
}

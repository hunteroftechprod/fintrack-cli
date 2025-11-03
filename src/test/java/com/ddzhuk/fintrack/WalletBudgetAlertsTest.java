package com.ddzhuk.fintrack;

import com.ddzhuk.fintrack.core.Wallet;
import com.ddzhuk.fintrack.core.WalletService;
import com.ddzhuk.fintrack.infra.FileStorage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WalletBudgetAlertsTest {

    @Test
    void warnAt80Percent() {
        WalletService s = new WalletService(new FileStorage());
        Wallet w = s.load("t_warn80");
        s.setBudget(w, "Еда", 1000);
        String msg = s.addExpense(w, "Еда", 850, "продукты");
        assertTrue(msg.contains("Предупреждение") || msg.contains("⚠"), "Должно предупреждать при >=80%");
    }

    @Test
    void exceedBudgetAlert() {
        WalletService s = new WalletService(new FileStorage());
        Wallet w = s.load("t_exceed");
        s.setBudget(w, "Еда", 1000);
        s.addExpense(w, "Еда", 800, "");
        String msg = s.addExpense(w, "Еда", 300, "");
        assertTrue(msg.contains("Превышение бюджета"), "Должен быть алерт на перерасход");
    }

    @Test
    void expensesGreaterThanIncomeAlert() {
        WalletService s = new WalletService(new FileStorage());
        Wallet w = s.load("t_minus");
        String msg = s.addExpense(w, "Коммунальные", 1200, "");
        assertTrue(msg.contains("Расходы превышают доходы"), "Должен быть алерт о минусе");
    }

    @Test
    void zeroBalanceInfo() {
        WalletService s = new WalletService(new FileStorage());
        Wallet w = s.load("t_zero");
        s.addIncome(w, "Зарплата", 1000, "");
        String msg = s.addExpense(w, "Еда", 1000, "");
        assertTrue(msg.contains("Баланс нулевой"), "При нулевом балансе должно сообщать");
    }
}

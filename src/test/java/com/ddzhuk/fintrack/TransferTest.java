package com.ddzhuk.fintrack;

import com.ddzhuk.fintrack.core.Wallet;
import com.ddzhuk.fintrack.core.WalletService;
import com.ddzhuk.fintrack.infra.FileStorage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TransferTest {

    @Test
    void transferAffectsBothWallets() {
        WalletService s = new WalletService(new FileStorage());
        Wallet w1 = s.load("t_from");
        Wallet w2 = s.load("t_to");
        s.addIncome(w1, "Зарплата", 1000, "");

        s.transfer(w1, w2, 250, "перевод");

        assertEquals(250.0, w2.totalIncome(), 1e-6, "Получатель должен получить перевод");
        assertEquals(250.0, w1.totalExpense(), 1e-6, "У отправителя должен уменьшиться баланс");
        assertTrue(w2.incomeByCategory().containsKey("Перевод"), "Доход должен быть отмечен в категории 'Перевод'");
    }
}

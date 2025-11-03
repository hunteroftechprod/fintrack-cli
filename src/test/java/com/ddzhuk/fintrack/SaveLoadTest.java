package com.ddzhuk.fintrack;

import com.ddzhuk.fintrack.core.Wallet;
import com.ddzhuk.fintrack.core.WalletService;
import com.ddzhuk.fintrack.infra.FileStorage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SaveLoadTest {
    @Test
    void testSaveAndLoadWallet() throws Exception {
        WalletService s = new WalletService(new FileStorage());
        String login = "t_save_load_" + System.currentTimeMillis(); // уникальный логин
        Wallet w1 = s.load(login);
        s.addIncome(w1, "Подарок", 500, "");
        s.save(login); // пишет users/<login>.json

        WalletService s2 = new WalletService(new FileStorage());
        Wallet w2 = s2.load(login);
        assertNotNull(w2);
        assertEquals(500.0, w2.totalIncome(), 1e-6);
    }
}

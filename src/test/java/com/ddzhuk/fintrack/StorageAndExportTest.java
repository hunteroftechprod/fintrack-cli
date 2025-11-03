package com.ddzhuk.fintrack;

import com.ddzhuk.fintrack.core.Wallet;
import com.ddzhuk.fintrack.core.WalletService;
import com.ddzhuk.fintrack.infra.FileStorage;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class StorageAndExportTest {

    @AfterEach
    void cleanup() throws Exception {
        // аккуратно чистим файлы конкретных тест-логинов
        for (String user : new String[]{"t_save","t_export","t_import_a","t_import_b"}) {
            Files.deleteIfExists(Path.of("users", user + ".json"));
        }
        Files.deleteIfExists(Path.of("out_test.json"));
        Files.deleteIfExists(Path.of("out_test.csv"));
    }

    @Test
    void saveAndLoadWalletJson() throws Exception {
        WalletService s = new WalletService(new FileStorage());
        Wallet w = s.load("t_save");
        s.addIncome(w, "Зарплата", 777, "");
        s.save("t_save");

        File f = new File("users/t_save.json");
        assertTrue(f.exists() && f.length() > 0);

        WalletService s2 = new WalletService(new FileStorage());
        Wallet w2 = s2.load("t_save");
        assertEquals(777.0, w2.totalIncome(), 1e-6);
    }

    @Test
    void exportJsonAndCsv() {
        WalletService s = new WalletService(new FileStorage());
        Wallet w = s.load("t_export");
        s.addIncome(w, "Продажи", 123.45, "ok");
        String j = s.exportJson(w, "out_test.json");
        String c = s.exportCsv(w, "out_test.csv");
        assertTrue(j.contains("Экспортировано"), "JSON экспорт должен сообщить об успехе");
        assertTrue(c.contains("Экспортировано"), "CSV экспорт должен сообщить об успехе");
        assertTrue(new File("out_test.json").exists());
        assertTrue(new File("out_test.csv").exists());
    }

    @Test
    void importJsonIntoMemory() {
        WalletService s = new WalletService(new FileStorage());
        Wallet a = s.load("t_import_a");
        s.addIncome(a, "Х", 100, "");
        s.exportJson(a, "out_test.json");

        WalletService s2 = new WalletService(new FileStorage());
        String msg = s2.importJson("out_test.json");
        assertTrue(msg.contains("Импортирован кошелёк пользователя: t_import_a"));
        Wallet loaded = s2.get("t_import_a");
        assertNotNull(loaded);
        assertEquals(100.0, loaded.totalIncome(), 1e-6);
    }
}

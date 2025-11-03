package com.ddzhuk.fintrack.cli;

import com.ddzhuk.fintrack.core.Wallet;
import com.ddzhuk.fintrack.core.WalletService;
import com.ddzhuk.fintrack.infra.AuthService;
import com.ddzhuk.fintrack.infra.FileStorage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String HELP = 
"Команды:\n" +
"  register <login> <password>             — регистрация\n" +
"  login <login> <password>                — вход (загружает кошелёк)\n" +
"  logout                                  — выход из аккаунта (с сохранением)\n" +
"  add-income <category> <amount> [note]   — добавить доход\n" +
"  add-expense <category> <amount> [note]  — добавить расход\n" +
"  set-budget <category> <amount>          — установить/изменить бюджет\n" +
"  del-budget <category>                   — удалить бюджет\n" +
"  transfer <toLogin> <amount> [note]      — перевод между кошельками\n" +
"  summary                                 — сводка по всем\n" +
"  summary-cats <cat1,cat2,...>            — сводка по категориям\n" +
"  export-json <path>                      — экспорт в JSON\n" +
"  export-csv <path>                       — экспорт в CSV\n" +
"  import-json <path>                      — импорт кошелька из JSON\n" +
"  save                                    — сохранить кошелёк в файл\n" +
"  help                                    — показать помощь\n" +
"  exit                                    — выход из приложения\n";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        AuthService auth = new AuthService();
        WalletService service = new WalletService(new FileStorage());

        System.out.println("FinTrack CLI. Введите 'help' для списка команд.");
        while (true) {
            System.out.print("> ");
            if (!sc.hasNextLine()) break;
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();

            try {
                switch (cmd) {
                    case "help" -> System.out.println(HELP);
                    case "register" -> {
                        if (parts.length < 3) { System.out.println("Формат: register <login> <password>"); break; }
                        boolean ok = auth.register(parts[1], parts[2]);
                        System.out.println(ok ? "Ок" : "Ошибка регистрации (логин занят или пароль короткий)");
                    }
                    case "login" -> {
                        if (parts.length < 3) { System.out.println("Формат: login <login> <password>"); break; }
                        boolean ok = auth.login(parts[1], parts[2]);
                        if (!ok) { System.out.println("Неверный логин/пароль"); break; }
                        Wallet w = service.load(parts[1]);
                        System.out.println("Вход выполнен. Баланс: " + w.balance());
                    }
                    case "logout" -> {
                        if (auth.currentUser() != null) {
                            service.save(auth.currentUser().getLogin());
                        }
                        auth.logout();
                        System.out.println("Вы вышли из аккаунта.");
                    }
                    case "add-income" -> {
                        Wallet w = needAuth(auth, service);
                        if (w == null) break;
                        if (parts.length < 3) { System.out.println("Формат: add-income <category> <amount> [note]"); break; }
                        double amount = parseAmount(parts[2]);
                        String note = parts.length > 3 ? String.join(" ", Arrays.copyOfRange(parts, 3, parts.length)) : "";
                        System.out.print(service.addIncome(w, parts[1], amount, note));
                    }
                    case "add-expense" -> {
                        Wallet w = needAuth(auth, service);
                        if (w == null) break;
                        if (parts.length < 3) { System.out.println("Формат: add-expense <category> <amount> [note]"); break; }
                        double amount = parseAmount(parts[2]);
                        String note = parts.length > 3 ? String.join(" ", Arrays.copyOfRange(parts, 3, parts.length)) : "";
                        System.out.print(service.addExpense(w, parts[1], amount, note));
                    }
                    case "set-budget" -> {
                        Wallet w = needAuth(auth, service);
                        if (w == null) break;
                        if (parts.length < 3) { System.out.println("Формат: set-budget <category> <amount>"); break; }
                        double amount = parseAmount(parts[2]);
                        System.out.println(service.setBudget(w, parts[1], amount));
                    }
                    case "del-budget" -> {
                        Wallet w = needAuth(auth, service);
                        if (w == null) break;
                        if (parts.length < 2) { System.out.println("Формат: del-budget <category>"); break; }
                        System.out.println(service.editBudget(w, parts[1], null));
                    }
                    case "transfer" -> {
                        if (auth.currentUser() == null) { System.out.println("Требуется авторизация."); break; }
                        if (parts.length < 3) { System.out.println("Формат: transfer <toLogin> <amount> [note]"); break; }
                        String toLogin = parts[1];
                        double amount = parseAmount(parts[2]);
                        String note = parts.length > 3 ? String.join(" ", Arrays.copyOfRange(parts, 3, parts.length)) : "";
                        Wallet from = service.get(auth.currentUser().getLogin());
                        Wallet to = service.get(toLogin);
                        if (to == null) {
                            to = service.load(toLogin);
                        }
                        System.out.print(service.transfer(from, to, amount, note));
                    }
                    case "summary" -> {
                        Wallet w = needAuth(auth, service);
                        if (w == null) break;
                        System.out.print(service.summary(w));
                    }
                    case "summary-cats" -> {
                        Wallet w = needAuth(auth, service);
                        if (w == null) break;
                        if (parts.length < 2) { System.out.println("Формат: summary-cats <cat1,cat2,...>"); break; }
                        List<String> cats = Arrays.asList(parts[1].split(","));
                        System.out.print(service.summaryByCategories(w, cats));
                    }
                    case "export-json" -> {
                        Wallet w = needAuth(auth, service);
                        if (w == null) break;
                        if (parts.length < 2) { System.out.println("Формат: export-json <path>"); break; }
                        System.out.println(service.exportJson(w, parts[1]));
                    }
                    case "export-csv" -> {
                        Wallet w = needAuth(auth, service);
                        if (w == null) break;
                        if (parts.length < 2) { System.out.println("Формат: export-csv <path>"); break; }
                        System.out.println(service.exportCsv(w, parts[1]));
                    }
                    case "import-json" -> {
                        if (parts.length < 2) { System.out.println("Формат: import-json <path>"); break; }
                        System.out.println(service.importJson(parts[1]));
                    }
                    case "save" -> {
                        if (auth.currentUser() == null) { System.out.println("Требуется авторизация."); break; }
                        service.save(auth.currentUser().getLogin());
                        System.out.println("Сохранено.");
                    }
                    case "exit" -> {
                        if (auth.currentUser() != null) {
                            try { service.save(auth.currentUser().getLogin()); } catch (IOException ignored) {}
                        }
                        System.out.println("Пока!");
                        return;
                    }
                    default -> System.out.println("Неизвестная команда. Введите 'help'.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private static Wallet needAuth(AuthService auth, WalletService service) {
        if (auth.currentUser() == null) {
            System.out.println("Требуется авторизация.");
            return null;
        }
        return service.get(auth.currentUser().getLogin());
    }

    private static double parseAmount(String s) {
        try {
            return Double.parseDouble(s.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Сумма должна быть числом.");
        }
    }
}

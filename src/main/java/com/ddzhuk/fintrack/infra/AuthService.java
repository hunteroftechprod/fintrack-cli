package com.ddzhuk.fintrack.infra;

import com.ddzhuk.fintrack.core.User;
import java.util.HashMap;
import java.util.Map;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    private final Map<String, User> users = new HashMap<>();
    private User current;

    public boolean register(String login, String password) {
        if (login == null || login.isBlank() || password == null || password.length() < 4) return false;
        if (users.containsKey(login)) return false;
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        users.put(login, new User(login, hash));
        return true;
    }

    public boolean login(String login, String password) {
        User u = users.get(login);
        if (u == null) return false;
        if (!BCrypt.checkpw(password, u.getPasswordHash())) return false;
        current = u;
        return true;
    }

    public void logout() {
        current = null;
    }

    public User currentUser() { return current; }

    public void putUser(User u) {
        users.put(u.getLogin(), u);
    }
}

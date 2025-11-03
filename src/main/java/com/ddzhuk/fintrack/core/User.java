package com.ddzhuk.fintrack.core;

import java.util.Objects;

public class User {
    private String login;
    private String passwordHash;

    public User() {}

    public User(String login, String passwordHash) {
        this.login = login;
        this.passwordHash = passwordHash;
    }

    public String getLogin() { return login; }
    public String getPasswordHash() { return passwordHash; }
}

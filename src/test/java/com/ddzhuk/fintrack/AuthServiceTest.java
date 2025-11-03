package com.ddzhuk.fintrack;

import com.ddzhuk.fintrack.infra.AuthService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    @Test
    void registerSuccess() {
        AuthService a = new AuthService();
        assertTrue(a.register("u1","pass"));
    }

    @Test
    void registerDuplicateFails() {
        AuthService a = new AuthService();
        assertTrue(a.register("u1","pass"));
        assertFalse(a.register("u1","pass2"));
    }

    @Test
    void loginWrongPassword() {
        AuthService a = new AuthService();
        a.register("u1","pass");
        assertFalse(a.login("u1","wrong"));
    }

    @Test
    void loginSuccess() {
        AuthService a = new AuthService();
        a.register("u1","pass");
        assertTrue(a.login("u1","pass"));
        assertNotNull(a.currentUser());
    }
}

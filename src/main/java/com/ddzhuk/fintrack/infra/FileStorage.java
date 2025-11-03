package com.ddzhuk.fintrack.infra;

import com.ddzhuk.fintrack.core.Wallet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;

public class FileStorage {
    private final ObjectMapper mapper;

    public FileStorage() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    public Wallet loadWallet(String login) throws IOException {
        File f = new File("users/" + login + ".json");
        if (!f.exists()) return new Wallet(login);
        return mapper.readValue(f, Wallet.class);
    }

    public void saveWallet(Wallet wallet) throws IOException {
        File dir = new File("users");
        if (!dir.exists()) dir.mkdirs();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(dir, wallet.getOwnerLogin() + ".json"), wallet);
    }
}

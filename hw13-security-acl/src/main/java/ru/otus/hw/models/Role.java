package ru.otus.hw.models;

public enum Role {
    ROLE_USER("ROLE_USER"),
    ROLE_ADMIN("ROLE_ADMIN");

    public final String name;

    Role(String name) {
        this.name = name;
    }
}

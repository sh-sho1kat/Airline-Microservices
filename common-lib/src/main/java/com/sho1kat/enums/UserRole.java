package com.sho1kat.enums;

import lombok.Getter;

@Getter
public enum UserRole {

    USER("Customer who can search and book flights"),
    STAFF("Airline staff / operations"),
    ADMIN("System administrator");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

}
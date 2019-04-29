package com.cosson.usermanagement.entity;

public enum RoleName {
    ROLE_ADMIN(1), ROLE_MANAGER(2), ROLE_USER(3);

    private final int value;

    RoleName(final int i) {
        value = i;
    }

    public int getValue() {
        return value;
    }
}

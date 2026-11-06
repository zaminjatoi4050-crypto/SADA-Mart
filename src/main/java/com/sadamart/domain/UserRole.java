package com.sadamart.domain;

public enum UserRole {
    ADMIN,
    MANAGER,
    CASHIER;

    public boolean canManageUsers() { return this == ADMIN; }
    public boolean canManageCatalogAndInventory() { return this == ADMIN || this == MANAGER; }
    public boolean canOperatePos() { return true; }
}

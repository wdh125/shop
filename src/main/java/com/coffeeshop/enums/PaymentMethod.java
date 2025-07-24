package com.coffeeshop.enums;

public enum PaymentMethod {
    CASH(UserRole.ROLE_ADMIN),
    QR_CODE(UserRole.ROLE_CUSTOMER),
    CARD(UserRole.ROLE_CUSTOMER);

    private final UserRole allowedRole;

    PaymentMethod(UserRole allowedRole) {
        this.allowedRole = allowedRole;
    }

    public UserRole getAllowedRole() {
        return allowedRole;
    }
}
package io.goorm.board.enums;

public enum UserRole {
    ADMIN("관리자"),
    BUYER("바이어");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Spring Security에서 사용할 권한명 (ROLE_ 접두어)
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}

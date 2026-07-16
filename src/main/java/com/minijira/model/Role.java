package com.minijira.model;

public enum Role {
    ADMIN("Admin"),
    PROJECT_MANAGER("Project Manager"),
    DEVELOPER("Developer"),
    VIEWER("Viewer");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

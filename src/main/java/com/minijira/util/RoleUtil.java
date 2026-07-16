package com.minijira.util;

import com.minijira.model.Role;
import com.minijira.model.User;

public class RoleUtil {

    public static boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }

    public static boolean isProjectManager(User user) {
        return user != null && (user.getRole() == Role.ADMIN || user.getRole() == Role.PROJECT_MANAGER);
    }

    public static boolean isDeveloper(User user) {
        return user != null && (user.getRole() == Role.ADMIN || user.getRole() == Role.PROJECT_MANAGER || user.getRole() == Role.DEVELOPER);
    }

    public static boolean hasRole(User user, Role... roles) {
        if (user == null) {
            return false;
        }
        for (Role role : roles) {
            if (user.getRole() == role) {
                return true;
            }
        }
        return false;
    }
}
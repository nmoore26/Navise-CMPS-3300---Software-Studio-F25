package com.example.navisewebsite.domain;

public final class AccountFactory {
    private AccountFactory() {}

    // @admin.com → Admin with password "admin"; else Student with "student"
    public static Account forEmail(String email) {
        if (email != null && email.endsWith("@admin.com")) {
            return new Admin(email, "admin");
        }
        return new Student(email, "student");
    }
}
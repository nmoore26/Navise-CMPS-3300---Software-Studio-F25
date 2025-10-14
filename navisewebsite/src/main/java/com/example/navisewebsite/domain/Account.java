package com.example.navisewebsite.domain;

public abstract class Account {
    protected final String email;
    protected final String storedPassword;
    protected Account(String email, String storedPassword) {
        this.email = email;
        this.storedPassword = storedPassword;
    }
    public String getEmail() { return email; }

    // ------------ Template Method ------------
    public final String loginFlow(String submittedPassword) {
        validate(submittedPassword);
        authenticate(submittedPassword);
        authorize();
        return postLogin();
    }

    protected void validate(String pw) {
        if (email == null || email.isBlank() || pw == null) {
            throw new IllegalArgumentException("Missing email or password");
        }
    }
    protected void authenticate(String pw) {
        if (!pw.equals(storedPassword)) throw new SecurityException("Invalid credentials");
    }
    protected abstract void authorize();
    protected abstract String postLogin();
}
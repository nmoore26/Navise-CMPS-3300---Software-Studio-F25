package com.example.navisewebsite.domain;

public class Student extends Account {
    public Student(String email, String storedPassword) { super(email, storedPassword); }
    @Override protected void authorize() { /* students: no special gate here */ }
    @Override protected String postLogin() { return "student"; }
}
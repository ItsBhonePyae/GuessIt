package com.guessit.actualapp.model;

public class User {
    private final int userId;
    private final String username;
    private final String email;
    private final String fullName;
    private final int roleId;
    private final String userStatus;

    public User(int userId, String username, String email, String fullName, int roleId, String userStatus) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roleId = roleId;
        this.userStatus = userStatus;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public int getRoleId() {
        return roleId;
    }

    public String getUserStatus() {
        return userStatus;
    }
}

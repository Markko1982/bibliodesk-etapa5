package com.bibliodesk.model;

public class User {
    private int id;
    private String name;
    private String login;
    private String passwordHash;
    private Profile profile;
    private boolean active;

    public User(int id, String name, String login, String passwordHash, Profile profile, boolean active) {
        this.id = id;
        this.name = name;
        this.login = login;
        this.passwordHash = passwordHash;
        this.profile = profile;
        this.active = active;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getLogin() { return login; }
    public String getPasswordHash() { return passwordHash; }
    public Profile getProfile() { return profile; }
    public boolean isActive() { return active; }

    public void setName(String name) { this.name = name; }
    public void setLogin(String login) { this.login = login; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setProfile(Profile profile) { this.profile = profile; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return name + " (" + login + ")";
    }
}

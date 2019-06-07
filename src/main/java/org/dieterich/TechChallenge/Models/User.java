package org.dieterich.TechChallenge.Models;

public class User {
    private String id;
    private String username;
    private String name;
    private String email;

    public User setId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getEmail() {
        return email;
    }
}

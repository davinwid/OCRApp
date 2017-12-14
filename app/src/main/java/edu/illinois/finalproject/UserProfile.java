package edu.illinois.finalproject;

import java.util.ArrayList;

/**
 * Created by Davinwid on 12/6/2017.
 */

public class UserProfile {

    private String name;
    private String email;
    private String userName;

    public UserProfile(String name, String userName, String email) {
        this.name = name;
        this.userName = userName;
        this.email = email;
    }

    UserProfile() {
    }

    public String getName() {
        return name;
    }

    String getUserName() {
        return userName;
    }

    String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
    }

    void setEmail(String email) {
        this.email = email;
    }

    void setUserName(String userName) {
        this.userName = userName;
    }
}

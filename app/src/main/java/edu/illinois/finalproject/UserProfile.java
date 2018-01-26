package edu.illinois.finalproject;

import java.util.ArrayList;

/**
 * Created by Davinwid on 12/6/2017.
 */

class UserProfile {

    private String name;
    private String email;
    private String userName;
    private ArrayList<String> recentSearches;

    public UserProfile(String name, String userName, String email, ArrayList<String> recentSearches) {
        this.name = name;
        this.userName = userName;
        this.email = email;
        this.recentSearches = recentSearches;
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

    public ArrayList<String> getRecentSearches() {
        return recentSearches;
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

    public void setRecentSearches(ArrayList<String> recentSearches) {
        this.recentSearches = recentSearches;
    }
}

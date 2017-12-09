package edu.illinois.finalproject;

/**
 * Created by Davinwid on 12/6/2017.
 */

public class UserProfile {

    private String name;
    private String email;
    private String userName;
    private SearchResult searchResult;

    public UserProfile(String name, String userName, String email, SearchResult searchResult) {
        this.name = name;
        this.userName = userName;
        this.email = email;
        this.searchResult = searchResult;
    }

    public UserProfile() {
    }

    public String getName() {
        return name;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public SearchResult getSearchResult() {
        return searchResult;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setSearchResult(SearchResult searchResult) {
        this.searchResult = searchResult;
    }
}

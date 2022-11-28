package com.example.globe;

public class User {

    // User attributes
    private String  FullName, Username, Email, Age, Country;
    private int NumOfTranslations, NumOfLandmarks, NumOfConversions;

    // Constructor
    public User(String fullName, String username, String email, String age, String country, int numOfTranslations, int numOfLandmarks, int numOfConversions) {
        FullName = fullName;
        Username = username;
        Email = email;
        Age = age;
        Country = country;
        NumOfTranslations = numOfTranslations;
        NumOfLandmarks = numOfLandmarks;
        NumOfConversions = numOfConversions;
    }

    public User(){}

    // Getters and setters
    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getAge() {
        return Age;
    }

    public void setAge(String age) {
        Age = age;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public int getNumOfTranslations() {
        return NumOfTranslations;
    }

    public void setNumOfTranslations(int numOfTranslations) {
        NumOfTranslations = numOfTranslations;
    }

    public int getNumOfLandmarks() {
        return NumOfLandmarks;
    }

    public void setNumOfLandmarks(int numOfLandmarks) {
        NumOfLandmarks = numOfLandmarks;
    }

    public int getNumOfConversions() {
        return NumOfConversions;
    }

    public void setNumOfConversions(int numOfConversions) {
        NumOfConversions = numOfConversions;
    }
}

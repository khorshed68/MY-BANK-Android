package com.khorshed.mybank.models;

public class BankConfiguration {
    private String id;
    private String key;
    private String value;
    private String category;
    private String description;

    public BankConfiguration() {
        // Empty constructor for Firestore
    }

    public BankConfiguration(String id, String key, String value, String category, String description) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.category = category;
        this.description = description;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

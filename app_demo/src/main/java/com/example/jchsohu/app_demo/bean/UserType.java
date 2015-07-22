package com.example.jchsohu.app_demo.bean;

/**
 * Created by jchsohu on 15-7-9.
 */
public enum UserType {

    BROADCAST("master"), VIEWER("viewer");

    private String value;

    private UserType(String type) {
        this.value = type;
    }

    public String getTypeValue() {
        return value;
    }
}

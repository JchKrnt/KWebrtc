package com.jch.kw.bean;

/**
 * Created by jingbiaowang on 2015/7/23.
 */
public enum UserType {

    MASTER, VIEWER;

    public String getVauleStr() {
        return name().toLowerCase();
    }

    public static UserType fromCanonicalForm(String canonical) {
        return UserType.valueOf(UserType.class, canonical.toUpperCase());
    }
}

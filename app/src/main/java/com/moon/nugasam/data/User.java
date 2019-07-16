package com.moon.nugasam.data;

public class User {
    public String name;
    public Integer nuga;
    public String imageUrl;
    public String fullName;
    public Integer permission;

    public User(){}

    public User(String name, Integer nuga, String imageUrl, String fullName) {
        this.name = name;
        this.nuga = nuga;
        this.imageUrl = imageUrl;
        this.fullName = fullName;
        this.permission = 0;
    }
}

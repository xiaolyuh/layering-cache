package com.github.xiaolyuh.domain;

public class User {

    private long userId;

    private String name;

    private String[] lastName;

    private int age;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getLastName() {
        return lastName;
    }

    public void setLastName(String[] lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}

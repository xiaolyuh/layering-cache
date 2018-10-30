package com.github.xiaolyuh.demo.entity;

import java.io.Serializable;

public class Person implements Serializable {
    private long id;

    private String name;

    private Integer age;

    private String address;

    public Person() {
        super();
    }

    public Person(long id) {
        this.id = id;
    }

    public Person(long id, String name, Integer age, String address) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.address = address;
    }

    public Person(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Person [id=" + id + ", name=" + name + ", age=" + age + ", address=" + address + "]";
    }

}

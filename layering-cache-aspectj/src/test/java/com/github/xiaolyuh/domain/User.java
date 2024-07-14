package com.github.xiaolyuh.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class User implements Serializable {

    public User() {
        this.userId = 11L;
        this.name = "name";
        this.address = new Address();
        this.lastName = new String[]{"w", "四川", "~！@#%……&*（）——+{}：“？》:''\">?《~!@#$%^&*()_+{}\\"};
        List<String> lastNameList = new ArrayList<>();
        lastNameList.add("W");
        lastNameList.add("成都");
        this.lastNameList = lastNameList;
        this.lastNameSet = new HashSet<>(lastNameList);
        this.lastName = new String[]{"w", "四川", "~！@#%……&*（）——+{}：“？》:''\">?《~!@#$%^&*()_+{}\\"};
        this.age = 122;
        this.height = 18.2;
        this.income = new BigDecimal(22.22);
        this.birthday = new Date();
    }

    public User(long userId,int age) {
        this();
        this.userId = userId;
        this.age = age;
    }

    public User(long userId ) {
        this();
        this.userId = userId;
    }

    private long userId;

    private String name;

    private Address address;

    private String[] lastName;

    private List<String> lastNameList;

    private Set<String> lastNameSet;

    private int age;

    private double height;

    private BigDecimal income;

    private Date birthday;

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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<String> getLastNameList() {
        return lastNameList;
    }

    public void setLastNameList(List<String> lastNameList) {
        this.lastNameList = lastNameList;
    }

    public Set<String> getLastNameSet() {
        return lastNameSet;
    }

    public void setLastNameSet(Set<String> lastNameSet) {
        this.lastNameSet = lastNameSet;
    }

    public BigDecimal getIncome() {
        return income.setScale(2, BigDecimal.ROUND_UP);
    }

    public void setIncome(BigDecimal income) {
        this.income = income;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public static class Address implements Serializable {
        private String addredd;

        public Address() {
            this.addredd = "成都";
        }

        public String getAddredd() {
            return addredd;
        }

        public void setAddredd(String addredd) {
            this.addredd = addredd;
        }
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
}

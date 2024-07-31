package com.github.xiaolyuh.demo.service;


import com.github.xiaolyuh.demo.entity.Person;
import java.util.List;

public interface PersonService {
    Person save(Person person);

    void remove(Long id);

    void removeAll();

    Person findOne(Person person);

    List<Person> batch(List<Person> personList);
}

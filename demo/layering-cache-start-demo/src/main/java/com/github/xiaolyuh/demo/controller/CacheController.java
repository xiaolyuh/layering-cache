package com.github.xiaolyuh.demo.controller;

import com.github.xiaolyuh.demo.entity.Person;
import com.github.xiaolyuh.demo.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CacheController {

    @Autowired
    PersonService personService;

    @RequestMapping("/put")
    public long put(@RequestBody Person person) {
        Person p = personService.save(person);
        return 1L;
    }

    @RequestMapping("/able")
    public Person cacheable(@RequestBody Person person) {

        return personService.findOne(person);
    }

    @RequestMapping("/evit")
    public String evit(@RequestBody Person person) {

        personService.remove(person.getId());
        return "ok";
    }

}
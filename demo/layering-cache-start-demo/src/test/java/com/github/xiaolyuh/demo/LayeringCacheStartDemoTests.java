package com.github.xiaolyuh.demo;

import com.github.xiaolyuh.demo.entity.Person;
import com.github.xiaolyuh.demo.service.PersonService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LayeringCacheStartDemoTests {

    @Autowired
    private PersonService personService;

    @Test
    public void testSave() {
        Person p = new Person(1, "name1", 12, "address1");
        personService.save(p);

        Person person = personService.findOne(p);
        Assert.assertEquals(person.getId(), 1);
    }

    @Test
    public void testRemove() {
        Person p = new Person(5, "name1", 12, "address1");
        personService.save(p);

        personService.remove(5L);
        Person person = personService.findOne(p);
        Assert.assertEquals(person.getId(), 2);
    }

    @Test
    public void testRemoveAll() throws InterruptedException {
        Person p = new Person(6, "name1", 12, "address1");
        personService.save(p);

        Person person = personService.findOne(p);
        Assert.assertEquals(person.getId(), 6);
        personService.removeAll();

        Thread.sleep(1000);
        person = personService.findOne(p);
        Assert.assertEquals(person.getId(), 2);
    }

    @Test
    public void testFindOne() {
        Person p = new Person(2);
        personService.findOne(p);
        Person person = personService.findOne(p);
        Assert.assertEquals(person.getName(), "name2");
    }


}

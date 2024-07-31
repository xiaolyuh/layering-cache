package com.github.xiaolyuh.demo.service;

import com.alibaba.fastjson.JSON;
import com.github.xiaolyuh.demo.entity.Person;
import com.github.xiaolyuh.demo.utils.OkHttpClientUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PersonServiceTest {

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


    @Test
//    @Ignore
    public void batch() throws IOException {

        Person person0 = new Person(100, "name1", 12, "address1");
        Person person1 = new Person(1, "name1", 12, "address1");
        Person person2 = new Person(1000, "name1", 12, "address1");
        Person person3 = new Person(2, "name1", 12, "address1");
        Person person4 = new Person(1001, "name1", 12, "address1");

        List<Person> personList = Arrays.asList(person0, person1, person2, person3, person4);

        List<Person> batch = personService.batch(personList);
        Assert.assertEquals(batch.size(), 3);
    }


}

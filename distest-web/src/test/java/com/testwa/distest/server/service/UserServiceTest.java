package com.testwa.distest.server.service;

import com.testwa.core.base.exception.AccountAlreadyExistException;
import com.testwa.core.base.exception.AccountException;
import com.testwa.distest.WebServerApplication;
import com.testwa.distest.common.enums.DB;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.user.service.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

/**
 * Created by wen on 19/10/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = WebServerApplication.class)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testInsert() throws AccountException, AccountAlreadyExistException {
        long countBefore =userService.count();
        User user = new User();
        user.setUsername("xiaoming1");
        user.setPassword("xiaoming1");
        user.setEmail("xiaoming1@testwa.com");
        userService.save(user);
        long countAfter =userService.count();
        Assert.assertEquals(++countBefore,countAfter);
    }

    @Test
    public void testFindByUserIds() {
        List<Long> idList = Arrays.asList(new Long[] {1l, 2l});
        List<User> users = userService.findByUserIds(idList);
    }

    @Test
    public void testFindOne(){
        User user = userService.findOne(1l);
        System.out.println(user.toString());
    }

    @Test
    public void testFindBy(){
        User u = userService.findByUsername("admin");
        System.out.println(u.toString());
        User u1 = userService.findByEmail("xiaoming@testwa.com");
        System.out.println(u1.toString());
    }

    @Test
    public void testUpdate(){
        User user = userService.findByEmail("xiaoming@testwa.com");
        user.setPhone("18600753024");
        user.setNickname("wen");
        user.setSex(DB.Sex.MALE);
        int val = userService.update(user);
        Assert.assertEquals(val, 1);
    }

    @Test
    public void testDelete(){
        int val = userService.delete(4);
        Assert.assertEquals(val, 1);
    }

    @Test
    public void testDeleteAll(){
        List<Long> idList = Arrays.asList(new Long[] {1l, 2l});
        userService.deleteAll(idList);
    }

    @Test
    public void testFindByPage(){
        User user = new User();
        PageResult<User> pr = userService.findByPage(user, 1, 10);
        System.out.println(pr.getTotal());
    }


}

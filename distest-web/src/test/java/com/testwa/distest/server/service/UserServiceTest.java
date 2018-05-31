package com.testwa.distest.server.service;

import com.testwa.core.base.exception.AccountAlreadyExistException;
import com.testwa.core.base.exception.AccountException;
import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.common.enums.DB;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.user.service.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by wen on 19/10/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-dev.properties")
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testInsert() throws AccountException, AccountAlreadyExistException {
        long countBefore =userService.count();
        User user = new User();
        user.setUsername("aaa1");
        user.setPassword("123456");
        user.setEmail("aaa1@testwa.com");
        user.setSex(DB.Sex.MALE);
        String code = userService.save(user);
        System.out.println(code);
        long countAfter =userService.count();
        Assert.assertEquals(++countBefore,countAfter);
    }

    @Test
    public void testFindByUserIds() {
        List<Long> idList = Arrays.asList(new Long[] {3l, 5l, 6l});
        List<User> users = userService.findByUserIds(idList);
        System.out.println(users.toString());
    }

    @Test
    public void testFindOne(){
        User user = userService.findOne(3l);
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
        User update = userService.findByEmail("admin@testwa.com");
        update.setPhone("18600753024");
        update.setNickname("wen");
        update.setSex(DB.Sex.MALE);
        update.setLastLoginIp(123456);
        update.setLastLoginTime(new Date());
        int val = userService.update(update);
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

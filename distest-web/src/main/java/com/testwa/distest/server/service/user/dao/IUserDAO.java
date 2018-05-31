package com.testwa.distest.server.service.user.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.User;

import java.util.List;

/**
 * Created by wen on 19/10/2017.
 */
public interface IUserDAO extends IBaseDAO<User, Long> {
    List<User> findBy(User user);

    User findOne(Long key);

    List<User> findAll(List<Long> keys);

    List<User> findByUsernames(List<String> usernames);

    List<User> findByEmails(List<String> emails);

    List<User> query(User userPart);

    User getByCode(String userCode);

    void active(String userCode);
}

package com.testwa.distest.server.service.user.dao;

import com.testwa.distest.common.dao.IBaseDAO;
import com.testwa.core.entity.User;

import java.util.List;

/**
 * Created by wen on 19/10/2017.
 */
public interface IUserDAO extends IBaseDAO<User, Long> {
    List<User> findBy(User user);

    List<User> findByUsernames(List<String> usernames);

    List<User> findByEmails(List<String> emails);
}

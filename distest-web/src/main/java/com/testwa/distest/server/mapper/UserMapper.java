package com.testwa.distest.server.mapper;

import java.util.List;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper extends BaseMapper<User, Long> {

	List<User> findBy(User user);

    List<User> findList(List<Long> keys);

	List<User> findByUsernameList(List<String> usernameList);

	List<User> findByEmailList(List<String> emailList);

    List<User> query(User userPart);

    User getByCode(String userCode);

    List<User> findByUserCodeList(List<String> userCodeList);

	void updateActiveToTrue(String userCode);

    User getByUsername(String username);

    User getByEmail(String email);

    void resetPwd(@Param("userCode") String userCode, @Param("password") String password);

}
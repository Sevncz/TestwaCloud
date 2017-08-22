package com.testwa.distest.admin.mapper;

import com.testwa.distest.admin.model.UserEntity;

import java.util.List;


public interface UserMapper {
	
	List<UserEntity> getAll();
	
	UserEntity getOne(Long id);

	void insert(UserEntity user);

	void update(UserEntity user);

	void delete(Long id);

}
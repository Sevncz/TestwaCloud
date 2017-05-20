/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.repository;

import java.io.Serializable;

import com.testwa.distest.server.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends CommonRepository<User, Serializable> {

    User findByEmail(String email);

    User findByUsername(String username);

    User findById(String userId);
}

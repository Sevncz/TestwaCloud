/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.mvc.repository;

import java.io.Serializable;

import com.testwa.distest.server.mvc.model.User;

public interface UserRepository extends CommonRepository<User, Serializable> {

    User findByEmail(String email);

    User findByUsername(String username);

    User findById(String userId);
}

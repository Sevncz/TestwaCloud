/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.mvc.repository;

import com.testwa.distest.server.mvc.model.Role;

import java.io.Serializable;

public interface RoleRepository extends CommonRepository<Role, Serializable> {

    Role findById(String id);

    Role findByValue(String value);
}

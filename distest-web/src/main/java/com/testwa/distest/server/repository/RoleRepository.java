/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.repository;

import com.testwa.distest.server.model.permission.Role;

import java.io.Serializable;

public interface RoleRepository extends CommonRepository<Role, Serializable> {

    Role findById(String id);

    Role findByCode(Integer code);

    Role findByValue(String value);
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.repository;

import com.testwa.distest.server.model.ClientDetail;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;

public interface ClientDetailRepository extends CommonRepository<ClientDetail, Serializable> {

    ClientDetail findByClientId(String clientId);

}

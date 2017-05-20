/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.testwa.distest.server.repository;

import java.io.Serializable;

import com.testwa.distest.server.model.OAuth2AuthenticationAccessToken;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OAuth2AccessTokenRepository extends CommonRepository<OAuth2AuthenticationAccessToken, Serializable> {

}

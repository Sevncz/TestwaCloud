/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.repository;

import com.testwa.distest.server.model.OAuth2AuthenticationRefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface OAuth2RefreshTokenRepository extends CommonRepository<OAuth2AuthenticationRefreshToken, String> {

}
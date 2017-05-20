/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.config;

import com.mongodb.DBObject;

import java.net.UnknownHostException;
import java.util.*;

import com.mongodb.MongoClientURI;
import com.testwa.distest.server.model.User;
import com.testwa.distest.server.service.security.ClientDetailService;
import com.testwa.distest.server.service.security.UserAuthConfigService;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.stereotype.Component;

@ReadingConverter
public class CustomMongoDBConvertor implements Converter<DBObject, OAuth2Authentication> {


//    @Autowired
//    private UserAuthConfigService authConfigService;
//    @Autowired
//    private ClientDetailService clientDetailService;
//
//    @Override
//    public OAuth2Authentication convert(DBObject source) {
//        DBObject storedRequest = (DBObject) source.get("storedRequest");
//        OAuth2Request oAuth2Request = new OAuth2Request((Map<String, String>) storedRequest.get("requestParameters"),
//                (String) storedRequest.get("clientId"), null, true, new HashSet((List) storedRequest.get("scope")),
//                null, null, null, null);
//        DBObject userAuthorization = (DBObject) source.get("userAuthentication");
//        if (null != userAuthorization) { //its a user
//            Object prinObj = userAuthorization.get("principal");
//            User u = null;
//            if ((null != prinObj) && prinObj instanceof String) {
//                u = authConfigService.getUser((String) prinObj);
//            } else if (null != prinObj) {
//                DBObject principalDBO = (DBObject) prinObj;
//                String email = (String) principalDBO.get("username");
//                u = authConfigService.getUser(email);
//            }
//            if (null == u) {
//                return null;
//            }
//
//            Authentication userAuthentication = new UserAuthenticationToken(u.getEmail(),
//                    userAuthorization.get("credentials"), authConfigService.getRights(u));
//            OAuth2Authentication authentication = new OAuth2Authentication(oAuth2Request, userAuthentication);
//            return authentication;
//        } else { //its a client
//            String clientId = (String) storedRequest.get("clientId");
//            ClientDetails client = null;
//            if ((null != clientId) && clientId instanceof String) {
//                client = clientDetailService.loadClientByClientId(clientId);
//            }
//            if (null == client) {
//                return null;
//            }
//            Authentication userAuthentication = new ClientAuthenticationToken(client.getClientId(),
//                    null, client.getAuthorities());
//            return new OAuth2Authentication(oAuth2Request, userAuthentication);
//        }
//    }
    @Override
    public OAuth2Authentication convert(DBObject source) {
        DBObject storedRequest = (DBObject)source.get("storedRequest");
        OAuth2Request oAuth2Request = new OAuth2Request((Map<String, String>)storedRequest.get("requestParameters"),
                (String)storedRequest.get("clientId"), null, true, new HashSet((List)storedRequest.get("scope")),
                null, null, null, null);

        DBObject userAuthorization = (DBObject)source.get("userAuthentication");
        Object principal = getPrincipalObject(userAuthorization.get("principal"));
        Authentication userAuthentication = new UsernamePasswordAuthenticationToken(principal,
                userAuthorization.get("credentials"), getAuthorities((List) userAuthorization.get("authorities")));

        return new OAuth2Authentication(oAuth2Request,  userAuthentication );
    }

    private Object getPrincipalObject(Object principal) {
        if(principal instanceof DBObject) {
            DBObject principalDBObject = (DBObject)principal;

            String userName = (String) principalDBObject.get("username");
            String password = "";
            boolean enabled = (boolean) principalDBObject.get("enabled");
            boolean accountNonExpired = (boolean) principalDBObject.get("accountNonExpired");
            boolean credentialsNonExpired = (boolean) principalDBObject.get("credentialsNonExpired");
            boolean accountNonLocked = (boolean) principalDBObject.get("accountNonLocked");

            return new org.springframework.security.core.userdetails.User(userName, password, enabled,
                    accountNonExpired, credentialsNonExpired, accountNonLocked, Collections.EMPTY_LIST);
        } else {
            return principal;
        }
    }

    private Collection<GrantedAuthority> getAuthorities(List<Map<String, String>> authorities) {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>(authorities.size());
        for(Map<String, String> authority : authorities) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority.get("role")));
        }
        return grantedAuthorities;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.service.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.testwa.distest.server.model.ClientDetail;
import com.testwa.distest.server.repository.ClientDetailRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientAlreadyExistsException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.ClientRegistrationService;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Service;

/**
 *
 * @author malike_st
 */
@Service
public class ClientDetailService implements ClientDetailsService, ClientRegistrationService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ClientDetailRepository clientDetailsRepository;
    @Autowired
    private Environment env;

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        ClientDetail clientDetails = clientDetailsRepository.findByClientId(clientId);
        if (null == clientDetails) {
            throw new ClientRegistrationException("Client not found with id '" + clientId + "'");
        }
        return getClientFromMongoDBClientDetails(clientDetails);
    }

    @Override
    public void addClientDetails(ClientDetails cd) throws ClientAlreadyExistsException {
        ClientDetail clientDetails = getMongoDBClientDetailsFromClient(cd);
        clientDetailsRepository.save(clientDetails);
    }

    @Override
    public void updateClientDetails(ClientDetails cd) throws NoSuchClientException {
        ClientDetail clientDetails = clientDetailsRepository.findByClientId(cd.getClientId());
        if (null == clientDetails) {
            throw new NoSuchClientException("Client not found with ID '" + cd.getClientId() + "'");
        }
        clientDetails = getMongoDBClientDetailsFromClient(cd);
        clientDetailsRepository.save(clientDetails);
    }

    @Override
    public void updateClientSecret(String clientId, String secret) throws NoSuchClientException {
        ClientDetail clientDetails = clientDetailsRepository.findByClientId(clientId);
        if (null == clientDetails) {
            throw new NoSuchClientException("Client not found with ID '" + clientId + "'");
        }
        clientDetails.setClientSecret(passwordEncoder.encode(secret));
        clientDetailsRepository.save(clientDetails);
    }

    @Override
    public void removeClientDetails(String clientId) throws NoSuchClientException {
        ClientDetail clientDetails = clientDetailsRepository.findByClientId(clientId);
        if (null == clientDetails) {
            throw new NoSuchClientException("Client not found with ID '" + clientId + "'");
        }
        clientDetailsRepository.delete(clientDetails);
    }

    @Override
    public List listClientDetails() {
        List<ClientDetail> mdbcds = clientDetailsRepository.findAll();
        return getClientsFromMongoDBClientDetails(mdbcds);
    }

    private List<BaseClientDetails> getClientsFromMongoDBClientDetails(List<ClientDetail> clientDetails) {
        List<BaseClientDetails> bcds = new LinkedList<>();
        if (clientDetails != null && !clientDetails.isEmpty()) {
            clientDetails.stream().forEach(mdbcd -> {
                bcds.add(getClientFromMongoDBClientDetails(mdbcd));
            });
        }
        return bcds;
    }

    private BaseClientDetails getClientFromMongoDBClientDetails(ClientDetail clientDetails) {
        BaseClientDetails bc = new BaseClientDetails();
        bc.setAccessTokenValiditySeconds(clientDetails.getAccessTokenValiditySeconds());
        bc.setAuthorizedGrantTypes(clientDetails.getAuthorizedGrantTypes());
        bc.setClientId(clientDetails.getClientId());
        bc.setClientSecret(clientDetails.getClientSecret());
        bc.setRefreshTokenValiditySeconds(clientDetails.getRefreshTokenValiditySeconds());
        bc.setRegisteredRedirectUri(clientDetails.getRegisteredRedirectUri());
        bc.setResourceIds(clientDetails.getResourceIds());
        bc.setScope(clientDetails.getScope());
        return bc;
    }

    private ClientDetail getMongoDBClientDetailsFromClient(ClientDetails cd) {
        ClientDetail clientDetails = new ClientDetail();
        clientDetails.setAccessTokenValiditySeconds(cd.getAccessTokenValiditySeconds());
        clientDetails.setAdditionalInformation(cd.getAdditionalInformation());
        clientDetails.setAuthorizedGrantTypes(cd.getAuthorizedGrantTypes());
        clientDetails.setClientId(cd.getClientId());
        clientDetails.setClientSecret(cd.getClientSecret());
        clientDetails.setRefreshTokenValiditySeconds(cd.getRefreshTokenValiditySeconds());
        clientDetails.setRegisteredRedirectUri(cd.getRegisteredRedirectUri());
        clientDetails.setResourceIds(cd.getResourceIds());
        clientDetails.setScope(cd.getScope());
        clientDetails.setScoped(cd.isScoped());
        clientDetails.setSecretRequired(cd.isSecretRequired());
        clientDetails.setId(cd.getClientId());
        return clientDetails;
    }

    public ClientDetail save(ClientDetail authClient) {
        return clientDetailsRepository.save(authClient);
    }

    public void deleteAll() {
        clientDetailsRepository.deleteAll();
    }

    public void init() {
        ClientDetail authClient = new ClientDetail();
        authClient.setId(RandomStringUtils.randomAlphanumeric(10));
        authClient.setClientId(env.getProperty("app.client.id"));
        authClient.setResourceIds(new HashSet<>(Arrays.asList("rest_api")));
        authClient.setClientSecret(passwordEncoder.encode(env.getProperty("app.client.secret")));
        authClient.setRefreshTokenValiditySeconds(4500);
        authClient.setAccessTokenValiditySeconds(4500);
        authClient.setAuthorities(new HashSet<>(Arrays.asList("trust", "read", "write")));
        authClient.setAuthorizedGrantTypes(new HashSet<>(Arrays.asList("client_credentials", "authorization_code", "implicit", "password", "refresh_token")));
        authClient.setScope(new HashSet<>(Arrays.asList("trust", "read", "write")));
        authClient.setSecretRequired(true);
        save(authClient);
    }
}

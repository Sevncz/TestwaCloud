package com.testwa.distest.server.security.service;


public class JwtAuthenticationResponse {

    private final String access_token;
    private final String refresh_token;
    private final long expires_in;

    public JwtAuthenticationResponse(String access_token, String refresh_token, long expires_in) {
        this.access_token = access_token;
        this.refresh_token = refresh_token;
        this.expires_in = expires_in;
    }

    public String getAccess_token() {
        return access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public long getExpires_in() {
        return expires_in;
    }
}

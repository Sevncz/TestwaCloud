package com.testwa.distest.server.security.service;

public class JwtAuthenticationResponse {

    private final String accessToken;
    private final String refreshToken;
    private final long expiresIn;

    public JwtAuthenticationResponse(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}

package com.testwa.distest.config.security;

import lombok.Data;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class JwtAuthenticationResponse implements Serializable {

    private static final long serialVersionUID = 1250166508152483573L;

    private final String accessToken;
    private final String refreshToken;
    private final long expiresIn;


    public JwtAuthenticationResponse(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }
}

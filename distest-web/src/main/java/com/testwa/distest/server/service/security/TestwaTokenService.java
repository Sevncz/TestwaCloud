package com.testwa.distest.server.service.security;

import com.testwa.distest.server.authorization.Constants;
import io.jsonwebtoken.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

/**
 * Created by wen on 2016/11/5.
 */
@Component
public class TestwaTokenService {

    @Autowired
    private Environment env;

    public void saveToken(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(Constants.TOKENCODE, token);
        cookie.setMaxAge(Integer.parseInt(env.getProperty("token.cookie.maxAge")));
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public String getToken(HttpServletRequest request){

        // 从header中得到token
        String authentication = request.getHeader(Constants.AUTHORIZATION);
        String token = null;
        if(StringUtils.isNotBlank(authentication)){
            String[] param = authentication.trim().split("\\s+");
            if (param.length != 2) {
                return null;
            }
            token = param[1];
        }
        // 从cookie中得到token
        if(StringUtils.isBlank(token)){
            token = getValueFromCookie(request, Constants.TOKENCODE);
        }

        return token;
    }

    public String getValueFromCookie(HttpServletRequest request, String key){
        Cookie[] cookies = request.getCookies();
        String value = "";
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals(key)){
                    value = cookie.getValue();
                }
            }
        }
        return value;
    }

    public Claims parserToken(String token) throws Exception {
        if(StringUtils.isNotBlank(token)){
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(DatatypeConverter.parseBase64Binary(env.getProperty("secret.key")))
                        .parseClaimsJws(token).getBody();
                return claims;
            }catch (final SignatureException e) {
                throw new Exception("Invalid token.");
            }
        }else{
            throw new Exception("token was null.");
        }
    }

    public void deleteToken(HttpServletResponse response) {
        Cookie cookie = new Cookie(Constants.TOKENCODE, "");
        cookie.setMaxAge(10);
        response.addCookie(cookie);
    }


    public String createToken(String userid, String issuer, String subject) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(env.getProperty("secret.key"));
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        JwtBuilder builder = Jwts.builder().setId(userid)
                .setIssuedAt(now)
                .setSubject(subject)
                .setIssuer(issuer)
                .signWith(signatureAlgorithm, signingKey);

        Long ttlSec = env.getProperty("token.ttl", Long.class);
        if (ttlSec != null && ttlSec >= 0) {
            long expMillis = nowMillis + ttlSec * 1000;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

        return builder.compact();
    }

    public String getCaptchaKey(HttpServletRequest request) {
        String captchaCode = request.getHeader(Constants.CAPTCHA);
        if(StringUtils.isBlank(captchaCode)){
            captchaCode = getValueFromCookie(request, Constants.CAPTCHACODE);
        }
        return captchaCode;
    }
}

package io.github.kk01001.oauth2.util;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description JWT工具类
 */
public class JwtUtils {

    /**
     * 生成JWT Token
     */
    public static String generateToken(JwtEncoder encoder, String subject, 
                                     Map<String, Object> claims, long expirationMinutes) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
            .issuer("oauth2-authorization-server")
            .issuedAt(now)
            .expiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES))
            .subject(subject);
        
        claims.forEach(claimsBuilder::claim);
        
        JwtEncoderParameters parameters = JwtEncoderParameters.from(claimsBuilder.build());
        return encoder.encode(parameters).getTokenValue();
    }
} 
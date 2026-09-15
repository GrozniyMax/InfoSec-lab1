package com.grozniy.lab1.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the {@code app.jwt.*} properties from {@code application.yaml}.
 * <p>
 * Enabled via {@code @EnableConfigurationProperties(JwtConfig.class)} on
 * {@link com.grozniy.lab1.config.SecurityConfig} so the property source stays
 * co-located with the security configuration that consumes it.
 */
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {

    private String secret;
    private long expirationMs;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }
}
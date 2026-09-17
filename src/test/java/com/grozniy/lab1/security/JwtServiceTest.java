package com.grozniy.lab1.security;

import com.grozniy.lab1.config.JwtConfig;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET =
            "test-secret-key-that-is-long-enough-for-hs256-signing-1234567890";

    private JwtService service(long expirationMs) {
        JwtConfig config = new JwtConfig();
        config.setSecret(SECRET);
        config.setExpirationMs(expirationMs);
        return new JwtService(config);
    }

    @Test
    void generateAndExtractUsernameRoundtrip() {
        JwtService jwtService = service(3600000);

        String token = jwtService.generateToken("alice");

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
        assertThat(jwtService.isTokenValid(token, "alice")).isTrue();
    }

    @Test
    void tokenSignedWithWrongKeyIsRejected() {
        JwtService jwtService = service(3600000);

        // A token minted with a different secret.
        JwtConfig otherConfig = new JwtConfig();
        otherConfig.setSecret("a-completely-different-32-plus-character-secret-key-000");
        otherConfig.setExpirationMs(3600000);
        JwtService otherService = new JwtService(otherConfig);
        String forged = otherService.generateToken("alice");

        assertThatThrownBy(() -> jwtService.extractUsername(forged))
                .isInstanceOf(JwtException.class);
        assertThat(jwtService.isTokenValid(forged, "alice")).isFalse();
    }

    @Test
    void expiredTokenIsRejected() {
        // Negative expiration makes the token already expired at creation.
        JwtService jwtService = service(-1000);

        String token = jwtService.generateToken("alice");

        assertThatThrownBy(() -> jwtService.extractUsername(token))
                .isInstanceOf(JwtException.class);
        assertThat(jwtService.isTokenValid(token, "alice")).isFalse();
    }

    @Test
    void tokenIsInvalidForDifferentUsername() {
        JwtService jwtService = service(3600000);

        String token = jwtService.generateToken("alice");

        assertThat(jwtService.isTokenValid(token, "bob")).isFalse();
    }
}
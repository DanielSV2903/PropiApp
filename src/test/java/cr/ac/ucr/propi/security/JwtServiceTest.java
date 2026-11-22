package cr.ac.ucr.propi.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "9a7b6c5d4e3f2a1b0c9d8e7f6a5b4c3d2e1f0a9b8c7d6e5f4a3b2c1d0e9f8a7b";
    private final long expirationMs = 3600000; // 1 hora

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, expirationMs);
    }

    @Test
    void testGenerateAndValidateToken() {
        String username = "admin_user";
        List<String> roles = List.of("rol_admin");

        String token = jwtService.generateToken(username, roles);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertEquals(username, jwtService.extractUsername(token));

        List<String> extractedRoles = jwtService.extractRoles(token);
        assertNotNull(extractedRoles);
        assertEquals(1, extractedRoles.size());
        assertEquals("rol_admin", extractedRoles.get(0));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtService.isTokenValid("token.falso.invalido"));
    }
}

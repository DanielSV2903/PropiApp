package cr.ac.ucr.propi.security;

import cr.ac.ucr.propi.exception.InvalidCredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AuthenticationProvider que valida credenciales abriendo una conexión directa
 * a SQL Server con DriverManager y luego consulta los roles del usuario
 * en sys.database_role_members.
 */
@Component
public class SqlServerAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(SqlServerAuthenticationProvider.class);

    private final String jdbcUrl;

    public SqlServerAuthenticationProvider(
            @Value("${spring.datasource.url}") String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        // Intentar conexión directa con las credenciales del usuario
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {

            List<String> roles = fetchUserRoles(conn, username);

            UserPrincipal principal = new UserPrincipal(username, password, roles);
            return new UsernamePasswordAuthenticationToken(principal, password, principal.getAuthorities());

        } catch (SQLException ex) {
            log.warn("Fallo de autenticación SQL Server para usuario '{}': {}", username, ex.getMessage());
            throw new InvalidCredentialsException(
                    "Credenciales de SQL Server inválidas para el usuario: " + username, ex);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Consulta los roles de base de datos del usuario autenticado
     * via sys.database_role_members. Solo retorna roles soportados.
     */
    private List<String> fetchUserRoles(Connection conn, String username) throws SQLException {
        List<String> roles = new ArrayList<>();

        String sql = """
                SELECT r.name AS role_name
                FROM sys.database_role_members drm
                INNER JOIN sys.database_principals r ON drm.role_principal_id = r.principal_id
                INNER JOIN sys.database_principals u ON drm.member_principal_id = u.principal_id
                WHERE u.name = ?
                  AND r.name IN ('rol_admin', 'rol_digitador', 'rol_consulta')
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roles.add(rs.getString("role_name"));
                }
            }
        }

        return roles;
    }
}

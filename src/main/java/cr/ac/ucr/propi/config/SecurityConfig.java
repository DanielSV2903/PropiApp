package cr.ac.ucr.propi.config;

import cr.ac.ucr.propi.security.JwtAuthenticationFilter;
import cr.ac.ucr.propi.security.SqlServerAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

/**
 * Configuración de Spring Security con JWT stateless y RBAC a nivel de endpoints (REQ 003, REQ 008).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SqlServerAuthenticationProvider sqlServerAuthenticationProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          SqlServerAuthenticationProvider sqlServerAuthenticationProvider) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.sqlServerAuthenticationProvider = sqlServerAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(sqlServerAuthenticationProvider));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ── Público ──────────────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()

                // ── Tipos de Impuesto: rol_admin ────────────────────────
                .requestMatchers("/impuestos/**").hasRole("ROL_ADMIN")

                // ── Notas de Crédito / Débito: rol_admin, rol_digitador ──
                .requestMatchers("/notas/**").hasAnyRole("ROL_ADMIN", "ROL_DIGITADOR")

                // ── Clientes: rol_admin, rol_digitador ───────────────────
                .requestMatchers("/clientes/**").hasAnyRole("ROL_ADMIN", "ROL_DIGITADOR")

                // ── Facturas: rol_admin, rol_digitador ───────────────────
                .requestMatchers("/facturas/**").hasAnyRole("ROL_ADMIN", "ROL_DIGITADOR")

                // ── Productos: todos los roles autenticados ──────────────
                .requestMatchers("/productos/**").hasAnyRole("ROL_ADMIN", "ROL_DIGITADOR", "ROL_CONSULTA")

                // ── Reportes generales: rol_admin, rol_consulta ──────────
                .requestMatchers("/reportes/clientes", "/reportes/facturas", "/reportes/auditoria", "/auditoria/**")
                .hasAnyRole("ROL_ADMIN", "ROL_CONSULTA")

                // ── Reporte de seguridad: solo rol_admin ─────────────────
                .requestMatchers("/reportes/seguridad").hasRole("ROL_ADMIN")

                // ── Cualquier otra solicitud: autenticado ────────────────
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

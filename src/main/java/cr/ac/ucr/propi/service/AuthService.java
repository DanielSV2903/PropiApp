package cr.ac.ucr.propi.service;

import cr.ac.ucr.propi.model.dto.LoginResponseDto;
import cr.ac.ucr.propi.repository.AuthJdbcRepository;
import cr.ac.ucr.propi.security.JwtService;
import cr.ac.ucr.propi.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de autenticación. Invoca sp_registrar_ingreso y sp_registrar_salida.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthJdbcRepository authJdbcRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       AuthJdbcRepository authJdbcRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.authJdbcRepository = authJdbcRepository;
    }

    public LoginResponseDto login(String username, String password, String terminal, String ip) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        List<String> roles = principal.getRoleNames();

        Long codAcceso = authJdbcRepository.registrarIngreso(username, terminal, ip);

        String token = jwtService.generateToken(username, roles);
        long expirationMs = jwtService.getExpirationMs();

        log.info("Login exitoso para usuario: {}, codAcceso: {}", username, codAcceso);

        return new LoginResponseDto(token, username, roles, expirationMs, codAcceso);
    }

    public void logout(Long codAcceso) {
        if (codAcceso != null) {
            authJdbcRepository.registrarSalida(codAcceso);
            log.info("Logout exitoso para codAcceso: {}", codAcceso);
        } else {
            log.warn("Se solicitó logout sin codAcceso");
        }
    }
}

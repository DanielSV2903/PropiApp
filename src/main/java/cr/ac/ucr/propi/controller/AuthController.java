package cr.ac.ucr.propi.controller;

import cr.ac.ucr.propi.model.dto.LoginRequestDto;
import cr.ac.ucr.propi.model.dto.LoginResponseDto;
import cr.ac.ucr.propi.model.dto.LogoutRequestDto;
import cr.ac.ucr.propi.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador de Autenticación (PropiDB v2.1).
 * Implementa login con sp_registrar_ingreso y logout con sp_registrar_salida.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody @Valid LoginRequestDto loginRequest,
            HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }

        String terminal = request.getHeader("X-Client-Terminal");
        if (terminal == null || terminal.isBlank()) {
            terminal = request.getHeader("User-Agent");
        }

        LoginResponseDto response = authService.login(
                loginRequest.username(),
                loginRequest.password(),
                terminal,
                ip
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestBody(required = false) LogoutRequestDto logoutRequest) {

        Long codAcceso = logoutRequest != null ? logoutRequest.codAcceso() : null;
        authService.logout(codAcceso);

        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }
}

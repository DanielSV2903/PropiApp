package cr.ac.ucr.propi.controller;

import cr.ac.ucr.propi.service.BitacoraService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {

    private final BitacoraService bitacoraService;

    public AuditoriaController(BitacoraService bitacoraService) {
        this.bitacoraService = bitacoraService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listarBitacora(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        List<Map<String, Object>> registros = bitacoraService.listarBitacora(desde, hasta);
        return ResponseEntity.ok(registros);
    }
}

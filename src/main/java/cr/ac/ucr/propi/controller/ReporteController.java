package cr.ac.ucr.propi.controller;

import cr.ac.ucr.propi.service.ReporteService;
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
@RequestMapping("/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/clientes")
    public ResponseEntity<List<Map<String, Object>>> reporteClientes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String estado) {

        List<Map<String, Object>> resultado = reporteService.consultarClientesResumido(desde, hasta, estado);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/facturas")
    public ResponseEntity<List<Map<String, Object>>> reporteFacturas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        List<Map<String, Object>> resultado = reporteService.consultarFacturas(desde, hasta);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/auditoria")
    public ResponseEntity<List<Map<String, Object>>> reporteAuditoria(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String usuario) {

        List<Map<String, Object>> resultado = reporteService.consultarPistasAuditoria(desde, hasta, usuario);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/seguridad")
    public ResponseEntity<List<Map<String, Object>>> reporteSeguridad(
            @RequestParam(required = false) String usuario) {

        List<Map<String, Object>> resultado = reporteService.consultarConfiguracionSeguridad(usuario);
        return ResponseEntity.ok(resultado);
    }
}

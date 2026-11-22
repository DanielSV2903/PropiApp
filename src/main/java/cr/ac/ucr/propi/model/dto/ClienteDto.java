package cr.ac.ucr.propi.model.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * DTO para Cliente (PropiDB v2.1) con desglose territorial y laboral.
 * Cumple con REQ 006 (los datos sensibles se cifran/descifran mediante SP en SQL Server).
 */
public class ClienteDto {

    private Long codCliente;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El tipo de identificación es obligatorio")
    private String tipoIdentificacion;

    @NotBlank(message = "El número de identificación es obligatorio")
    private String numIdentificacion;

    @NotBlank(message = "La provincia es obligatoria")
    private String provincia;

    @NotBlank(message = "El cantón es obligatorio")
    private String canton;

    @NotBlank(message = "El distrito es obligatorio")
    private String distrito;

    private String barrio;
    private String profesionOficio;

    @NotBlank(message = "La actividad económica es obligatoria")
    private String actividadEconomica;

    @NotBlank(message = "El correo electrónico es obligatorio")
    private String correo;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    private String estado;
    private LocalDateTime fecRegistro;
    private LocalDateTime fecModificacion;

    public ClienteDto() {
    }

    public ClienteDto(Long codCliente, String nombre, String tipoIdentificacion, String numIdentificacion,
                      String provincia, String canton, String distrito, String barrio, String profesionOficio,
                      String actividadEconomica, String correo, String telefono, String estado,
                      LocalDateTime fecRegistro, LocalDateTime fecModificacion) {
        this.codCliente = codCliente;
        this.nombre = nombre;
        this.tipoIdentificacion = tipoIdentificacion;
        this.numIdentificacion = numIdentificacion;
        this.provincia = provincia;
        this.canton = canton;
        this.distrito = distrito;
        this.barrio = barrio;
        this.profesionOficio = profesionOficio;
        this.actividadEconomica = actividadEconomica;
        this.correo = correo;
        this.telefono = telefono;
        this.estado = estado;
        this.fecRegistro = fecRegistro;
        this.fecModificacion = fecModificacion;
    }

    public Long getCodCliente() {
        return codCliente;
    }

    public void setCodCliente(Long codCliente) {
        this.codCliente = codCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoIdentificacion() {
        return tipoIdentificacion;
    }

    public void setTipoIdentificacion(String tipoIdentificacion) {
        this.tipoIdentificacion = tipoIdentificacion;
    }

    public String getNumIdentificacion() {
        return numIdentificacion;
    }

    public void setNumIdentificacion(String numIdentificacion) {
        this.numIdentificacion = numIdentificacion;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCanton() {
        return canton;
    }

    public void setCanton(String canton) {
        this.canton = canton;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getBarrio() {
        return barrio;
    }

    public void setBarrio(String barrio) {
        this.barrio = barrio;
    }

    public String getProfesionOficio() {
        return profesionOficio;
    }

    public void setProfesionOficio(String profesionOficio) {
        this.profesionOficio = profesionOficio;
    }

    public String getActividadEconomica() {
        return actividadEconomica;
    }

    public void setActividadEconomica(String actividadEconomica) {
        this.actividadEconomica = actividadEconomica;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFecRegistro() {
        return fecRegistro;
    }

    public void setFecRegistro(LocalDateTime fecRegistro) {
        this.fecRegistro = fecRegistro;
    }

    public LocalDateTime getFecModificacion() {
        return fecModificacion;
    }

    public void setFecModificacion(LocalDateTime fecModificacion) {
        this.fecModificacion = fecModificacion;
    }
}

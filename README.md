# Sistema Propi (Proptech) — Backend API (PropiDB v2.1)

**Materia:** IF0010 Bases de Datos II — Universidad de Costa Rica (UCR)  
**Stack:** Java 21 (LTS) | Spring Boot 3.3.5 | Spring JDBC Puro (`SimpleJdbcCall` / `JdbcTemplate` — Cero JPA/Hibernate) | Microsoft SQL Server 2022+ | Spring Security 6.x + JJWT 0.12.5

---

## 1. Características Técnicas de la Versión 2.1

* **Cero SQL Embebido en Repositorios:** Todas las operaciones de consulta, inserción, actualización y borrado se delegan estrictamente a **Procedimientos Almacenados** mediante `SimpleJdbcCall` (cumplimiento estricto de **REQ 002**, **REQ 005**, **REQ 006** y **REQ 007**).
* **Propagación del Contexto de Auditoría (REQ 005):** Inyección obligatoria de `cod_usuario` y `cod_sesion` en la sesión de base de datos (`sys.sp_set_session_context`) antes de ejecutar cualquier procedimiento de escritura (`crear`, `actualizar`, `eliminar`), asegurando que los triggers de `tbl_bitacora_cambios` capturen al usuario web real y no al usuario técnico del pool.
* **Facturación con Table-Valued Parameter (TVP):** Uso de `SQLServerDataTable` del driver `mssql-jdbc` para enviar el conjunto de líneas de detalle al tipo tabla `dbo.tt_factura_detalle` en una sola llamada atómica a `sp_factura_crear`.
* **Cifrado Celular de Clientes (REQ 006):** La apertura y cierre de `SymKeyClientes` desencriptada por `CertClientes` se maneja dentro de los propios procedimientos almacenados (`sp_cliente_crear`, `sp_cliente_actualizar`, `sp_cliente_listar`), protegiendo `num_identificacion`, `correo` y `telefono` en reposo.
* **Control de Accesos RBAC y Manejo de Errores (REQ 008):** `GlobalExceptionHandler` captura errores nativos de SQL Server:
  * Error `229` (*Permission denied on object*) -> `403 Forbidden` (demostración en vivo de los `DENY` del rol).
  * Errores de negocio personalizados (`50012`, `50040`, `50051`) y violaciones de restricciones `CHECK` / `RAISERROR` de triggers -> `400 Bad Request`.

---

## 2. Preparación de la Base de Datos

1. Abra **SQL Server Management Studio (SSMS)** o **Azure Data Studio**.
2. Abra y ejecute el script unificado: [`src/main/resources/schema.sql`](src/main/resources/schema.sql).
3. El script creará:
   * La base de datos `PropiDB` con *Snapshot Isolation*.
   * La Master Key, el Certificado `CertClientes` y la Llave Simétrica `SymKeyClientes` (AES-256).
   * Las tablas: `tbl_bitacora_acceso`, `tbl_bitacora_cambios`, `tbl_tipo_impuesto`, `tbl_cliente`, `tbl_producto`, `tbl_factura`, `tbl_factura_detalle`, `tbl_nota_credito_debito`.
   * El tipo tabla `dbo.tt_factura_detalle`.
   * Los 18 triggers de auditoría transaccional con JSON.
   * Todos los procedimientos almacenados CRUD y de reportes.
   * Los roles de base de datos (`rol_admin`, `rol_digitador`, `rol_consulta`) con sus `GRANT` y `DENY`.
   * Los usuarios de prueba:
     * **Admin:** `usr_admin` / `AdminPropi2026!`
     * **Digitador:** `usr_digitador` / `DigiPropi2026!`
     * **Consulta:** `usr_consulta` / `ConsPropi2026!`

---

## 3. Compilación y Ejecución

```powershell
# Compilar el proyecto con Java 21
.\mvnw.cmd compile

# Iniciar la aplicación Spring Boot
.\mvnw.cmd spring-boot:run
```

La API quedará escuchando en: `http://localhost:8080/api/v1`

---

## 4. Matriz de Endpoints REST

| Método | Endpoint | Roles Permitidos | Procedimiento Almacenado Invocado |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/login` | Público | `sp_registrar_ingreso` (retorna `codAcceso` y JWT) |
| `POST` | `/api/v1/auth/logout` | Autenticado | `sp_registrar_salida` (recibe `codAcceso`) |
| `GET` | `/api/v1/impuestos` | `rol_admin` | `sp_tipo_impuesto_listar` |
| `POST` | `/api/v1/impuestos` | `rol_admin` | `sp_tipo_impuesto_crear` |
| `PUT` | `/api/v1/impuestos/{id}` | `rol_admin` | `sp_tipo_impuesto_actualizar` |
| `GET` | `/api/v1/clientes` | `rol_admin`, `rol_digitador` | `sp_cliente_listar` (con parámetro `soloActivos`) |
| `POST` | `/api/v1/clientes` | `rol_admin`, `rol_digitador` | `sp_cliente_crear` (cifra datos sensibles) |
| `PUT` | `/api/v1/clientes/{id}` | `rol_admin`, `rol_digitador` | `sp_cliente_actualizar` |
| `DELETE`| `/api/v1/clientes/{id}` | `rol_admin`, `rol_digitador` | `sp_cliente_eliminar` |
| `GET` | `/api/v1/productos` | Todos autenticados | `sp_producto_listar` |
| `POST` | `/api/v1/productos` | `rol_admin`, `rol_digitador` | `sp_producto_crear` |
| `PUT` | `/api/v1/productos/{id}` | `rol_admin`, `rol_digitador` | `sp_producto_actualizar` |
| `GET` | `/api/v1/facturas` | `rol_admin`, `rol_digitador` | `sp_consultar_facturas` |
| `GET` | `/api/v1/facturas/{id}` | `rol_admin`, `rol_digitador` | `sp_factura_obtener_detalle` |
| `POST` | `/api/v1/facturas` | `rol_admin`, `rol_digitador` | `sp_factura_crear` (con TVP `dbo.tt_factura_detalle`) |
| `GET` | `/api/v1/notas` | `rol_admin`, `rol_digitador` | `sp_nota_listar` |
| `POST` | `/api/v1/notas` | `rol_admin`, `rol_digitador` | `sp_nota_crear` |
| `GET` | `/api/v1/reportes/clientes` | `rol_admin`, `rol_consulta` | `sp_consultar_clientes_resumido` |
| `GET` | `/api/v1/reportes/facturas` | `rol_admin`, `rol_consulta` | `sp_consultar_facturas` |
| `GET` | `/api/v1/reportes/auditoria` | `rol_admin`, `rol_consulta` | `sp_consultar_pistas_auditoria` |
| `GET` | `/api/v1/reportes/seguridad` | `rol_admin` | `sp_consultar_configuracion_seguridad` |
| `GET` | `/api/v1/auditoria` | `rol_admin`, `rol_consulta` | `sp_consultar_pistas_auditoria` |

Se incluye el archivo de pruebas [`propi-api.http`](propi-api.http) para probar el flujo completo en VS Code o IntelliJ IDEA.

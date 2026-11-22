-- ============================================================================
-- SISTEMA PROPI (PROPTECH) - SCRIPT UNIFICADO DE BASE DE DATOS (V2.1)
-- IF0010 Bases de Datos II (UCR) - Recinto de Paraíso
-- Motor: Microsoft SQL Server 2022+
-- Cubre: REQ 001, REQ 002, REQ 003, REQ 004, REQ 005, REQ 006, REQ 007, REQ 008
-- ============================================================================

USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = N'PropiDB')
BEGIN
    ALTER DATABASE PropiDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE PropiDB;
END
GO

CREATE DATABASE PropiDB;
GO

ALTER DATABASE PropiDB SET ALLOW_SNAPSHOT_ISOLATION ON;
GO

USE PropiDB;
GO

-- ────────────────────────────────────────────────────────────────────────────
-- 1. INFRAESTRUCTURA DE CIFRADO COLUMNAR (REQ 006)
-- ────────────────────────────────────────────────────────────────────────────
CREATE MASTER KEY ENCRYPTION BY PASSWORD = 'MasterKeyPassword2026!';
GO

CREATE CERTIFICATE CertClientes
WITH SUBJECT = 'Certificado de Seguridad para Datos Sensibles de Clientes';
GO

CREATE SYMMETRIC KEY SymKeyClientes
WITH ALGORITHM = AES_256
ENCRYPTION BY CERTIFICATE CertClientes;
GO

-- ────────────────────────────────────────────────────────────────────────────
-- 2. TABLAS DEL SISTEMA CON RESTRICCIONES CHECK Y CATÁLOGOS
-- ────────────────────────────────────────────────────────────────────────────

-- 2.1. Bitácora de Accesos (REQ 003)
CREATE TABLE tbl_bitacora_acceso (
    cod_acceso    BIGINT IDENTITY(1,1) PRIMARY KEY,
    nom_usuario   VARCHAR(100) NOT NULL,
    nom_terminal  VARCHAR(255) NOT NULL,
    dir_ip        VARCHAR(50)  NOT NULL,
    fec_ingreso   DATETIME     NOT NULL DEFAULT GETDATE(),
    fec_salida    DATETIME     NULL,
    fec_registro  DATETIME     NOT NULL DEFAULT GETDATE(),

    CONSTRAINT chk_acceso_fec_salida CHECK (fec_salida IS NULL OR fec_salida >= fec_ingreso),
    CONSTRAINT chk_acceso_nom_usuario CHECK (LEN(TRIM(nom_usuario)) > 0),
    CONSTRAINT chk_acceso_dir_ip CHECK (LEN(TRIM(dir_ip)) >= 3)
);
GO

-- 2.2. Bitácora de Cambios / Pistas de Auditoría (REQ 005)
CREATE TABLE tbl_bitacora_cambios (
    cod_bitacora       BIGINT IDENTITY(1,1) PRIMARY KEY,
    fec_registro       DATETIME      NOT NULL DEFAULT GETDATE(),
    cod_guid           VARCHAR(255)  NULL DEFAULT NEWID(),
    ind_accion         CHAR(1)       NOT NULL, -- I: Inserción, D: Borrado, U: Modificación
    cod_usuario        CHAR(128)     NOT NULL,
    cod_sesion_usuario INT           NULL,
    cod_tabla          VARCHAR(255)  NOT NULL,
    cod_llave          VARCHAR(2000) NULL,
    des_datos          VARCHAR(MAX)  NULL,

    CONSTRAINT chk_cambios_ind_accion CHECK (ind_accion IN ('I', 'U', 'D')),
    CONSTRAINT chk_cambios_cod_tabla CHECK (LEN(TRIM(cod_tabla)) > 0),
    CONSTRAINT chk_cambios_fec_registro CHECK (fec_registro <= DATEADD(MINUTE, 5, GETDATE()))
);
GO

-- 2.3. Tipos de Impuesto (Descripción del caso)
CREATE TABLE tbl_tipo_impuesto (
    cod_impuesto BIGINT IDENTITY(1,1) PRIMARY KEY,
    codigo       VARCHAR(20)   NOT NULL UNIQUE,
    descripcion  VARCHAR(150)  NOT NULL,
    porcentaje   DECIMAL(5,2)  NOT NULL,
    tarifa       VARCHAR(50)   NOT NULL,
    estado       VARCHAR(20)   NOT NULL DEFAULT 'ACTIVO',

    CONSTRAINT chk_impuesto_porcentaje CHECK (porcentaje >= 0.00 AND porcentaje <= 100.00),
    CONSTRAINT chk_impuesto_estado CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    CONSTRAINT chk_impuesto_codigo CHECK (LEN(TRIM(codigo)) >= 1)
);
GO

-- 2.4. Clientes con Cifrado Celular (REQ 006 + Descripción del caso)
CREATE TABLE tbl_cliente (
    cod_cliente         BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre              VARCHAR(150)   NOT NULL,
    tipo_identificacion VARCHAR(20)    NOT NULL,
    num_identificacion  VARBINARY(256) NOT NULL, -- Cifrado (REQ 006)
    provincia           VARCHAR(60)    NOT NULL,
    canton              VARCHAR(60)    NOT NULL,
    distrito            VARCHAR(60)    NOT NULL,
    barrio              VARCHAR(100)   NULL,
    profesion_oficio    VARCHAR(100)   NULL,
    actividad_economica VARCHAR(150)   NOT NULL,
    correo              VARBINARY(256) NOT NULL, -- Cifrado (REQ 006)
    telefono            VARBINARY(256) NOT NULL, -- Cifrado (REQ 006)
    fec_registro        DATETIME       NOT NULL DEFAULT GETDATE(),
    fec_modificacion    DATETIME       NULL,
    estado              VARCHAR(20)    NOT NULL DEFAULT 'ACTIVO',

    CONSTRAINT chk_cliente_tipo_id CHECK (tipo_identificacion IN ('CED_FISICA', 'CED_JURIDICA', 'DIMEX', 'PASAPORTE')),
    CONSTRAINT chk_cliente_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'SUSPENDIDO', 'BLOQUEADO')),
    CONSTRAINT chk_cliente_nombre CHECK (LEN(TRIM(nombre)) >= 3)
);
GO

-- 2.5. Catálogo de Productos / Servicios / Inmuebles (Descripción del caso)
CREATE TABLE tbl_producto (
    cod_producto  BIGINT IDENTITY(1,1) PRIMARY KEY,
    codigo_cabys  VARCHAR(20)   NOT NULL,
    descripcion   VARCHAR(250)  NOT NULL,
    unidad_medida VARCHAR(30)   NOT NULL DEFAULT 'Unidad',
    precio        DECIMAL(18,2) NOT NULL,
    cod_impuesto  BIGINT        NOT NULL REFERENCES tbl_tipo_impuesto(cod_impuesto),
    estado        VARCHAR(20)   NOT NULL DEFAULT 'ACTIVO',

    CONSTRAINT chk_producto_precio CHECK (precio > 0.00),
    CONSTRAINT chk_producto_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'AGOTADO')),
    CONSTRAINT chk_producto_cabys CHECK (LEN(TRIM(codigo_cabys)) >= 5)
);
GO

-- 2.6. Cabecera de Facturación (Descripción del caso)
CREATE TABLE tbl_factura (
    cod_factura     BIGINT IDENTITY(1,1) PRIMARY KEY,
    cod_cliente     BIGINT        NOT NULL REFERENCES tbl_cliente(cod_cliente),
    fec_factura     DATETIME      NOT NULL DEFAULT GETDATE(),
    condicion_venta VARCHAR(20)   NOT NULL DEFAULT 'CONTADO',
    medio_pago      VARCHAR(30)   NOT NULL DEFAULT 'TRANSFERENCIA',
    moneda          CHAR(3)       NOT NULL DEFAULT 'CRC',
    tipo_cambio     DECIMAL(10,2) NOT NULL DEFAULT 1.00,
    observaciones   VARCHAR(500)  NULL,
    subtotal        DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_impuestos DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    monto_total     DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    estado          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVA',

    CONSTRAINT chk_factura_condicion CHECK (condicion_venta IN ('CREDITO', 'CONTADO')),
    CONSTRAINT chk_factura_medio_pago CHECK (medio_pago IN ('EFECTIVO', 'TARJETA', 'CHEQUE', 'TRANSFERENCIA', 'SINPE_MOVIL', 'OTROS')),
    CONSTRAINT chk_factura_moneda CHECK (moneda IN ('CRC', 'USD')),
    CONSTRAINT chk_factura_estado CHECK (estado IN ('ACTIVA', 'ANULADA')),
    CONSTRAINT chk_factura_tipo_cambio CHECK (tipo_cambio > 0.00)
);
GO

-- 2.7. Detalle de Factura
CREATE TABLE tbl_factura_detalle (
    cod_detalle     BIGINT IDENTITY(1,1) PRIMARY KEY,
    cod_factura     BIGINT        NOT NULL REFERENCES tbl_factura(cod_factura),
    cod_producto    BIGINT        NOT NULL REFERENCES tbl_producto(cod_producto),
    cantidad        DECIMAL(10,2) NOT NULL,
    precio_unitario DECIMAL(18,2) NOT NULL,
    monto_impuesto  DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    monto_linea     DECIMAL(18,2) NOT NULL,

    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_precio CHECK (precio_unitario > 0.00),
    CONSTRAINT chk_detalle_monto CHECK (monto_linea >= 0.00)
);
GO

-- 2.8. Notas de Crédito / Débito (Descripción del caso)
CREATE TABLE tbl_nota_credito_debito (
    cod_nota     BIGINT IDENTITY(1,1) PRIMARY KEY,
    codigo       VARCHAR(30)   NOT NULL UNIQUE,
    fec_nota     DATETIME      NOT NULL DEFAULT GETDATE(),
    cod_factura  BIGINT        NOT NULL REFERENCES tbl_factura(cod_factura),
    tipo         VARCHAR(10)   NOT NULL, -- CREDITO, DEBITO
    motivo       VARCHAR(300)  NOT NULL,
    monto_ajuste DECIMAL(18,2) NOT NULL,

    CONSTRAINT chk_nota_tipo CHECK (tipo IN ('CREDITO', 'DEBITO')),
    CONSTRAINT chk_nota_monto CHECK (monto_ajuste > 0.00),
    CONSTRAINT chk_nota_motivo CHECK (LEN(TRIM(motivo)) >= 5)
);
GO

-- Tipo tabla para líneas de factura en una transacción
CREATE TYPE dbo.tt_factura_detalle AS TABLE (
    cod_producto BIGINT        NOT NULL PRIMARY KEY,
    cantidad     DECIMAL(10,2) NOT NULL CHECK (cantidad > 0)
);
GO

-- Datos semilla iniciales
INSERT INTO tbl_tipo_impuesto (codigo, descripcion, porcentaje, tarifa, estado) VALUES
('01', 'Impuesto al Valor Agregado General', 13.00, 'Tarifa General 13%', 'ACTIVO'),
('02', 'Impuesto Reducido Bienes Raices', 4.00, 'Tarifa Reducida 4%', 'ACTIVO'),
('08', 'Exento de IVA', 0.00, 'Tarifa Exenta 0%', 'ACTIVO');

INSERT INTO tbl_producto (codigo_cabys, descripcion, unidad_medida, precio, cod_impuesto, estado) VALUES
('7211100000100', 'Apartamento Torre Escazu (85m2)', 'Unidad', 175000.00, 1, 'ACTIVO'),
('7211100000200', 'Casa Condominio Santa Ana (3 Hab)', 'Unidad', 245000.00, 1, 'ACTIVO'),
('7212100000300', 'Lote Comercial San Pedro (450m2)', 'Metro Cuadrado', 310000.00, 2, 'ACTIVO');
GO

-- ────────────────────────────────────────────────────────────────────────────
-- 3. TRIGGERS DE AUDITORÍA (3 POR TABLA: INSERT, UPDATE, DELETE - REQ 005)
-- ────────────────────────────────────────────────────────────────────────────

-- 3.1. tbl_bitacora_acceso
CREATE OR ALTER TRIGGER trg_acceso_insert ON tbl_bitacora_acceso AFTER INSERT AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM inserted WHERE fec_ingreso > DATEADD(MINUTE, 5, GETDATE()))
    BEGIN
        RAISERROR('Seguridad: Fecha de ingreso futura no permitida.', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO
CREATE OR ALTER TRIGGER trg_acceso_update ON tbl_bitacora_acceso AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF UPDATE(nom_usuario) OR UPDATE(nom_terminal) OR UPDATE(dir_ip) OR UPDATE(fec_ingreso)
    BEGIN
        RAISERROR('Seguridad: Solo se permite actualizar fec_salida.', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO
CREATE OR ALTER TRIGGER trg_acceso_delete ON tbl_bitacora_acceso INSTEAD OF DELETE AS
BEGIN
    SET NOCOUNT ON;
    RAISERROR('Seguridad: Bitacora de accesos es inmutable.', 16, 1);
    ROLLBACK TRANSACTION;
END;
GO

-- 3.2. tbl_bitacora_cambios (Inmutabilidad y trazabilidad)
CREATE OR ALTER TRIGGER trg_bitacora_insert ON tbl_bitacora_cambios AFTER INSERT AS
BEGIN
    SET NOCOUNT ON;
    UPDATE b SET cod_guid = ISNULL(b.cod_guid, NEWID())
    FROM tbl_bitacora_cambios b INNER JOIN inserted i ON b.cod_bitacora = i.cod_bitacora
    WHERE b.cod_guid IS NULL;
END;
GO
CREATE OR ALTER TRIGGER trg_bitacora_update ON tbl_bitacora_cambios INSTEAD OF UPDATE AS
BEGIN
    SET NOCOUNT ON;
    RAISERROR('Auditoria: Los registros de bitacora no pueden modificarse.', 16, 1);
    ROLLBACK TRANSACTION;
END;
GO
CREATE OR ALTER TRIGGER trg_bitacora_delete ON tbl_bitacora_cambios INSTEAD OF DELETE AS
BEGIN
    SET NOCOUNT ON;
    RAISERROR('Auditoria: Los registros de bitacora no pueden eliminarse.', 16, 1);
    ROLLBACK TRANSACTION;
END;
GO

-- 3.3. tbl_tipo_impuesto (I, U, D con ANTES y DESPUES)
CREATE OR ALTER TRIGGER trg_impuesto_insert ON tbl_tipo_impuesto AFTER INSERT AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @usr CHAR(128) = ISNULL(CAST(SESSION_CONTEXT(N'cod_usuario') AS CHAR(128)), SUSER_SNAME());
    DECLARE @ses INT = CAST(SESSION_CONTEXT(N'cod_sesion') AS INT);
    INSERT INTO tbl_bitacora_cambios (fec_registro, cod_guid, ind_accion, cod_usuario, cod_sesion_usuario, cod_tabla, cod_llave, des_datos)
    SELECT GETDATE(), NEWID(), 'I', @usr, @ses, 'tbl_tipo_impuesto', CAST(i.cod_impuesto AS VARCHAR(2000)),
           (SELECT i.* FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
    FROM inserted i;
END;
GO
CREATE OR ALTER TRIGGER trg_impuesto_update ON tbl_tipo_impuesto AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @usr CHAR(128) = ISNULL(CAST(SESSION_CONTEXT(N'cod_usuario') AS CHAR(128)), SUSER_SNAME());
    DECLARE @ses INT = CAST(SESSION_CONTEXT(N'cod_sesion') AS INT);
    INSERT INTO tbl_bitacora_cambios (fec_registro, cod_guid, ind_accion, cod_usuario, cod_sesion_usuario, cod_tabla, cod_llave, des_datos)
    SELECT GETDATE(), NEWID(), 'U', @usr, @ses, 'tbl_tipo_impuesto', CAST(i.cod_impuesto AS VARCHAR(2000)),
           CONCAT('ANTES: ', (SELECT d.* FOR JSON PATH, WITHOUT_ARRAY_WRAPPER), ' DESPUES: ', (SELECT i.* FOR JSON PATH, WITHOUT_ARRAY_WRAPPER))
    FROM inserted i INNER JOIN deleted d ON i.cod_impuesto = d.cod_impuesto;
END;
GO
CREATE OR ALTER TRIGGER trg_impuesto_delete ON tbl_tipo_impuesto AFTER DELETE AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @usr CHAR(128) = ISNULL(CAST(SESSION_CONTEXT(N'cod_usuario') AS CHAR(128)), SUSER_SNAME());
    DECLARE @ses INT = CAST(SESSION_CONTEXT(N'cod_sesion') AS INT);
    INSERT INTO tbl_bitacora_cambios (fec_registro, cod_guid, ind_accion, cod_usuario, cod_sesion_usuario, cod_tabla, cod_llave, des_datos)
    SELECT GETDATE(), NEWID(), 'D', @usr, @ses, 'tbl_tipo_impuesto', CAST(d.cod_impuesto AS VARCHAR(2000)),
           (SELECT d.* FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
    FROM deleted d;
END;
GO

-- 3.4. tbl_cliente (I, U, D con ANTES y DESPUES en JSON sin exponer binario crudo)
CREATE OR ALTER TRIGGER trg_cliente_insert ON tbl_cliente AFTER INSERT AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @usr CHAR(128) = ISNULL(CAST(SESSION_CONTEXT(N'cod_usuario') AS CHAR(128)), SUSER_SNAME());
    DECLARE @ses INT = CAST(SESSION_CONTEXT(N'cod_sesion') AS INT);
    INSERT INTO tbl_bitacora_cambios (fec_registro, cod_guid, ind_accion, cod_usuario, cod_sesion_usuario, cod_tabla, cod_llave, des_datos)
    SELECT GETDATE(), NEWID(), 'I', @usr, @ses, 'tbl_cliente', CAST(i.cod_cliente AS VARCHAR(2000)),
           (SELECT i.cod_cliente, i.nombre, i.tipo_identificacion, i.provincia, i.canton, i.distrito, i.barrio, i.profesion_oficio, i.actividad_economica, i.estado FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
    FROM inserted i;
END;
GO
CREATE OR ALTER TRIGGER trg_cliente_update ON tbl_cliente AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @usr CHAR(128) = ISNULL(CAST(SESSION_CONTEXT(N'cod_usuario') AS CHAR(128)), SUSER_SNAME());
    DECLARE @ses INT = CAST(SESSION_CONTEXT(N'cod_sesion') AS INT);
    INSERT INTO tbl_bitacora_cambios (fec_registro, cod_guid, ind_accion, cod_usuario, cod_sesion_usuario, cod_tabla, cod_llave, des_datos)
    SELECT GETDATE(), NEWID(), 'U', @usr, @ses, 'tbl_cliente', CAST(i.cod_cliente AS VARCHAR(2000)),
           CONCAT(
               'ANTES: ', (SELECT d.cod_cliente, d.nombre, d.tipo_identificacion, d.provincia, d.canton, d.distrito, d.barrio, d.profesion_oficio, d.actividad_economica, d.estado FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               ' DESPUES: ', (SELECT i.cod_cliente, i.nombre, i.tipo_identificacion, i.provincia, i.canton, i.distrito, i.barrio, i.profesion_oficio, i.actividad_economica, i.estado FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
           )
    FROM inserted i INNER JOIN deleted d ON i.cod_cliente = d.cod_cliente;
END;
GO
CREATE OR ALTER TRIGGER trg_cliente_delete ON tbl_cliente INSTEAD OF DELETE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM tbl_factura f INNER JOIN deleted d ON f.cod_cliente = d.cod_cliente)
    BEGIN
        RAISERROR('Regla de Negocio: No se puede eliminar un cliente con facturas registradas.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END
    DECLARE @usr CHAR(128) = ISNULL(CAST(SESSION_CONTEXT(N'cod_usuario') AS CHAR(128)), SUSER_SNAME());
    DECLARE @ses INT = CAST(SESSION_CONTEXT(N'cod_sesion') AS INT);
    INSERT INTO tbl_bitacora_cambios (fec_registro, cod_guid, ind_accion, cod_usuario, cod_sesion_usuario, cod_tabla, cod_llave, des_datos)
    SELECT GETDATE(), NEWID(), 'D', @usr, @ses, 'tbl_cliente', CAST(d.cod_cliente AS VARCHAR(2000)),
           (SELECT d.cod_cliente, d.nombre, d.tipo_identificacion, d.estado FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
    FROM deleted d;
    DELETE FROM tbl_cliente WHERE cod_cliente IN (SELECT cod_cliente FROM deleted);
END;
GO

-- 3.5. tbl_producto (I, U, D)
CREATE OR ALTER TRIGGER trg_producto_insert ON tbl_producto AFTER INSERT AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @usr CHAR(128) = ISNULL(CAST(SESSION_CONTEXT(N'cod_usuario') AS CHAR(128)), SUSER_SNAME());
    DECLARE @ses INT = CAST(SESSION_CONTEXT(N'cod_sesion') AS INT);
    INSERT INTO tbl_bitacora_cambios (fec_registro, cod_guid, ind_accion, cod_usuario, cod_sesion_usuario, cod_tabla, cod_llave, des_datos)
    SELECT GETDATE(), NEWID(), 'I', @usr, @ses, 'tbl_producto', CAST(i.cod_producto AS VARCHAR(2000)),
           (SELECT i.* FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
    FROM inserted i;
END;
GO
CREATE OR ALTER TRIGGER trg_producto_update ON tbl_producto AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @usr CHAR(128) = ISNULL(CAST(SESSION_CONTEXT(N'cod_usuario') AS CHAR(128)), SUSER_SNAME());
    DECLARE @ses INT = CAST(SESSION_CONTEXT(N'cod_sesion') AS INT);
    INSERT INTO tbl_bitacora_cambios (fec_registro, cod_guid, ind_accion, cod_usuario, cod_sesion_usuario, cod_tabla, cod_llave, des_datos)
    SELECT GETDATE(), NEWID(), 'U', @usr, @ses, 'tbl_producto', CAST(i.cod_producto AS VARCHAR(2000)),
           CONCAT('ANTES: ', (SELECT d.* FOR JSON PATH, WITHOUT_ARRAY_WRAPPER), ' DESPUES: ', (SELECT i.* FOR JSON PATH, WITHOUT_ARRAY_WRAPPER))
    FROM inserted i INNER JOIN deleted d ON i.cod_producto = d.cod_producto;
END;
GO
CREATE OR ALTER TRIGGER trg_producto_delete ON tbl_producto INSTEAD OF DELETE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM tbl_factura_detalle fd INNER JOIN deleted d ON fd.cod_producto = d.cod_producto)
    BEGIN
        RAISERROR('Regla de Negocio: No se puede eliminar un producto facturado.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END
    DECLARE @usr CHAR(128) = ISNULL(CAST(SESSION_CONTEXT(N'cod_usuario') AS CHAR(128)), SUSER_SNAME());
    DECLARE @ses INT = CAST(SESSION_CONTEXT(N'cod_sesion') AS INT);
    INSERT INTO tbl_bitacora_cambios (fec_registro, cod_guid, ind_accion, cod_usuario, cod_sesion_usuario, cod_tabla, cod_llave, des_datos)
    SELECT GETDATE(), NEWID(), 'D', @usr, @ses, 'tbl_producto', CAST(d.cod_producto AS VARCHAR(2000)),
           (SELECT d.* FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
    FROM deleted d;
    DELETE FROM tbl_producto WHERE cod_producto IN (SELECT cod_producto FROM deleted);
END;
GO

-- 3.6. tbl_factura (I, U, D)
CREATE OR ALTER TRIGGER trg_factura_insert ON tbl_factura AFTER INSERT AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM inserted i INNER JOIN tbl_cliente c ON i.cod_cliente = c.cod_cliente WHERE c.estado <> 'ACTIVO')
    BEGIN
        RAISERROR('Regla de Negocio: Cliente inactivo.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END
    DECLARE @usr CHAR(128) = ISNULL(CAST(SESSION_CONTEXT(N'cod_usuario') AS CHAR(128)), SUSER_SNAME());
    DECLARE @ses INT = CAST(SESSION_CONTEXT(N'cod_sesion') AS INT);
    INSERT INTO tbl_bitacora_cambios (fec_registro, cod_guid, ind_accion, cod_usuario, cod_sesion_usuario, cod_tabla, cod_llave, des_datos)
    SELECT GETDATE(), NEWID(), 'I', @usr, @ses, 'tbl_factura', CAST(i.cod_factura AS VARCHAR(2000)),
           (SELECT i.* FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
    FROM inserted i;
END;
GO
CREATE OR ALTER TRIGGER trg_factura_update ON tbl_factura AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @usr CHAR(128) = ISNULL(CAST(SESSION_CONTEXT(N'cod_usuario') AS CHAR(128)), SUSER_SNAME());
    DECLARE @ses INT = CAST(SESSION_CONTEXT(N'cod_sesion') AS INT);
    INSERT INTO tbl_bitacora_cambios (fec_registro, cod_guid, ind_accion, cod_usuario, cod_sesion_usuario, cod_tabla, cod_llave, des_datos)
    SELECT GETDATE(), NEWID(), 'U', @usr, @ses, 'tbl_factura', CAST(i.cod_factura AS VARCHAR(2000)),
           CONCAT('ANTES: ', (SELECT d.* FOR JSON PATH, WITHOUT_ARRAY_WRAPPER), ' DESPUES: ', (SELECT i.* FOR JSON PATH, WITHOUT_ARRAY_WRAPPER))
    FROM inserted i INNER JOIN deleted d ON i.cod_factura = d.cod_factura;
END;
GO
CREATE OR ALTER TRIGGER trg_factura_delete ON tbl_factura INSTEAD OF DELETE AS
BEGIN
    SET NOCOUNT ON;
    RAISERROR('Regla Fiscal: Las facturas no pueden eliminarse fisicamente.', 16, 1);
    ROLLBACK TRANSACTION;
END;
GO

-- 3.7. tbl_factura_detalle (I, U, D)
CREATE OR ALTER TRIGGER trg_factura_detalle_insert ON tbl_factura_detalle AFTER INSERT AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @usr CHAR(128) = ISNULL(CAST(SESSION_CONTEXT(N'cod_usuario') AS CHAR(128)), SUSER_SNAME());
    DECLARE @ses INT = CAST(SESSION_CONTEXT(N'cod_sesion') AS INT);
    INSERT INTO tbl_bitacora_cambios (fec_registro, cod_guid, ind_accion, cod_usuario, cod_sesion_usuario, cod_tabla, cod_llave, des_datos)
    SELECT GETDATE(), NEWID(), 'I', @usr, @ses, 'tbl_factura_detalle', CAST(i.cod_detalle AS VARCHAR(2000)),
           (SELECT i.* FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
    FROM inserted i;
END;
GO
CREATE OR ALTER TRIGGER trg_factura_detalle_update ON tbl_factura_detalle INSTEAD OF UPDATE AS
BEGIN
    SET NOCOUNT ON;
    RAISERROR('Regla Fiscal: El detalle de factura es inmutable.', 16, 1);
    ROLLBACK TRANSACTION;
END;
GO
CREATE OR ALTER TRIGGER trg_factura_detalle_delete ON tbl_factura_detalle INSTEAD OF DELETE AS
BEGIN
    SET NOCOUNT ON;
    RAISERROR('Regla Fiscal: El detalle de factura no se puede eliminar.', 16, 1);
    ROLLBACK TRANSACTION;
END;
GO

-- 3.8. tbl_nota_credito_debito (I, U, D)
CREATE OR ALTER TRIGGER trg_nota_insert ON tbl_nota_credito_debito AFTER INSERT AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @usr CHAR(128) = ISNULL(CAST(SESSION_CONTEXT(N'cod_usuario') AS CHAR(128)), SUSER_SNAME());
    DECLARE @ses INT = CAST(SESSION_CONTEXT(N'cod_sesion') AS INT);
    INSERT INTO tbl_bitacora_cambios (fec_registro, cod_guid, ind_accion, cod_usuario, cod_sesion_usuario, cod_tabla, cod_llave, des_datos)
    SELECT GETDATE(), NEWID(), 'I', @usr, @ses, 'tbl_nota_credito_debito', CAST(i.cod_nota AS VARCHAR(2000)),
           (SELECT i.* FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
    FROM inserted i;
END;
GO
CREATE OR ALTER TRIGGER trg_nota_update ON tbl_nota_credito_debito INSTEAD OF UPDATE AS
BEGIN
    SET NOCOUNT ON;
    RAISERROR('Regla Fiscal: Las notas de credito/debito son inmutables.', 16, 1);
    ROLLBACK TRANSACTION;
END;
GO
CREATE OR ALTER TRIGGER trg_nota_delete ON tbl_nota_credito_debito INSTEAD OF DELETE AS
BEGIN
    SET NOCOUNT ON;
    RAISERROR('Regla Fiscal: Las notas de credito/debito no pueden eliminarse.', 16, 1);
    ROLLBACK TRANSACTION;
END;
GO

-- ────────────────────────────────────────────────────────────────────────────
-- 4. PROCEDIMIENTOS ALMACENADOS CRUD (REQ 002) CON TRY/CATCH Y TRANSACCIONES
-- Nota: Se usa punto y coma (;) antes de cada THROW para cumplir T-SQL estricto.
-- ────────────────────────────────────────────────────────────────────────────

-- 4.1. Bitácora Acceso
CREATE OR ALTER PROCEDURE sp_registrar_ingreso
    @nom_usuario  VARCHAR(100),
    @nom_terminal VARCHAR(255),
    @dir_ip       VARCHAR(50),
    @cod_acceso   BIGINT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        INSERT INTO tbl_bitacora_acceso (nom_usuario, nom_terminal, dir_ip)
        VALUES (@nom_usuario, @nom_terminal, @dir_ip);
        SET @cod_acceso = SCOPE_IDENTITY();
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE sp_registrar_salida
    @cod_acceso BIGINT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        UPDATE tbl_bitacora_acceso
        SET fec_salida = GETDATE()
        WHERE cod_acceso = @cod_acceso AND fec_salida IS NULL;
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 4.2. CRUD Clientes (Cifrado Columnar REQ 006)
CREATE OR ALTER PROCEDURE sp_cliente_crear
    @nombre              VARCHAR(150),
    @tipo_identificacion VARCHAR(20),
    @num_identificacion  VARCHAR(50),
    @provincia           VARCHAR(60),
    @canton              VARCHAR(60),
    @distrito            VARCHAR(60),
    @barrio              VARCHAR(100),
    @profesion_oficio    VARCHAR(100),
    @actividad_economica VARCHAR(150),
    @correo              VARCHAR(150),
    @telefono            VARCHAR(30),
    @cod_cliente         BIGINT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        OPEN SYMMETRIC KEY SymKeyClientes DECRYPTION BY CERTIFICATE CertClientes;

        INSERT INTO tbl_cliente (
            nombre, tipo_identificacion, num_identificacion, provincia, canton,
            distrito, barrio, profesion_oficio, actividad_economica, correo, telefono
        ) VALUES (
            @nombre, @tipo_identificacion,
            EncryptByKey(Key_GUID('SymKeyClientes'), @num_identificacion),
            @provincia, @canton, @distrito, @barrio, @profesion_oficio, @actividad_economica,
            EncryptByKey(Key_GUID('SymKeyClientes'), @correo),
            EncryptByKey(Key_GUID('SymKeyClientes'), @telefono)
        );
        SET @cod_cliente = SCOPE_IDENTITY();

        CLOSE SYMMETRIC KEY SymKeyClientes;
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        BEGIN TRY CLOSE SYMMETRIC KEY SymKeyClientes; END TRY BEGIN CATCH END CATCH;
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE sp_cliente_actualizar
    @cod_cliente         BIGINT,
    @nombre              VARCHAR(150),
    @tipo_identificacion VARCHAR(20),
    @num_identificacion  VARCHAR(50),
    @provincia           VARCHAR(60),
    @canton              VARCHAR(60),
    @distrito            VARCHAR(60),
    @barrio              VARCHAR(100),
    @profesion_oficio    VARCHAR(100),
    @actividad_economica VARCHAR(150),
    @correo              VARCHAR(150),
    @telefono            VARCHAR(30),
    @estado              VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        OPEN SYMMETRIC KEY SymKeyClientes DECRYPTION BY CERTIFICATE CertClientes;

        UPDATE tbl_cliente
        SET nombre              = @nombre,
            tipo_identificacion = @tipo_identificacion,
            num_identificacion  = EncryptByKey(Key_GUID('SymKeyClientes'), @num_identificacion),
            provincia           = @provincia,
            canton              = @canton,
            distrito            = @distrito,
            barrio              = @barrio,
            profesion_oficio    = @profesion_oficio,
            actividad_economica = @actividad_economica,
            correo              = EncryptByKey(Key_GUID('SymKeyClientes'), @correo),
            telefono            = EncryptByKey(Key_GUID('SymKeyClientes'), @telefono),
            estado              = @estado,
            fec_modificacion    = GETDATE()
        WHERE cod_cliente = @cod_cliente;

        IF @@ROWCOUNT = 0
        BEGIN
            THROW 50012, 'El cliente no existe.', 1;
        END

        CLOSE SYMMETRIC KEY SymKeyClientes;
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        BEGIN TRY CLOSE SYMMETRIC KEY SymKeyClientes; END TRY BEGIN CATCH END CATCH;
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE sp_cliente_listar
    @solo_activos BIT = 0
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        OPEN SYMMETRIC KEY SymKeyClientes DECRYPTION BY CERTIFICATE CertClientes;
        SELECT
            cod_cliente, nombre, tipo_identificacion,
            CONVERT(VARCHAR(50), DecryptByKey(num_identificacion)) AS num_identificacion,
            provincia, canton, distrito, barrio, profesion_oficio, actividad_economica,
            CONVERT(VARCHAR(150), DecryptByKey(correo)) AS correo,
            CONVERT(VARCHAR(30), DecryptByKey(telefono)) AS telefono,
            fec_registro, fec_modificacion, estado
        FROM tbl_cliente
        WHERE (@solo_activos = 0 OR estado = 'ACTIVO')
        ORDER BY cod_cliente DESC;
        CLOSE SYMMETRIC KEY SymKeyClientes;
    END TRY
    BEGIN CATCH
        BEGIN TRY CLOSE SYMMETRIC KEY SymKeyClientes; END TRY BEGIN CATCH END CATCH;
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE sp_cliente_eliminar
    @cod_cliente BIGINT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        DELETE FROM tbl_cliente WHERE cod_cliente = @cod_cliente;
        IF @@ROWCOUNT = 0
        BEGIN
            THROW 50013, 'Cliente no encontrado.', 1;
        END
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 4.3. CRUD Tipos de Impuesto
CREATE OR ALTER PROCEDURE sp_tipo_impuesto_crear
    @codigo       VARCHAR(20),
    @descripcion  VARCHAR(150),
    @porcentaje   DECIMAL(5,2),
    @tarifa       VARCHAR(50),
    @cod_impuesto BIGINT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        INSERT INTO tbl_tipo_impuesto (codigo, descripcion, porcentaje, tarifa)
        VALUES (@codigo, @descripcion, @porcentaje, @tarifa);
        SET @cod_impuesto = SCOPE_IDENTITY();
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE sp_tipo_impuesto_actualizar
    @cod_impuesto BIGINT,
    @codigo       VARCHAR(20),
    @descripcion  VARCHAR(150),
    @porcentaje   DECIMAL(5,2),
    @tarifa       VARCHAR(50),
    @estado       VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        UPDATE tbl_tipo_impuesto
        SET codigo = @codigo, descripcion = @descripcion, porcentaje = @porcentaje, tarifa = @tarifa, estado = @estado
        WHERE cod_impuesto = @cod_impuesto;
        IF @@ROWCOUNT = 0
        BEGIN
            THROW 50020, 'Tipo de impuesto no existe.', 1;
        END
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE sp_tipo_impuesto_listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT cod_impuesto, codigo, descripcion, porcentaje, tarifa, estado
    FROM tbl_tipo_impuesto ORDER BY codigo;
END;
GO

-- 4.4. CRUD Productos / Servicios
CREATE OR ALTER PROCEDURE sp_producto_crear
    @codigo_cabys  VARCHAR(20),
    @descripcion   VARCHAR(250),
    @unidad_medida VARCHAR(30),
    @precio        DECIMAL(18,2),
    @cod_impuesto  BIGINT,
    @cod_producto  BIGINT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        INSERT INTO tbl_producto (codigo_cabys, descripcion, unidad_medida, precio, cod_impuesto)
        VALUES (@codigo_cabys, @descripcion, @unidad_medida, @precio, @cod_impuesto);
        SET @cod_producto = SCOPE_IDENTITY();
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE sp_producto_actualizar
    @cod_producto  BIGINT,
    @codigo_cabys  VARCHAR(20),
    @descripcion   VARCHAR(250),
    @unidad_medida VARCHAR(30),
    @precio        DECIMAL(18,2),
    @cod_impuesto  BIGINT,
    @estado        VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        UPDATE tbl_producto
        SET codigo_cabys = @codigo_cabys, descripcion = @descripcion, unidad_medida = @unidad_medida,
            precio = @precio, cod_impuesto = @cod_impuesto, estado = @estado
        WHERE cod_producto = @cod_producto;
        IF @@ROWCOUNT = 0
        BEGIN
            THROW 50030, 'Producto no existe.', 1;
        END
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE sp_producto_listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT p.cod_producto, p.codigo_cabys, p.descripcion, p.unidad_medida, p.precio,
           p.cod_impuesto, ti.descripcion AS nombre_impuesto, ti.porcentaje, p.estado
    FROM tbl_producto p
    INNER JOIN tbl_tipo_impuesto ti ON p.cod_impuesto = ti.cod_impuesto
    ORDER BY p.cod_producto DESC;
END;
GO

-- 4.5. Facturación Transaccional (Cabecera + Detalle + Cálculo Impuestos)
CREATE OR ALTER PROCEDURE sp_factura_crear
    @cod_cliente     BIGINT,
    @condicion_venta VARCHAR(20),
    @medio_pago      VARCHAR(30),
    @moneda          CHAR(3),
    @tipo_cambio     DECIMAL(10,2),
    @observaciones   VARCHAR(500),
    @detalle         dbo.tt_factura_detalle READONLY,
    @cod_factura     BIGINT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        IF NOT EXISTS (SELECT 1 FROM @detalle)
        BEGIN
            THROW 50040, 'La factura debe tener al menos una linea de detalle.', 1;
        END

        BEGIN TRANSACTION;

        INSERT INTO tbl_factura (cod_cliente, condicion_venta, medio_pago, moneda, tipo_cambio, observaciones)
        VALUES (@cod_cliente, @condicion_venta, @medio_pago, @moneda, @tipo_cambio, @observaciones);
        SET @cod_factura = SCOPE_IDENTITY();

        INSERT INTO tbl_factura_detalle (cod_factura, cod_producto, cantidad, precio_unitario, monto_impuesto, monto_linea)
        SELECT
            @cod_factura,
            d.cod_producto,
            d.cantidad,
            p.precio,
            ROUND((d.cantidad * p.precio) * (ti.porcentaje / 100.0), 2),
            ROUND((d.cantidad * p.precio) * (1 + (ti.porcentaje / 100.0)), 2)
        FROM @detalle d
        INNER JOIN tbl_producto p ON d.cod_producto = p.cod_producto
        INNER JOIN tbl_tipo_impuesto ti ON p.cod_impuesto = ti.cod_impuesto;

        UPDATE f
        SET subtotal        = ISNULL(t.sub, 0),
            total_impuestos = ISNULL(t.imp, 0),
            monto_total     = ISNULL(t.tot, 0)
        FROM tbl_factura f
        CROSS APPLY (
            SELECT
                SUM(cantidad * precio_unitario) AS sub,
                SUM(monto_impuesto) AS imp,
                SUM(monto_linea) AS tot
            FROM tbl_factura_detalle
            WHERE cod_factura = @cod_factura
        ) t
        WHERE f.cod_factura = @cod_factura;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE sp_factura_obtener_detalle
    @cod_factura BIGINT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT f.*, c.nombre AS nombre_cliente
    FROM tbl_factura f
    INNER JOIN tbl_cliente c ON f.cod_cliente = c.cod_cliente
    WHERE f.cod_factura = @cod_factura;

    SELECT fd.*, p.codigo_cabys, p.descripcion AS nombre_producto
    FROM tbl_factura_detalle fd
    INNER JOIN tbl_producto p ON fd.cod_producto = p.cod_producto
    WHERE fd.cod_factura = @cod_factura;
END;
GO

-- 4.6. Notas de Crédito / Débito
CREATE OR ALTER PROCEDURE sp_nota_crear
    @codigo       VARCHAR(30),
    @cod_factura  BIGINT,
    @tipo         VARCHAR(10),
    @motivo       VARCHAR(300),
    @monto_ajuste DECIMAL(18,2),
    @cod_nota     BIGINT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        DECLARE @total_fac DECIMAL(18,2);
        SELECT @total_fac = monto_total FROM tbl_factura WITH (UPDLOCK, HOLDLOCK)
        WHERE cod_factura = @cod_factura AND estado = 'ACTIVA';

        IF @total_fac IS NULL
        BEGIN
            THROW 50050, 'La factura no existe o se encuentra anulada.', 1;
        END

        IF @tipo = 'CREDITO' AND @monto_ajuste > @total_fac
        BEGIN
            THROW 50051, 'El monto de la nota de credito supera el total de la factura.', 1;
        END

        INSERT INTO tbl_nota_credito_debito (codigo, cod_factura, tipo, motivo, monto_ajuste)
        VALUES (@codigo, @cod_factura, @tipo, @motivo, @monto_ajuste);
        SET @cod_nota = SCOPE_IDENTITY();

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE sp_nota_listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT n.*, f.cod_cliente, c.nombre AS nombre_cliente
    FROM tbl_nota_credito_debito n
    INNER JOIN tbl_factura f ON n.cod_factura = f.cod_factura
    INNER JOIN tbl_cliente c ON f.cod_cliente = c.cod_cliente
    ORDER BY n.fec_nota DESC;
END;
GO

-- ────────────────────────────────────────────────────────────────────────────
-- 5. PROCEDIMIENTOS ALMACENADOS DE REPORTES (REQ 007)
-- ────────────────────────────────────────────────────────────────────────────

-- 5.1. Reporte de Clientes Resumido
CREATE OR ALTER PROCEDURE sp_consultar_clientes_resumido
    @fec_desde DATETIME = NULL,
    @fec_hasta DATETIME = NULL,
    @estado    VARCHAR(20) = 'TODOS'
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        SET TRANSACTION ISOLATION LEVEL SNAPSHOT;
        BEGIN TRANSACTION;
        OPEN SYMMETRIC KEY SymKeyClientes DECRYPTION BY CERTIFICATE CertClientes;

        SELECT
            c.cod_cliente,
            c.nombre,
            CONVERT(VARCHAR(50), DecryptByKey(c.num_identificacion)) AS num_identificacion,
            c.actividad_economica,
            CONVERT(VARCHAR(150), DecryptByKey(c.correo)) AS correo,
            CONVERT(VARCHAR(30), DecryptByKey(c.telefono)) AS telefono,
            c.estado,
            c.fec_registro
        FROM tbl_cliente c
        WHERE (@estado IS NULL OR @estado = 'TODOS' OR c.estado = @estado)
          AND (@fec_desde IS NULL OR c.fec_registro >= @fec_desde)
          AND (@fec_hasta IS NULL OR c.fec_registro <= @fec_hasta)
        ORDER BY c.nombre;

        CLOSE SYMMETRIC KEY SymKeyClientes;
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        BEGIN TRY CLOSE SYMMETRIC KEY SymKeyClientes; END TRY BEGIN CATCH END CATCH;
        THROW;
    END CATCH
END;
GO

-- 5.2. Reporte de Facturas
CREATE OR ALTER PROCEDURE sp_consultar_facturas
    @fec_desde DATETIME = NULL,
    @fec_hasta DATETIME = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        SET TRANSACTION ISOLATION LEVEL SNAPSHOT;
        BEGIN TRANSACTION;
        OPEN SYMMETRIC KEY SymKeyClientes DECRYPTION BY CERTIFICATE CertClientes;

        SELECT
            c.nombre AS nombre_cliente,
            CONVERT(VARCHAR(50), DecryptByKey(c.num_identificacion)) AS num_identificacion,
            c.actividad_economica,
            f.cod_factura AS numero_factura,
            f.fec_factura AS fecha,
            f.condicion_venta,
            f.medio_pago,
            f.moneda,
            f.subtotal,
            f.total_impuestos,
            f.monto_total AS total_factura,
            f.estado
        FROM tbl_factura f
        INNER JOIN tbl_cliente c ON f.cod_cliente = c.cod_cliente
        WHERE (@fec_desde IS NULL OR f.fec_factura >= @fec_desde)
          AND (@fec_hasta IS NULL OR f.fec_factura <= @fec_hasta)
        ORDER BY f.fec_factura DESC;

        CLOSE SYMMETRIC KEY SymKeyClientes;
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        BEGIN TRY CLOSE SYMMETRIC KEY SymKeyClientes; END TRY BEGIN CATCH END CATCH;
        THROW;
    END CATCH
END;
GO

-- 5.3. Reporte de Pistas de Auditoría
CREATE OR ALTER PROCEDURE sp_consultar_pistas_auditoria
    @fec_desde   DATETIME = NULL,
    @fec_hasta   DATETIME = NULL,
    @cod_usuario VARCHAR(128) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
        BEGIN TRANSACTION;

        SELECT
            b.cod_bitacora,
            b.fec_registro,
            b.cod_guid,
            CASE b.ind_accion
                WHEN 'I' THEN 'Insercion'
                WHEN 'U' THEN 'Modificacion'
                WHEN 'D' THEN 'Borrado'
            END AS accion,
            RTRIM(b.cod_usuario) AS nombre_usuario,
            b.cod_sesion_usuario,
            b.cod_tabla AS tabla_afectada,
            b.cod_llave,
            b.des_datos AS datos_afectados
        FROM tbl_bitacora_cambios b
        WHERE (@cod_usuario IS NULL OR @cod_usuario = '' OR RTRIM(b.cod_usuario) = @cod_usuario)
          AND (@fec_desde IS NULL OR b.fec_registro >= @fec_desde)
          AND (@fec_hasta IS NULL OR b.fec_registro <= @fec_hasta)
        ORDER BY b.fec_registro DESC;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 5.4. Reporte de Configuración de Seguridad (REQ 007 completo)
CREATE OR ALTER PROCEDURE sp_consultar_configuracion_seguridad
    @nom_usuario VARCHAR(128) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
        BEGIN TRANSACTION;

        ;WITH usuarios AS (
            SELECT dp.principal_id, dp.name
            FROM sys.database_principals dp
            WHERE dp.type IN ('S', 'U', 'G')
              AND dp.name NOT IN ('dbo', 'guest', 'INFORMATION_SCHEMA', 'sys')
              AND (@nom_usuario IS NULL OR @nom_usuario = '' OR dp.name = @nom_usuario)
        ),
        permisos_efectivos AS (
            SELECT u.name AS nombre_usuario, r.name AS rol, p.class, p.major_id, p.permission_name, p.state_desc
            FROM usuarios u
            INNER JOIN sys.database_role_members drm ON drm.member_principal_id = u.principal_id
            INNER JOIN sys.database_principals r ON r.principal_id = drm.role_principal_id
            INNER JOIN sys.database_permissions p ON p.grantee_principal_id = r.principal_id
            UNION ALL
            SELECT u.name AS nombre_usuario, '(Directo)' AS rol, p.class, p.major_id, p.permission_name, p.state_desc
            FROM usuarios u
            INNER JOIN sys.database_permissions p ON p.grantee_principal_id = u.principal_id
        )
        SELECT
            pe.nombre_usuario,
            pe.rol,
            SCHEMA_NAME(o.schema_id) + '.' + o.name AS nombre_objeto,
            CASE
                WHEN o.type = 'U' THEN 'Tabla'
                WHEN o.type = 'V' THEN 'Vista'
                WHEN o.type IN ('P', 'PC') THEN 'Procedimiento'
                WHEN o.type IN ('FN', 'IF', 'TF') THEN 'Funcion'
                ELSE o.type_desc
            END AS tipo_objeto,
            pe.permission_name AS tipo_permiso,
            pe.state_desc AS estado_permiso
        FROM permisos_efectivos pe
        INNER JOIN sys.objects o ON o.object_id = pe.major_id
        WHERE pe.class = 1
          AND o.is_ms_shipped = 0
          AND pe.permission_name IN ('SELECT', 'INSERT', 'UPDATE', 'DELETE', 'EXECUTE', 'CONTROL')
        ORDER BY pe.nombre_usuario, pe.rol, nombre_objeto, pe.permission_name;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- ────────────────────────────────────────────────────────────────────────────
-- 6. ROLES DE BASE DE DATOS Y PERMISOS GRANULARES: GRANT, REVOKE, DENY (REQ 008)
-- ────────────────────────────────────────────────────────────────────────────
CREATE ROLE rol_admin;
CREATE ROLE rol_digitador;
CREATE ROLE rol_consulta;
GO

-- Rol Administrador: Acceso Full
GRANT CONTROL ON DATABASE::PropiDB TO rol_admin;
GRANT EXECUTE TO rol_admin;
GRANT VIEW DEFINITION TO rol_admin;
GO

-- Rol Digitador: Acceso a clientes, facturación y notas de crédito/débito
GRANT EXECUTE ON sp_registrar_ingreso TO rol_digitador;
GRANT EXECUTE ON sp_registrar_salida TO rol_digitador;
GRANT EXECUTE ON sp_cliente_crear TO rol_digitador;
GRANT EXECUTE ON sp_cliente_actualizar TO rol_digitador;
GRANT EXECUTE ON sp_cliente_listar TO rol_digitador;
GRANT EXECUTE ON sp_tipo_impuesto_listar TO rol_digitador;
GRANT EXECUTE ON sp_producto_listar TO rol_digitador;
GRANT EXECUTE ON sp_factura_crear TO rol_digitador;
GRANT EXECUTE ON sp_factura_obtener_detalle TO rol_digitador;
GRANT EXECUTE ON sp_nota_crear TO rol_digitador;
GRANT EXECUTE ON sp_nota_listar TO rol_digitador;
GRANT EXECUTE ON TYPE::dbo.tt_factura_detalle TO rol_digitador;

-- Permisos de tablas requeridos por Triggers/Cifrado y DENY explícitos (REQ 008)
GRANT SELECT, INSERT, UPDATE ON tbl_cliente TO rol_digitador;
GRANT SELECT, INSERT ON tbl_factura TO rol_digitador;
GRANT SELECT, INSERT ON tbl_factura_detalle TO rol_digitador;
GRANT SELECT, INSERT ON tbl_nota_credito_debito TO rol_digitador;
GRANT SELECT ON tbl_producto TO rol_digitador;
GRANT SELECT ON tbl_tipo_impuesto TO rol_digitador;
GRANT INSERT ON tbl_bitacora_cambios TO rol_digitador;
GRANT VIEW DEFINITION ON CERTIFICATE::CertClientes TO rol_digitador;
GRANT CONTROL ON SYMMETRIC KEY::SymKeyClientes TO rol_digitador;

DENY DELETE ON tbl_cliente TO rol_digitador;
DENY DELETE ON tbl_factura TO rol_digitador;
DENY DELETE ON tbl_factura_detalle TO rol_digitador;
DENY DELETE ON tbl_nota_credito_debito TO rol_digitador;
DENY EXECUTE ON sp_consultar_pistas_auditoria TO rol_digitador;
DENY EXECUTE ON sp_consultar_configuracion_seguridad TO rol_digitador;
GO

-- Rol Consulta: Acceso exclusivo a reportería
GRANT EXECUTE ON sp_registrar_ingreso TO rol_consulta;
GRANT EXECUTE ON sp_registrar_salida TO rol_consulta;
GRANT EXECUTE ON sp_consultar_clientes_resumido TO rol_consulta;
GRANT EXECUTE ON sp_consultar_facturas TO rol_consulta;
GRANT EXECUTE ON sp_consultar_pistas_auditoria TO rol_consulta;
GRANT EXECUTE ON sp_consultar_configuracion_seguridad TO rol_consulta;
GRANT VIEW DEFINITION ON CERTIFICATE::CertClientes TO rol_consulta;
GRANT CONTROL ON SYMMETRIC KEY::SymKeyClientes TO rol_consulta;
GRANT VIEW DEFINITION TO rol_consulta;

DENY INSERT, UPDATE, DELETE ON tbl_cliente TO rol_consulta;
DENY INSERT, UPDATE, DELETE ON tbl_factura TO rol_consulta;
DENY INSERT, UPDATE, DELETE ON tbl_producto TO rol_consulta;
DENY INSERT, UPDATE, DELETE ON tbl_tipo_impuesto TO rol_consulta;
DENY INSERT, UPDATE, DELETE ON tbl_nota_credito_debito TO rol_consulta;
GO

-- ────────────────────────────────────────────────────────────────────────────
-- 7. LOGINS Y USERS DE PRUEBA (REQ 003, REQ 004)
-- ────────────────────────────────────────────────────────────────────────────
USE master;
GO

IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = N'usr_admin')
    CREATE LOGIN usr_admin WITH PASSWORD = N'AdminPropi2026!', CHECK_POLICY = OFF;
IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = N'usr_digitador')
    CREATE LOGIN usr_digitador WITH PASSWORD = N'DigiPropi2026!', CHECK_POLICY = OFF;
IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = N'usr_consulta')
    CREATE LOGIN usr_consulta WITH PASSWORD = N'ConsPropi2026!', CHECK_POLICY = OFF;
GO

USE PropiDB;
GO

CREATE USER usr_admin FOR LOGIN usr_admin;
ALTER ROLE rol_admin ADD MEMBER usr_admin;

CREATE USER usr_digitador FOR LOGIN usr_digitador;
ALTER ROLE rol_digitador ADD MEMBER usr_digitador;

CREATE USER usr_consulta FOR LOGIN usr_consulta;
ALTER ROLE rol_consulta ADD MEMBER usr_consulta;
GO

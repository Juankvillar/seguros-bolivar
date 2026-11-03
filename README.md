# Seguros Bolívar — Prueba Técnica

API de gestión de pólizas desarrollada para el módulo práctico de la prueba de Desarrollador TI/Sénior.

El documento consolidado con la solución de los cinco módulos se encuentra en [JuanVillar_Prueba_Tecnica.pdf](JuanVillar_Prueba_Tecnica.pdf).

## Tecnología y alcance

- Java 21.
- Spring Boot 3.4.
- Maven.
- Persistencia en memoria para conservar el alcance del ejercicio.

La API incluye las capas `controller`, `service` y `repository`, las entidades de dominio `Policy` y `Risk`, validaciones de negocio, un mock de CORE y seguridad mínima con API key.

## Supuesto de IPC

El enunciado no define la fuente ni el formato del IPC. Para este ejercicio, la aplicación usa una tasa controlada y configurable de `0.05` (5 %) en `application.yml`; el consumidor no puede modificarla en la solicitud de renovación.

## Ejecutar la aplicación

Requisitos: Java 21 y Maven 3.6+.

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Seguridad mínima

Todos los endpoints requieren el header:

```text
x-api-key: 123456
```

## Endpoints y ejemplos

Listar pólizas por tipo y estado:

```bash
curl -H 'x-api-key: 123456' \
  'http://localhost:8080/polizas?tipo=COLECTIVA&estado=ACTIVA'
```

Consultar riesgos de una póliza:

```bash
curl -H 'x-api-key: 123456' \
  'http://localhost:8080/polizas/1/riesgos'
```

Renovar una póliza. La tasa de IPC configurada para el ejercicio es 5%:

```bash
curl -X POST -H 'x-api-key: 123456' \
  'http://localhost:8080/polizas/2/renovar'
```

Cancelar una póliza:

```bash
curl -X POST -H 'x-api-key: 123456' \
  'http://localhost:8080/polizas/1/cancelar'
```

Agregar un riesgo a una póliza colectiva:

```bash
curl -X POST \
  -H 'x-api-key: 123456' \
  -H 'Content-Type: application/json' \
  -d '{"description":"Apartamento 103"}' \
  'http://localhost:8080/polizas/1/riesgos'
```

Cancelar un riesgo:

```bash
curl -X POST -H 'x-api-key: 123456' \
  'http://localhost:8080/riesgos/101/cancelar'
```

Registrar el intento de actualización hacia CORE:

```bash
curl -X POST \
  -H 'x-api-key: 123456' \
  -H 'Content-Type: application/json' \
  -d '{"evento":"ACTUALIZACION","polizaId":555}' \
  'http://localhost:8080/core-mock/evento'
```

## Ejecutar pruebas

```bash
mvn test
```

## Reglas de negocio implementadas

- Una póliza individual solo puede tener un riesgo.
- Una póliza cancelada no puede renovarse.
- Cancelar una póliza cancela todos sus riesgos.
- Solo las pólizas colectivas aceptan riesgos nuevos.
- Las operaciones de renovación, cancelación y gestión de riesgos registran el intento de actualización hacia CORE.


# Microservicio de Distribución (vg-ms-distribution) - API Endpoints

Este documento describe los endpoints disponibles en el microservicio de Distribución, incluyendo ejemplos de cómo interactuar con ellos para insertar y editar entidades.

La ruta base para todos los endpoints es `/jass/ms-distribution/admin`.

---

## 1. Dashboard y Estadísticas

**Ruta Base:** `/jass/ms-distribution/admin/dashboard`

### Endpoints

*   **`GET /jass/ms-distribution/admin/dashboard/stats`**: Obtener estadísticas completas del dashboard.
*   **`GET /jass/ms-distribution/admin/dashboard/summary`**: Obtener resumen del sistema de distribución.

---

## 2. Programas de Distribución

**Ruta Base:** `/jass/ms-distribution/admin/program`

### Endpoints

*   **`GET /jass/ms-distribution/admin/program`**: Obtener todos los programas de distribución.
*   **`GET /jass/ms-distribution/admin/program?organizationId={id}`**: Obtener programas por ID de organización.
*   **`GET /jass/ms-distribution/admin/program/enriched`**: Obtener todos los programas enriquecidos.
*   **`GET /jass/ms-distribution/admin/program/{id}`**: Obtener un programa por ID.
*   **`POST /jass/ms-distribution/admin/program`**: Crear un nuevo programa de distribución.
*   **`PUT /jass/ms-distribution/admin/program/{id}`**: Actualizar un programa existente.
*   **`DELETE /jass/ms-distribution/admin/program/{id}`**: Eliminar un programa.
*   **`PATCH /jass/ms-distribution/admin/program/activate/{id}`**: Activar un programa.
*   **`PATCH /jass/ms-distribution/admin/program/deactivate/{id}`**: Desactivar un programa.

### Ejemplos JSON

#### **POST /jass/ms-distribution/admin/program (Insertar)**

```json
{
  "organizationId": "6896b2ecf3e398570ffd99d3",
  "programCode": "PROG001",
  "scheduleId": "68c08b7163293e2fe5fcdb1a",
  "routeId": "68c08b7163293e2fe5fcdb1b",
  "zoneId": "ZN0001",
  "streetId": "ST0001",
  "programDate": "2025-10-16",
  "plannedStartTime": "08:00",
  "plannedEndTime": "12:00",
  "responsibleUserId": "68c08b7163293e2fe5fcdb1c",
  "observations": "Programa de distribución matutino"
}
```

#### **PUT /jass/ms-distribution/admin/program/{id} (Editar)**

```json
{
  "organizationId": "6896b2ecf3e398570ffd99d3",
  "programCode": "PROG001",
  "scheduleId": "68c08b7163293e2fe5fcdb1a",
  "routeId": "68c08b7163293e2fe5fcdb1b",
  "zoneId": "ZN0002",
  "streetId": "ST0002",
  "programDate": "2025-10-16",
  "plannedStartTime": "09:00",
  "plannedEndTime": "13:00",
  "actualStartTime": "09:15",
  "actualEndTime": "12:45",
  "status": "COMPLETED",
  "responsibleUserId": "68c08b7163293e2fe5fcdb1c",
  "observations": "Programa completado con retraso menor"
}
```

---

## 3. Rutas de Distribución

**Ruta Base:** `/jass/ms-distribution/admin/route`

### Endpoints

*   **`GET /jass/ms-distribution/admin/route`**: Obtener todas las rutas de distribución.
*   **`GET /jass/ms-distribution/admin/route/active`**: Obtener todas las rutas activas.
*   **`GET /jass/ms-distribution/admin/route/{id}`**: Obtener una ruta por ID.
*   **`POST /jass/ms-distribution/admin/route`**: Crear una nueva ruta de distribución.
*   **`PUT /jass/ms-distribution/admin/route/{id}`**: Actualizar una ruta existente.
*   **`DELETE /jass/ms-distribution/admin/route/{id}`**: Eliminar una ruta.
*   **`PATCH /jass/ms-distribution/admin/route/activate/{id}`**: Activar una ruta.
*   **`PATCH /jass/ms-distribution/admin/route/deactivate/{id}`**: Desactivar una ruta.

### Ejemplos JSON

#### **POST /jass/ms-distribution/admin/route (Insertar)**

```json
{
  "organizationId": "6896b2ecf3e398570ffd99d3",
  "routeCode": "RT001",
  "routeName": "Ruta Centro",
  "zones": "ZN001,ZN002,ZN003",
  "totalEstimatedDuration": 240,
  "responsibleUserId": "68c08b7163293e2fe5fcdb1c"
}
```

#### **PUT /jass/ms-distribution/admin/route/{id} (Editar)**

```json
{
  "organizationId": "6896b2ecf3e398570ffd99d3",
  "routeCode": "RT001",
  "routeName": "Ruta Centro Ampliada",
  "zones": "ZN001,ZN002,ZN003,ZN004",
  "totalEstimatedDuration": 300,
  "responsibleUserId": "68c08b7163293e2fe5fcdb1c",
  "status": "ACTIVE"
}
```

---

## 4. Horarios de Distribución

**Ruta Base:** `/jass/ms-distribution/admin/schedule`

### Endpoints

*   **`GET /jass/ms-distribution/admin/schedule`**: Obtener todos los horarios de distribución.
*   **`GET /jass/ms-distribution/admin/schedule/active`**: Obtener todos los horarios activos.
*   **`GET /jass/ms-distribution/admin/schedule/{id}`**: Obtener un horario por ID.
*   **`POST /jass/ms-distribution/admin/schedule`**: Crear un nuevo horario de distribución.
*   **`PUT /jass/ms-distribution/admin/schedule/{id}`**: Actualizar un horario existente.
*   **`DELETE /jass/ms-distribution/admin/schedule/{id}`**: Eliminar un horario.
*   **`PATCH /jass/ms-distribution/admin/schedule/activate/{id}`**: Activar un horario.
*   **`PATCH /jass/ms-distribution/admin/schedule/deactivate/{id}`**: Desactivar un horario.

### Ejemplos JSON

#### **POST /jass/ms-distribution/admin/schedule (Insertar)**

```json
{
  "organizationId": "6896b2ecf3e398570ffd99d3",
  "scheduleCode": "SCH001",
  "scheduleName": "Horario Matutino",
  "startTime": "08:00",
  "endTime": "12:00",
  "daysOfWeek": "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY",
  "frequency": "DAILY"
}
```

#### **PUT /jass/ms-distribution/admin/schedule/{id} (Editar)**

```json
{
  "organizationId": "6896b2ecf3e398570ffd99d3",
  "scheduleCode": "SCH001",
  "scheduleName": "Horario Matutino Extendido",
  "startTime": "07:30",
  "endTime": "13:00",
  "daysOfWeek": "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY",
  "frequency": "DAILY",
  "status": "ACTIVE"
}
```

---

## 5. Tarifas

**Ruta Base:** `/jass/ms-distribution/admin/fare`

### Endpoints

*   **`GET /jass/ms-distribution/admin/fare`**: Obtener todas las tarifas.
*   **`GET /jass/ms-distribution/admin/fare/active`**: Obtener todas las tarifas activas.
*   **`GET /jass/ms-distribution/admin/fare/{id}`**: Obtener una tarifa por ID.
*   **`POST /jass/ms-distribution/admin/fare`**: Crear una nueva tarifa.
*   **`PUT /jass/ms-distribution/admin/fare/{id}`**: Actualizar una tarifa existente.
*   **`DELETE /jass/ms-distribution/admin/fare/{id}`**: Eliminar una tarifa.
*   **`PATCH /jass/ms-distribution/admin/fare/activate/{id}`**: Activar una tarifa.
*   **`PATCH /jass/ms-distribution/admin/fare/deactivate/{id}`**: Desactivar una tarifa.

### Ejemplos JSON

#### **POST /jass/ms-distribution/admin/fare (Insertar)**

```json
{
  "organizationId": "6896b2ecf3e398570ffd99d3",
  "fareCode": "FARE001",
  "fareName": "Tarifa Residencial Básica",
  "fareType": "RESIDENTIAL",
  "fareAmount": 15.50
}
```

#### **PUT /jass/ms-distribution/admin/fare/{id} (Editar)**

```json
{
  "organizationId": "6896b2ecf3e398570ffd99d3",
  "fareCode": "FARE001",
  "fareName": "Tarifa Residencial Básica Actualizada",
  "fareType": "RESIDENTIAL",
  "fareAmount": 18.00,
  "status": "ACTIVE"
}
```

---

## 6. Programación de Tarifas

**Ruta Base:** `/jass/ms-distribution/admin/fare-schedule`

### Endpoints

*   **`GET /jass/ms-distribution/admin/fare-schedule`**: Obtener todas las programaciones de tarifas.
*   **`GET /jass/ms-distribution/admin/fare-schedule/active`**: Obtener todas las programaciones activas.
*   **`GET /jass/ms-distribution/admin/fare-schedule/{id}`**: Obtener una programación por ID.
*   **`POST /jass/ms-distribution/admin/fare-schedule`**: Crear una nueva programación de tarifa.
*   **`PUT /jass/ms-distribution/admin/fare-schedule/{id}`**: Actualizar una programación existente.
*   **`DELETE /jass/ms-distribution/admin/fare-schedule/{id}`**: Eliminar una programación.
*   **`PATCH /jass/ms-distribution/admin/fare-schedule/activate/{id}`**: Activar una programación.
*   **`PATCH /jass/ms-distribution/admin/fare-schedule/deactivate/{id}`**: Desactivar una programación.

### Ejemplos JSON

#### **POST /jass/ms-distribution/admin/fare-schedule (Insertar)**

```json
{
  "organizationId": "6896b2ecf3e398570ffd99d3",
  "scheduleCode": "FS001",
  "fareId": "68c08b7163293e2fe5fcdb1a",
  "effectiveDate": "2025-11-01T00:00:00",
  "expirationDate": "2025-12-31T23:59:59",
  "description": "Tarifa de temporada alta"
}
```

#### **PUT /jass/ms-distribution/admin/fare-schedule/{id} (Editar)**

```json
{
  "organizationId": "6896b2ecf3e398570ffd99d3",
  "scheduleCode": "FS001",
  "fareId": "68c08b7163293e2fe5fcdb1a",
  "effectiveDate": "2025-11-01T00:00:00",
  "expirationDate": "2026-01-31T23:59:59",
  "description": "Tarifa de temporada alta extendida",
  "status": "ACTIVE"
}
```
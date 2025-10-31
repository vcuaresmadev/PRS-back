# Resumen de Pruebas Unitarias - Backend VG MS Distribution

Este documento proporciona un resumen completo de todas las pruebas unitarias implementadas en el backend del microservicio de distribución.

## 🧪 Tipos de Pruebas

El proyecto contiene dos tipos principales de pruebas:

1. **Pruebas Unitarias** - Validan la lógica de negocio de cada servicio de forma aislada
2. **Pruebas de Integración** - Validan la interacción entre componentes y con la base de datos

## ▶️ Comandos para Ejecutar las Pruebas

### Ejecutar Todas las Pruebas

```bash
# Ejecutar todas las pruebas del proyecto
mvn test

# Ejecutar todas las pruebas con perfil específico
mvn test -Dspring.profiles.active=test

# Generar reporte de cobertura de código
mvn test jacoco:report

# Ver reporte de cobertura en servidor local
mvn test jacoco:report exec:java@serve-report
```

### Ejecutar Pruebas Individualmente

```bash
# Ejecutar solo pruebas unitarias de un servicio específico
mvn -Dtest=DistributionProgramServiceImplTest test

# Ejecutar solo pruebas de integración de un componente
mvn -Dtest=*IntegrationTest test

# Ejecutar una prueba específica por nombre de método
mvn -Dtest=DistributionProgramServiceImplTest#getAll_shouldMapToResponse test

# Ejecutar pruebas parametrizadas
mvn -Dtest=ParametrizedDistributionProgramTest test

# Ejecutar pruebas de un paquete específico
mvn -Dtest=pe.edu.vallegrande.msdistribution.application.services.impl.* test
```

## 📁 Estructura de Pruebas

```
src/test/java/pe/edu/vallegrande/msdistribution/
├── application/services/impl/
│   ├── DistributionProgramServiceImplTest.java
│   ├── DistributionRouteServiceImplTest.java
│   ├── DistributionScheduleServiceImplTest.java
│   ├── FareServiceImplTest.java
│   └── ParametrizedDistributionProgramTest.java
└── integration/
    ├── DistributionProgramIntegrationTest.java
    ├── DistributionRouteIntegrationTest.java
    ├── DistributionScheduleIntegrationTest.java
    └── FareIntegrationTest.java
```

## 🧩 Pruebas Unitarias por Componente

### 1. Programas de Distribución (`DistributionProgramServiceImplTest`)

Valida todas las operaciones CRUD para programas de distribución:

- ✅ Obtener todos los programas
- ✅ Obtener programa por ID (casos existentes y no existentes)
- ✅ Crear nuevo programa
- ✅ Actualizar programa existente
- ✅ Eliminar programa
- ✅ Activar/desactivar programas
- ✅ Manejo de errores del repositorio
- ✅ Validación de datos nulos

### 2. Rutas de Distribución (`DistributionRouteServiceImplTest`)

Valida la gestión de rutas de distribución:

- ✅ Creación de rutas con generación automática de códigos
- ✅ Generación secuencial de códigos (RUT001, RUT002, etc.)
- ✅ Validación de nombres duplicados
- ✅ Obtención de todas las rutas
- ✅ Obtención de ruta por ID
- ✅ Actualización de rutas
- ✅ Eliminación de rutas
- ✅ Manejo de errores en el repositorio
- ✅ Validación de estructura de zonas

### 3. Horarios de Distribución (`DistributionScheduleServiceImplTest`)

Valida la gestión de horarios:

- ✅ Creación de horarios con generación automática de códigos
- ✅ Generación secuencial de códigos (SCH001, SCH002, etc.)
- ✅ Validación de solapamiento de horarios
- ✅ Obtención de todos los horarios
- ✅ Obtención de horario por ID
- ✅ Actualización de horarios
- ✅ Eliminación de horarios
- ✅ Manejo de errores
- ✅ Validación de rangos de tiempo

### 4. Tarifas (`FareServiceImplTest`)

Valida la gestión de tarifas de distribución:

- ✅ Creación de tarifas con generación automática de códigos
- ✅ Generación secuencial de códigos (TAR001, TAR002, etc.)
- ✅ Validación de duplicados
- ✅ Obtención de todas las tarifas
- ✅ Obtención de tarifa por ID
- ✅ Actualización de tarifas
- ✅ Eliminación de tarifas
- ✅ Manejo de errores
- ✅ Validación de precios

### 5. Pruebas Parametrizadas (`ParametrizedDistributionProgramTest`)

Valida escenarios con múltiples combinaciones de datos:

- ✅ Creación de programas por tipo de participante (Admin, Operador, Técnico, Supervisor)
- ✅ Validación de permisos según roles
- ✅ Estados de programa según participante
- ✅ Horarios de distribución por zona geográfica
- ✅ Validación de tipos de tarifa

## 🔗 Pruebas de Integración por Componente

### 1. Programas de Distribución (`DistributionProgramIntegrationTest`)

Pruebas completas contra base de datos embebida:

- ✅ Creación de programas con persistencia real
- ✅ Obtención de programas por ID
- ✅ Listado de todos los programas
- ✅ Actualización de programas
- ✅ Eliminación de programas
- ✅ Activación/desactivación de programas
- ✅ Paginación de resultados
- ✅ Filtros por organización, estado y fecha

### 2. Rutas de Distribución (`DistributionRouteIntegrationTest`)

- ✅ Creación completa de rutas con zonas ordenadas
- ✅ Obtención de rutas por ID
- ✅ Listado de todas las rutas
- ✅ Actualización de rutas
- ✅ Eliminación de rutas
- ✅ Activación/desactivación de rutas
- ✅ Validación de unicidad de nombres

### 3. Horarios de Distribución (`DistributionScheduleIntegrationTest`)

- ✅ Creación de horarios con días de la semana
- ✅ Obtención de horarios por ID
- ✅ Listado de todos los horarios
- ✅ Actualización de horarios
- ✅ Eliminación de horarios
- ✅ Validación de solapamiento
- ✅ Filtrado por organización

### 4. Tarifas (`FareIntegrationTest`)

- ✅ Creación de tarifas con tipos y categorías
- ✅ Obtención de tarifas por ID
- ✅ Listado de todas las tarifas
- ✅ Actualización de tarifas
- ✅ Eliminación de tarifas
- ✅ Validación de duplicados
- ✅ Filtrado por organización y tipo

## 🛠 Tecnologías Utilizadas en Pruebas

- **JUnit 5**: Framework principal de pruebas
- **Mockito**: Para mocking de dependencias
- **Reactor Test**: Para pruebas de flujos reactivos (Mono/Flux)
- **Spring Boot Test**: Para pruebas de integración
- **Testcontainers/MongoDB Embedded**: Para bases de datos en memoria
- **StepVerifier**: Para verificar flujos reactivos

## 📊 Cobertura de Pruebas

Las pruebas cubren los siguientes aspectos:

1. **Escenarios Positivos**: Comportamiento esperado con datos válidos
2. **Escenarios Negativos**: Manejo de errores y casos límite
3. **Validaciones**: Comprobación de reglas de negocio
4. **Integración**: Interacción con otros servicios y base de datos
5. **Permisos**: Control de acceso basado en roles
6. **Formatos**: Validación de estructuras de datos

## 🎯 Objetivos de las Pruebas

- ✅ Validar la corrección de la lógica de negocio
- ✅ Garantizar la integridad de los datos
- ✅ Verificar el manejo adecuado de errores
- ✅ Probar todos los caminos posibles del código
- ✅ Asegurar la calidad del software entregado
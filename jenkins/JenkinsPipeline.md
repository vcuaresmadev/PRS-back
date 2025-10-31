# Jenkins Pipeline para Pruebas Unitarias

## Configuración Inicial

### 1. Instalación de Plugins Requeridos
En Jenkins, instala los siguientes plugins:
- Git Plugin
- GitHub Plugin
- Maven Integration Plugin
- JUnit Plugin
- JaCoCo Plugin
- SonarQube Scanner Plugin
- Docker Pipeline Plugin
- Pipeline Utility Steps Plugin

### 2. Configuración de Herramientas
En "Manage Jenkins" > "Global Tool Configuration":
- Configura JDK con nombre "Java17"
- Configura Maven con nombre "M3"

### 3. Creación del Pipeline Job
1. Ingresa a Jenkins
2. Crea un nuevo item de tipo "Pipeline"
3. Configura el pipeline para obtener el Jenkinsfile desde GitHub

## Jenkinsfile Explicado

El Jenkinsfile incluye las siguientes etapas:

### Checkout
- Obtiene el código fuente del repositorio GitHub

### Build
- Compila el proyecto usando Maven

### Unit Tests
- Ejecuta todas las pruebas unitarias
- Publica resultados de pruebas
- Genera reporte de cobertura de código

### Integration Tests
- Ejecuta pruebas de integración
- Publica resultados de pruebas

### Code Quality Analysis
- Ejecuta análisis de calidad con SonarQube

## Comandos de Pruebas en Jenkins

### Ejecutar todas las pruebas:
```bash
mvn test -Dspring.profiles.active=test
```

### Ejecutar solo pruebas unitarias:
```bash
mvn -Dtest=!*IntegrationTest test
```

### Ejecutar solo pruebas de integración:
```bash
mvn -Dtest=*IntegrationTest test
```

### Generar reporte de cobertura:
```bash
mvn test jacoco:report
```

## Variables de Entorno

Configura las siguientes variables en Jenkins:
- `GITHUB_REPO`: URL de tu repositorio
- `SONAR_TOKEN`: Token de autenticación para SonarQube

## Configuración de Webhook GitHub

Para integración continua automática:
1. En GitHub, ve a Settings > Webhooks
2. Agrega un webhook apuntando a: `http://[TU_JENKINS_URL]/github-webhook/`
3. Selecciona "Just the push event"

## Troubleshooting

### Problemas comunes:
1. **Versiones de Java**: Verifica que esté usando Java 17

# Configuración y Ejecución de Jenkins para tu Pipeline

## 1. Iniciar Jenkins en Codespace

### Opción A: Iniciar Jenkins manualmente
```bash
# Ejecutar el siguiente comando
java -jar jenkins.war
```

## 2. Acceder a Jenkins

1. Abre tu navegador y ve a: `URL pública`

## 3. Configuración Inicial de Jenkins

### Obtener la contraseña inicial
```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### Pasos de configuración:
1. Ingresa la contraseña inicial
2. Selecciona "Install suggested plugins"
3. Crea un usuario administrador

## 4. Instalar Plugins Requeridos

En "Manage Jenkins" > "Manage Plugins", instala:
- Git Plugin
- GitHub Plugin
- Maven Integration Plugin
- JUnit Plugin
- JaCoCo Plugin
- SonarQube Scanner Plugin
- Slack Notification Plugin

## 5. Configurar Herramientas

En "Manage Jenkins" > "Global Tool Configuration":

### JDK
- Name: `Java17`
- Install automatically > Add Installer > Install from adoptopenjdk.net

### Maven
- Name: `M3`
- Install automatically > Add Installer > Default

## 6. Crear el Pipeline Job

1. Haz clic en "New Item"
2. Ingresa un nombre (ej: `prs-back-pipeline`)
3. Selecciona "Pipeline" y haz clic en "OK"
4. En la sección "Pipeline", selecciona:
   - Definition: `Pipeline script from SCM`
   - SCM: `Git`
   - Repository URL: `https://github.com/MariaLazaroVelarde/PRS-back.git`
   - Branches to build: `*/main`
   - Script Path: `Jenkinsfile`

## 7. Ejecutar el Pipeline

1. En el job que creaste, haz clic en "Build Now"
2. Jenkins automáticamente:
   - Descargará tu código desde GitHub
   - Compilará el proyecto
   - Ejecutará las pruebas unitarias
   - Ejecutará las pruebas de integración
   - Generará reportes de cobertura
   - Enviará notificaciones a Slack

## 8. Monitorear la Ejecución

- Ver el progreso en "Build History"
- Haz clic en un número de build para ver detalles
- Revisa las salidas de cada etapa en "Console Output"

## 9. Configuración de Webhook para CI Automático

Para ejecutar el pipeline automáticamente en cada push:

1. En GitHub, ve a tu repositorio > Settings > Webhooks
2. Agrega un webhook con:
   - Payload URL: `http://[TU_URL_JENKINS]:8080/github-webhook/`
   - Content type: `application/json`
   - Events: "Just the push event"
3. En Jenkins, en tu job, ve a "Build Triggers" y marca "GitHub hook trigger for GITScm polling"

## 10. Solución de Problemas

### Problemas comunes y soluciones:

1. **Problemas con SonarQube**:
   - Asegúrate de tener un servidor SonarQube corriendo
   - Configura la conexión en "Manage Jenkins" > "Configure System" > "SonarQube servers"

¡Tu pipeline está listo para ejecutarse automáticamente cada vez que hagas cambios en tu repositorio!
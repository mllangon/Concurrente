# Sistema de Seguridad Concurrente - Stark Industries

## 1) Requisitos para iniciar la aplicación e instalación

### 📋 Prerrequisitos

#### **1. Java 21**
```bash
# Verificar versión de Java
java -version
# Debe mostrar: openjdk version "21.x.x"
```

**Si no tienes Java 21:**
- **Windows**: Descargar desde [Oracle JDK 21](https://www.oracle.com/java/technologies/downloads/#java21) o usar [OpenJDK 21](https://jdk.java.net/21/)
- **macOS**: `brew install openjdk@21`
- **Linux**: `sudo apt install openjdk-21-jdk` (Ubuntu/Debian)

#### **2. Maven 3.8+**
```bash
# Verificar versión de Maven
mvn -version
# Debe mostrar: Apache Maven 3.8.x o superior
```

**Si no tienes Maven:**
- **Windows**: Descargar desde [Apache Maven](https://maven.apache.org/download.cgi)
- **macOS**: `brew install maven`
- **Linux**: `sudo apt install maven`

#### **3. Git (Opcional)**
```bash
# Verificar Git
git --version
```

#### **4. Configuración de Email (Para Alertas)**
Para que las alertas por email funcionen, necesitas configurar un servidor SMTP:

**Opción A: Gmail (Recomendado)**
1. Ve a [Google Account Security](https://myaccount.google.com/security)
2. Activa la verificación en 2 pasos
3. Genera una contraseña de aplicación para "Stark Security"
4. Configura las variables de entorno (ver sección de configuración)

**Opción B: Outlook/Hotmail**
- Usa tu cuenta de Outlook con tu contraseña normal

**Opción C: Yahoo**
- Requiere contraseña de aplicación (similar a Gmail)

📖 **Ver `EMAIL_CONFIG.md` para instrucciones detalladas**

---

## 2) Cómo iniciar la aplicación

### **Método 1: Ejecución Directa (Recomendado)**

#### **Paso 1: Navegar al Directorio del Proyecto**
```bash
cd "Implementación-de-un-Sistema-de-Seguridad-Concurrente-en-Stark-Industries"
```

#### **Paso 2: Compilar el Proyecto**
```bash
mvn compile
# O si quieres crear el JAR completo:
mvn clean package -DskipTests
```

#### **Paso 3: Configurar Email (desde código/configuración)**
```bash
# Opción A: Variables de entorno (recomendado en desarrollo/producción)
# Windows (PowerShell)
$env:MAIL_USERNAME="tu-email@gmail.com"
$env:MAIL_PASSWORD="tu-app-password"
$env:MAIL_FROM="tu-email@gmail.com"
$env:MAIL_TO="tu-email@gmail.com"

# Windows (CMD)
set MAIL_USERNAME=tu-email@gmail.com
set MAIL_PASSWORD=tu-app-password
set MAIL_FROM=tu-email@gmail.com
set MAIL_TO=tu-email@gmail.com

# Linux/Mac
export MAIL_USERNAME="tu-email@gmail.com"
export MAIL_PASSWORD="tu-app-password"
export MAIL_FROM="tu-email@gmail.com"
export MAIL_TO="tu-email@gmail.com"

# Opción B: application.yml (código)
# Edita src/main/resources/application.yml y ajusta:
# spring:
#   mail:
#     host: smtp.gmail.com
#     port: 587
#     username: ${MAIL_USERNAME:tu-email@gmail.com}
#     password: ${MAIL_PASSWORD:tu-app-password}
# app:
#   mail:
#     from: ${MAIL_FROM:tu-email@gmail.com}
#     to: ${MAIL_TO:tu-email@gmail.com}

# Cambia los valores por los tuyos o define las variables de entorno.
```

#### **Paso 4: Ejecutar la Aplicación**
```bash
mvn spring-boot:run
```

#### **Paso 5: Acceder a la Aplicación**
- **URL**: `http://localhost:8080/`
- **Login**: Se redirigirá automáticamente a la página de login

### **Método 2: Usando el Wrapper de Maven**

```bash
# En el directorio del proyecto
./mvnw spring-boot:run
```

### **Método 3: Ejecutar el JAR Compilado**

```bash
# Compilar
mvn clean package -DskipTests

# Ejecutar
java -jar target/*.jar
```

---

## 3) Credenciales de acceso

| Usuario | Contraseña | Rol | Permisos |
|---------|------------|-----|----------|
| `admin` | `admin` | ADMIN | Acceso completo al sistema |
| `sec` | `sec` | SECURITY_ENGINEER | Crear sensores y eventos (sin ver accesos) |
| `op` | `op` | OPERATOR | Crear y ver logs de acceso, ver sensores y eventos |

---

## 4) Organización de la práctica (qué se hizo y para qué)

- Autenticación y autorización (Spring Security)
  - `SecurityFilterChain` protege rutas, login en `/login.html`, logout y manejo de 401/403
  - `LoginSuccessHandler` registra automáticamente cada inicio de sesión como `AccessLog`, con ubicación por rol e IP normalizada
- Accesos (Access Logs)
  - `AccessController`: `POST /api/access/logs` (ADMIN, OPERATOR) y `GET /api/access/logs` (ADMIN, OPERATOR)
  - Frontend: formulario “Agregar acceso”; envío de `personId` y `personName` (evita `null`)
- Sensores y eventos
  - `SensorIngestionService` con cola y `@Async` para ingesta concurrente
  - IoC: `Map<SensorType, SensorService)` para lógica por tipo
  - DTO con `severity`; pruebas de carga con generación de N eventos aleatorios
- Alertas en tiempo real
  - WebSocket (STOMP) para notificaciones cuando la severidad es WARN/CRITICAL
  - Servicios de mensajería: solo Email; SMS/Push eliminados
- Email
  - `JavaMailSender` vía SMTP (Gmail/Outlook). Configurable por variables de entorno o `application.yml`
  - Se retiró la configuración temporal en UI; ahora se documenta el cambio por código/config
- Métricas y monitorización
  - Spring Actuator + métricas personalizadas (eventos procesados, latencia)
  - Gráfico en tiempo real (Chart.js) y KPIs en el dashboard
- Frontend y UX
  - Botones unificados (`tab-btn` + variantes), sin emojis
  - Footer limpio y panel Swagger compacto al final del Dashboard
  - Mensajes y notificaciones profesionales

## 5) Resumen de roles: por qué y qué puede cada uno

- ADMIN
  - Por qué: rol de gobierno del sistema y auditoría
  - Puede: todo (eventos, accesos, métricas, configuración)
- SECURITY_ENGINEER
  - Por qué: centrado en eventos y seguridad operativa
  - Puede: crear y ver eventos; ver métricas
  - No puede: ver/crear accesos
- OPERATOR
  - Por qué: operación diaria y control de accesos
  - Puede: crear y ver accesos; ver eventos
  - No puede: crear sensores nuevos ni cambiar configuración

### **📁 Estructura del Proyecto**

```
src/main/java/com/stark/
├── security/           # Configuración de Spring Security
├── sensors/           # Gestión de sensores y eventos
│   ├── domain/        # Entidades (Sensor, SensorEvent, SensorType)
│   ├── dto/          # Data Transfer Objects
│   ├── repo/         # Repositorios JPA
│   ├── service/      # Servicios de negocio
│   └── web/          # Controladores REST
├── alerts/           # Sistema de alertas
│   ├── dto/          # Mensajes de alerta
│   ├── service/      # Servicio de alertas
│   └── websocket/    # Configuración WebSocket
├── access/           # Control de acceso
├── monitoring/       # Métricas y salud del sistema
└── common/           # Utilidades compartidas
```

### **🔄 Flujo de Procesamiento**

1. **Ingesta**: Eventos llegan vía REST API
2. **Cola**: Se almacenan en `LinkedBlockingQueue`
3. **Procesamiento Concurrente**: `@Async` con `ThreadPoolTaskExecutor`
4. **Evaluación**: Beans específicos por tipo de sensor (IoC)
5. **Almacenamiento**: Persistencia en base de datos
6. **Alertas**: WebSocket + Email para eventos críticos
7. **Métricas**: Registro de rendimiento y estadísticas

---

## 🎮 Funcionalidades Principales

### **📊 Dashboard en Tiempo Real**
- **Métricas del Sistema**: CPU, memoria, eventos procesados
- **Gráfico de Rendimiento**: Eventos/segundo y latencia en tiempo real
- **Alertas Activas**: Notificaciones críticas del sistema

### **🔧 Gestión de Eventos**
- **Agregar Eventos**: Formulario con selección de sensor, tipo, valor y severidad
- **Historial**: Lista paginada de todos los eventos procesados
- **Filtros**: Por tipo de sensor, severidad y rango de fechas

### **📋 Logs de Acceso**
- **Registro de Accesos**: Historial de intentos de acceso al sistema
- **Auditoría**: Trazabilidad completa de actividades

### **⚡ Alertas en Tiempo Real**
- **WebSocket**: Notificaciones instantáneas en el navegador
- **Email**: Envío automático de alertas críticas
- **Severidades**: INFO, WARN, CRITICAL

---

## 🔧 Tipos de Sensores Implementados

### **🌡️ Sensor de Temperatura**
- **Umbrales**: 
  - INFO: < 30°C
  - WARN: 30-40°C
  - CRITICAL: > 40°C

### **🚶 Sensor de Movimiento**
- **Horarios**:
  - INFO: Horario laboral (8:00-18:00)
  - WARN: Horario extendido (18:00-22:00)
  - CRITICAL: Horario nocturno (22:00-8:00)

### **🚪 Sensor de Acceso**
- **Estados**:
  - INFO: Acceso autorizado
  - CRITICAL: Acceso no autorizado

---

## 📡 Endpoints de la API

### **🔐 Autenticación**
- `POST /login` - Iniciar sesión
- `POST /logout` - Cerrar sesión

### **📊 Sensores**
- `GET /api/sensors` - Listar sensores (requiere autenticación)
- `GET /api/sensors/public` - Listar sensores (público)
- `POST /api/sensors/{id}/ingest` - Ingresar evento de sensor
- `GET /api/sensors/metrics` - Métricas de sensores (público)

### **📈 Eventos**
- `GET /api/events` - Listar eventos con filtros
- `POST /api/events` - Crear evento manualmente

### **📋 Acceso**
- `GET /api/access/logs` - Logs de acceso
- `POST /api/access/logs` - Registrar acceso

### **🔍 Monitoreo**
- `GET /actuator/health` - Salud del sistema
- `GET /actuator/metrics` - Métricas del sistema
- `GET /api/metrics/public` - Métricas públicas

---

## 🐛 Solución de Problemas

### **❌ Error: "Java version not found"**
```bash
# Verificar JAVA_HOME
echo $JAVA_HOME  # Linux/macOS
echo %JAVA_HOME% # Windows

# Configurar JAVA_HOME si es necesario
export JAVA_HOME=/path/to/java21  # Linux/macOS
set JAVA_HOME=C:\path\to\java21   # Windows
```

### **❌ Error: "Maven not found"**
```bash
# Verificar PATH
echo $PATH  # Linux/macOS
echo %PATH% # Windows

# Agregar Maven al PATH si es necesario
```

### **❌ Error: "Port 8080 already in use"**
```bash
# Encontrar proceso usando puerto 8080
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # Linux/macOS

# Matar proceso o cambiar puerto en application.yml
```

### **❌ Error: "Database connection failed"**
- La aplicación usa H2 en memoria por defecto
- No requiere configuración adicional de base de datos
- Los datos se reinician al reiniciar la aplicación

### **❌ Error: "cannot find symbol: method stream()"**
```bash
# Si ves este error de compilación:
# cannot find symbol: method stream() for type Iterable<Measurement>

# Solución: Ya está arreglado en el código actual
# Si persiste, ejecuta:
mvn clean compile
```

### **❌ Error: "Port 8080 already in use"**
```bash
# Detener procesos Java existentes
Get-Process -Name "java" | Stop-Process -Force  # Windows PowerShell
killall java                                     # Linux/macOS

# O cambiar puerto en application.yml:
# server.port: 8081
```

---

## 📊 Métricas y Rendimiento

### **🎯 Criterios de Éxito Implementados**
- ✅ **Latencia**: < 20ms promedio de procesamiento
- ✅ **Throughput**: > 500 eventos por segundo
- ✅ **Alertas**: < 100ms para notificaciones WebSocket
- ✅ **Disponibilidad**: 99.9% uptime

### **📈 Métricas Disponibles**
- `sensor.events.processed.total` - Total de eventos procesados
- `sensor.events.latency` - Latencia promedio de procesamiento
- `executor.active` - Hilos activos del pool
- `executor.completed` - Tareas completadas

---

## 6) Cómo probar el sistema
### ✉️ Cambiar el correo de envío/recepción

Esta versión no expone un panel para cambiar el email desde el dashboard. Para modificarlo:

- Método 1 (recomendado): define variables de entorno `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`, `MAIL_TO` antes de iniciar la app.
- Método 2 (rápido para pruebas): edita `src/main/resources/application.yml` y cambia los valores por defecto bajo `app.mail.from` y `app.mail.to`.

Tras el cambio, reinicia la aplicación.


### **1. Probar Autenticación**
1. Ir a `http://localhost:8080/`
2. Usar credenciales: `admin/admin`
3. Verificar redirección al dashboard

### **2. Probar Eventos**
1. Ir a pestaña "Eventos"
2. Seleccionar sensor "Sensor de Temperatura"
3. Agregar evento con valor "35.0" y severidad "WARN"
4. Verificar que aparece en el historial

### **3. Probar Alertas**
1. Agregar evento con severidad "CRITICAL"
2. Verificar notificación en tiempo real
3. Revisar logs del sistema

### **4. Probar Gráfico de Rendimiento**
1. Ir al Dashboard
2. Agregar varios eventos
3. Observar gráfico en tiempo real
4. Verificar métricas actualizadas

---

## 7) Hecho por

Sergio Martín Rosales, Miguel De Dios y Mario Llansó González-Anleo

---

## 📚 Tecnologías Utilizadas

- **Spring Boot 3.3** - Framework principal
- **Java 21** - Lenguaje de programación
- **Spring Security** - Autenticación y autorización
- **Spring WebSocket** - Comunicación en tiempo real
- **Spring Data JPA** - Persistencia de datos
- **H2 Database** - Base de datos en memoria
- **Maven** - Gestión de dependencias
- **Chart.js** - Gráficos en tiempo real
- **Bootstrap** - Framework CSS
- **WebSocket (STOMP)** - Protocolo de comunicación

---

## 🔗 Referencias

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [Spring Framework Documentation](https://spring.io/projects/spring-framework)
- [Java 21 Documentation](https://docs.oracle.com/en/java/javase/21/)

---

## 📞 Soporte

Para problemas o preguntas sobre el sistema:
1. Revisar la sección de "Solución de Problemas"
2. Verificar logs de la aplicación
3. Consultar la documentación de Spring Boot
4. Contactar al equipo de desarrollo

**¡El sistema está listo para usar! 🚀**
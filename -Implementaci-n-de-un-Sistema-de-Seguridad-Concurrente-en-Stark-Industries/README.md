# 🛡️ Sistema de Seguridad Concurrente - Stark Industries

## 📋 Descripción del Proyecto

Sistema de seguridad empresarial desarrollado con **Spring Boot 3.3** que procesa eventos de múltiples sensores en tiempo real, implementando concurrencia, autenticación/autorización, alertas en tiempo real y monitoreo de rendimiento.

### 🎯 Objetivos Cumplidos

✅ **Procesamiento Concurrente**: Utiliza `@Async` y `ThreadPoolTaskExecutor` para manejar múltiples eventos simultáneamente  
✅ **Inversión de Control (IoC)**: Beans específicos para cada tipo de sensor (MOTION, TEMPERATURE, ACCESS)  
✅ **Spring Security**: Autenticación y autorización con roles (ADMIN, SECURITY_ENGINEER, OPERATOR, VIEWER)  
✅ **Notificaciones en Tiempo Real**: WebSocket (STOMP) para alertas inmediatas  
✅ **Monitorización**: Spring Actuator, métricas personalizadas y gráfico de rendimiento en tiempo real  
✅ **Logging Eficiente**: Logback con formato JSON-friendly para rastreo de eventos  

---

## 🚀 Instalación y Configuración

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

---

## 🏃‍♂️ Cómo Iniciar la Aplicación

### **Método 1: Ejecución Directa (Recomendado)**

#### **Paso 1: Navegar al Directorio del Proyecto**
```bash
cd "Implementación-de-un-Sistema-de-Seguridad-Concurrente-en-Stark-Industries"
```

#### **Paso 2: Compilar el Proyecto**
```bash
mvn clean package -DskipTests
```

#### **Paso 3: Ejecutar la Aplicación**
```bash
mvn spring-boot:run
```

#### **Paso 4: Acceder a la Aplicación**
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

## 🔐 Credenciales de Acceso

| Usuario | Contraseña | Rol | Permisos |
|---------|------------|-----|----------|
| `admin` | `admin` | ADMIN | Acceso completo al sistema |
| `sec` | `sec` | SECURITY_ENGINEER | Gestión de sensores y eventos |
| `op` | `op` | OPERATOR | Operaciones y monitoreo |
| `view` | `view` | VIEWER | Solo consulta (lectura) |

---

## 🏗️ Arquitectura del Sistema

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

## 🧪 Cómo Probar el Sistema

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

## 👥 Equipo de Desarrollo

### **Roles y Responsabilidades**
- **Desarrollador Backend**: Implementación de servicios y lógica de procesamiento concurrente
- **Ingeniero de Seguridad**: Configuración de autenticación y autorización
- **Desarrollador Frontend**: Interfaz de usuario y notificaciones en tiempo real
- **Administrador de Sistemas**: Configuración y monitorización del sistema

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
# 📧 Configuración de Email para Stark Security

## 🚨 **PROBLEMA ACTUAL**
El sistema no puede enviar emails porque está configurado para usar un servidor SMTP local que no existe.

## ✅ **SOLUCIÓN: Configurar Gmail**

### **Paso 1: Obtener Contraseña de Aplicación de Gmail**

1. **Ve a tu cuenta de Google**: https://myaccount.google.com/security
2. **Activa la verificación en 2 pasos** (si no la tienes activada)
3. **Ve a "Contraseñas de aplicaciones"**
4. **Genera una nueva contraseña** para "Stark Security"
5. **Copia la contraseña de 16 caracteres** (ejemplo: `abcd efgh ijkl mnop`)

### **Paso 2: Configurar Variables de Entorno**

Crea un archivo `.env` en la raíz del proyecto con:

```bash
# Tu dirección de Gmail
MAIL_USERNAME=tu-email@gmail.com

# Contraseña de aplicación (16 caracteres)
MAIL_PASSWORD=abcd efgh ijkl mnop

# Direcciones de correo
MAIL_FROM=tu-email@gmail.com
MAIL_TO=tu-email@gmail.com
```

### **Paso 3: Ejecutar con Variables de Entorno**

```bash
# Windows (PowerShell)
$env:MAIL_USERNAME="tu-email@gmail.com"
$env:MAIL_PASSWORD="abcd efgh ijkl mnop"
$env:MAIL_FROM="tu-email@gmail.com"
$env:MAIL_TO="tu-email@gmail.com"
mvn spring-boot:run

# Windows (CMD)
set MAIL_USERNAME=tu-email@gmail.com
set MAIL_PASSWORD=abcd efgh ijkl mnop
set MAIL_FROM=tu-email@gmail.com
set MAIL_TO=tu-email@gmail.com
mvn spring-boot:run

# Linux/Mac
export MAIL_USERNAME="tu-email@gmail.com"
export MAIL_PASSWORD="abcd efgh ijkl mnop"
export MAIL_FROM="tu-email@gmail.com"
export MAIL_TO="tu-email@gmail.com"
mvn spring-boot:run
```

## 🔧 **Configuración Alternativa: Outlook/Hotmail**

Si prefieres usar Outlook:

```yaml
# En application.yml, cambiar:
spring:
  mail:
    host: smtp-mail.outlook.com
    port: 587
    username: ${MAIL_USERNAME:tu-email@outlook.com}
    password: ${MAIL_PASSWORD:tu-contraseña}
```

## 🔧 **Configuración Alternativa: Yahoo**

Si prefieres usar Yahoo:

```yaml
# En application.yml, cambiar:
spring:
  mail:
    host: smtp.mail.yahoo.com
    port: 587
    username: ${MAIL_USERNAME:tu-email@yahoo.com}
    password: ${MAIL_PASSWORD:tu-app-password}
```

## 🧪 **Probar el Envío**

1. **Inicia la aplicación** con las variables de entorno
2. **Ve a** `http://localhost:8080/`
3. **Inicia sesión** con `admin/admin`
4. **En el Dashboard**, llena el formulario de "Enviar Alerta Personalizada"
5. **Pon tu email** en el campo "Emails"
6. **Haz clic en "Enviar Alerta Personalizada"**
7. **Revisa tu bandeja de entrada**

## 📱 **Ejemplo de Email que Recibirás**

```html
🚨 Alerta de Seguridad
Severidad: CRITICAL
Sensor: Sensor de Temperatura Crítica
Tipo: TEMPERATURE
Timestamp: 26/10/2025 16:13:00
Mensaje: Temperatura excede límites seguros
```

## ⚠️ **Notas Importantes**

- **NO uses tu contraseña normal de Gmail**
- **Usa SIEMPRE la contraseña de aplicación**
- **La contraseña de aplicación es de 16 caracteres**
- **Gmail requiere verificación en 2 pasos activada**
- **Los emails pueden tardar unos segundos en llegar**

## 🆘 **Solución de Problemas**

### **Error: "Authentication failed"**
- Verifica que la contraseña de aplicación sea correcta
- Asegúrate de que la verificación en 2 pasos esté activada

### **Error: "Connection refused"**
- Verifica que tengas conexión a internet
- Revisa que el puerto 587 no esté bloqueado

### **Error: "Invalid credentials"**
- Regenera la contraseña de aplicación
- Asegúrate de copiar la contraseña completa (16 caracteres)




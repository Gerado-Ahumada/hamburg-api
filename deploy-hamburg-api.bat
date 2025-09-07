@echo off
REM Script de despliegue para Hamburg API en Windows
REM Este script configura las variables de entorno necesarias y ejecuta la aplicación

REM Configuración de variables de entorno para JWT
set HAMBURG_APP_JWTSECRET=hamburgSecretKeyWithAtLeast256BitsForSecurityRequirements12345678901234567890
set HAMBURG_APP_JWTEXPIRATIONMS=86400000

REM Configuración de variables de entorno para Base de Datos
set "DB_URL=jdbc:mysql://localhost:3306/hamburgbackend_db?useSSL=false&serverTimezone=UTC"
set DB_USERNAME=hamburg
set DB_PASSWORD=hamburg123
set DB_DRIVER=com.mysql.cj.jdbc.Driver

REM Otras variables de entorno (pueden añadirse según sea necesario)
set SPRING_PROFILES_ACTIVE=prod

REM Mensaje informativo
echo Configurando variables de entorno para Hamburg API...
echo JWT Secret configurado
echo JWT Expiration configurado: %HAMBURG_APP_JWTEXPIRATIONMS% ms
echo Perfil activo: %SPRING_PROFILES_ACTIVE%

REM Ejecutar la aplicación con Maven
echo Iniciando Hamburg API...
mvn spring-boot:run
@echo off
REM Script de despliegue para Hamburg API en Windows
REM Este script configura las variables de entorno necesarias y ejecuta la aplicación

REM Configuración de variables de entorno para JWT
set HAMBURG_APP_JWT_SECRET=hamburgSecretKeyWithAtLeast256BitsForSecurityRequirements12345678901234567890
set HAMBURG_APP_JWT_EXPIRATION_MS=86400000

REM Otras variables de entorno (pueden añadirse según sea necesario)
set SPRING_PROFILES_ACTIVE=prod

REM Mensaje informativo
echo Configurando variables de entorno para Hamburg API...
echo JWT Secret configurado
echo JWT Expiration configurado: %HAMBURG_APP_JWT_EXPIRATION_MS% ms
echo Perfil activo: %SPRING_PROFILES_ACTIVE%

REM Ejecutar la aplicación con Maven
echo Iniciando Hamburg API...
mvn spring-boot:run
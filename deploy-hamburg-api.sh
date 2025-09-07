#!/bin/bash

# Script de despliegue para Hamburg API
# Este script configura las variables de entorno necesarias y ejecuta la aplicación

# Configuración de variables de entorno para JWT
export HAMBURG_APP_JWT_SECRET="hamburgSecretKeyWithAtLeast256BitsForSecurityRequirements12345678901234567890"
export HAMBURG_APP_JWT_EXPIRATION_MS="86400000"

# Configuración de variables de entorno para Base de Datos
export DB_URL="jdbc:mysql://localhost:3306/hamburgbackend_db?useSSL=false&serverTimezone=UTC"
export DB_USERNAME="hamburg"
export DB_PASSWORD="hamburg123"
export DB_DRIVER="com.mysql.cj.jdbc.Driver"

# Otras variables de entorno (pueden añadirse según sea necesario)
export SPRING_PROFILES_ACTIVE="prod"

# Mensaje informativo
echo "Configurando variables de entorno para Hamburg API..."
echo "JWT Secret configurado"
echo "JWT Expiration configurado: $HAMBURG_APP_JWT_EXPIRATION_MS ms"
echo "Perfil activo: $SPRING_PROFILES_ACTIVE"

# Ejecutar la aplicación con Maven
echo "Iniciando Hamburg API..."
mvn spring-boot:run
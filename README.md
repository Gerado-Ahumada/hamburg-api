# Hamburg Backend

Proyecto backend desarrollado en Java 17 con Spring Boot que implementa un módulo de login utilizando MySQL con configuración segura mediante variables de entorno.

## Características

- Desarrollado con Java 17 y Spring Boot 3.1.5
- Autenticación mediante JWT (JSON Web Token)
- Base de datos MySQL con configuración externalizada
- Implementación de Spring Security
- Variables de entorno para credenciales sensibles
- Datos de prueba precargados automáticamente

## Estructura del Proyecto

```
src
├── main
│   ├── java
│   │   └── com
│   │       └── hamburg
│   │           └── backend
│   │               ├── config
│   │               │   └── DataInitializer.java
│   │               ├── controller
│   │               │   └── AuthController.java
│   │               ├── dto
│   │               │   ├── LoginRequest.java
│   │               │   └── LoginResponse.java
│   │               ├── model
│   │               │   └── Usuario.java
│   │               ├── repository
│   │               │   └── UsuarioRepository.java
│   │               ├── security
│   │               │   ├── WebSecurityConfig.java
│   │               │   ├── UserDetailsImpl.java
│   │               │   └── jwt
│   │               │       ├── AuthEntryPointJwt.java
│   │               │       ├── AuthTokenFilter.java
│   │               │       └── JwtUtils.java
│   │               ├── service
│   │               │   ├── AuthService.java
│   │               │   └── UsuarioService.java
│   │               └── HamburgBackendApplication.java
│   └── resources
│       ├── application.properties
│       └── data.sql
└── test
    └── java
        └── com
            └── hamburg
                └── backend
                    ├── controller
                    │   └── AuthControllerTest.java
                    └── service
                        └── AuthServiceTest.java
```

## Requisitos

- Java 17 o superior
- Maven 3.6 o superior
- MySQL 8.0 o superior
- Base de datos MySQL configurada y accesible

## Configuración

### Base de Datos MySQL

El proyecto requiere una base de datos MySQL configurada. Los usuarios de prueba se crean automáticamente:

- **Usuario Administrador**: admin / admin
- **Usuario de Prueba**: testplayer / testpassword

### Variables de Entorno Requeridas

Antes de ejecutar la aplicación, debe configurar las siguientes variables de entorno para la conexión a MySQL:

- `DB_URL`: URL de conexión a la base de datos MySQL
- `DB_USERNAME`: Usuario de la base de datos MySQL  
- `DB_PASSWORD`: Contraseña de la base de datos MySQL
- `HAMBURG_APP_JWT_SECRET`: Clave secreta para JWT (mínimo 256 bits)
- `HAMBURG_APP_JWT_EXPIRATION_MS`: Tiempo de expiración del token en milisegundos
- `SPRING_PROFILES_ACTIVE`: Perfil activo de Spring

## Ejecución

### Ejecución Directa

Para ejecutar el proyecto directamente, utiliza el siguiente comando:

```bash
./mvnw spring-boot:run
```

O si tienes Maven instalado:

```bash
mvn spring-boot:run
```

### Scripts de Despliegue

El proyecto incluye scripts de despliegue que configuran las variables de entorno necesarias:

#### Windows
Utilice el script `deploy-hamburg-api.bat` para iniciar la aplicación en Windows:

```bash
./deploy-hamburg-api.bat
```

#### Linux/Mac
Utilice el script `deploy-hamburg-api.sh` para iniciar la aplicación en Linux o Mac:

```bash
# Dar permisos de ejecución al script
chmod +x deploy-hamburg-api.sh

# Ejecutar el script
./deploy-hamburg-api.sh
```

## Variables de Entorno

La aplicación utiliza las siguientes variables de entorno:

### Base de Datos
- `DB_URL`: URL de conexión a la base de datos MySQL
- `DB_USERNAME`: Usuario de la base de datos MySQL
- `DB_PASSWORD`: Contraseña de la base de datos MySQL

### JWT y Seguridad
- `HAMBURG_APP_JWT_SECRET`: Clave secreta para la generación de tokens JWT (mínimo 256 bits)
- `HAMBURG_APP_JWT_EXPIRATION_MS`: Tiempo de expiración del token JWT en milisegundos

### Configuración de Spring
- `SPRING_PROFILES_ACTIVE`: Perfil activo de Spring (dev, prod, etc.)

### Personalización de Variables de Entorno

Puede personalizar las variables de entorno editando los scripts de despliegue o configurándolas directamente en su entorno antes de ejecutar la aplicación:

#### Windows
```powershell
# Configurar las variables de entorno antes de ejecutar
set DB_URL=<tu_url_mysql>
set DB_USERNAME=<tu_usuario>
set DB_PASSWORD=<tu_contraseña>
set HAMBURG_APP_JWT_SECRET=<tu_clave_jwt>
set HAMBURG_APP_JWT_EXPIRATION_MS=<tiempo_expiracion>
./deploy-hamburg-api.bat
```

#### Linux/Mac
```bash
# Configurar las variables de entorno antes de ejecutar
export DB_URL="<tu_url_mysql>"
export DB_USERNAME="<tu_usuario>"
export DB_PASSWORD="<tu_contraseña>"
export HAMBURG_APP_JWT_SECRET="<tu_clave_jwt>"
export HAMBURG_APP_JWT_EXPIRATION_MS="<tiempo_expiracion>"
./deploy-hamburg-api.sh
```

## Endpoints

### Autenticación

- **POST** `/api/auth/login`: Autenticar usuario y obtener token JWT

  Ejemplo de solicitud:
  ```json
  {
    "username": "admin",
    "password": "admin"
  }
  ```

  Ejemplo de respuesta:
  ```json
  {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "tipo": "Bearer",
    "id": 1,
    "username": "admin",
    "email": "admin@hamburg.com",
    "rol": "ROLE_ADMIN"
  }
  ```

## Seguridad

### Archivos Excluidos del Repositorio

Por seguridad, los siguientes archivos están excluidos del control de versiones:

- Scripts de deploy con credenciales (`deploy-hamburg-api.bat`, `deploy-hamburg-api.sh`)
- Archivos de configuración con credenciales (`.env`, `application-*.properties`)
- Carpetas de configuración del IDE (`.idea/`, `.vscode/`)
- Archivos compilados y temporales (`target/`, `*.log`)

### Mejores Prácticas Implementadas

- ✅ Credenciales externalizadas mediante variables de entorno
- ✅ Archivos sensibles excluidos del repositorio
- ✅ Configuración segura de JWT
- ✅ Scripts de deploy locales para desarrollo

## Acceso a la Base de Datos

Para acceder a la base de datos MySQL, utiliza tu cliente MySQL preferido con las credenciales configuradas:

- **Host**: localhost (o tu servidor MySQL)
- **Puerto**: 3306 (por defecto)
- **Base de Datos**: hamburg_db
- **Usuario/Contraseña**: Los configurados en las variables de entorno

## Migración desde H2

Este proyecto ha sido migrado de H2 a MySQL. Los cambios principales incluyen:

- Configuración de MySQL en lugar de H2
- Variables de entorno para credenciales de base de datos
- Eliminación de la consola H2
- Mejoras en la seguridad del repositorio
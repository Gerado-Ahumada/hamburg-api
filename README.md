# Hamburg Backend

Proyecto backend desarrollado en Java 17 con Spring Boot que implementa un módulo de login utilizando una base de datos temporal (H2) con datos ficticios.

## Características

- Desarrollado con Java 17 y Spring Boot 3.1.5
- Autenticación mediante JWT (JSON Web Token)
- Base de datos H2 en memoria para desarrollo
- Implementación de Spring Security
- Datos ficticios precargados

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

## Configuración

El proyecto viene preconfigurado con una base de datos H2 en memoria y un usuario administrador:

- **Usuario**: admin
- **Contraseña**: admin

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

- `HAMBURG_APP_JWT_SECRET`: Clave secreta para la generación de tokens JWT (mínimo 256 bits)
- `HAMBURG_APP_JWT_EXPIRATION_MS`: Tiempo de expiración del token JWT en milisegundos
- `SPRING_PROFILES_ACTIVE`: Perfil activo de Spring (dev, prod, etc.)

### Personalización de Variables de Entorno

Puede personalizar las variables de entorno editando los scripts de despliegue o configurándolas directamente en su entorno antes de ejecutar la aplicación:

#### Windows
```powershell
set HAMBURG_APP_JWT_SECRET=su_clave_secreta_personalizada
set HAMBURG_APP_JWT_EXPIRATION_MS=3600000
./deploy-hamburg-api.bat
```

#### Linux/Mac
```bash
export HAMBURG_APP_JWT_SECRET="su_clave_secreta_personalizada"
export HAMBURG_APP_JWT_EXPIRATION_MS="3600000"
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

## Consola H2

Puedes acceder a la consola de la base de datos H2 en:

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:hamburgdb`
- Usuario: `sa`
- Contraseña: (dejar en blanco)

## Integración con Base de Datos Real

Para integrar con una base de datos real, modifica el archivo `application.properties` con la configuración de tu base de datos y actualiza las dependencias en `pom.xml` según sea necesario.
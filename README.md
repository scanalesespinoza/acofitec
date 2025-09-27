# Acofitec Web

Sitio base de la comunidad Acofitec construido a partir del proyecto EventFlow. Incluye un
layout mínimo con navegación, páginas públicas iniciales y configuración lista para
trabajar con Quarkus 3.x y Java 21.

## Requisitos

- [JDK 21](https://adoptium.net/) disponible en tu `PATH` (variable `JAVA_HOME` apuntando a JDK 21).
- Maven Wrapper incluido en el repositorio (`./mvnw`).
- (Opcional) Variables de entorno para configuración OIDC.

## Ejecución en desarrollo

```bash
./mvnw quarkus:dev
```

El servidor quedará disponible en [http://localhost:8080](http://localhost:8080). Revisa las rutas
principales:

- `/` Página de inicio con hero, CTA y navegación.
- `/sobre` Página con Visión y Misión de Acofitec.

## Build del proyecto

Genera el paquete ejecutable omitiendo tests (no se incluyen aún):

```bash
./mvnw -DskipTests package
```

El artefacto quedará en `target/` listo para ejecutarse con `java -jar`.

## Configuración por variables de entorno

El archivo `src/main/resources/application.properties` está preparado para leer variables de entorno
que podrás definir según tu entorno:

- `HTTP_PORT` (por defecto `8080`).
- `OIDC_CLIENT_ID` (por defecto `dev`).
- `OIDC_CLIENT_SECRET` (por defecto `dev`).
- `OIDC_AUTH_SERVER_URL` (por defecto `http://localhost/oidc`).

Estas variables sirven como placeholders para futuras integraciones OIDC.

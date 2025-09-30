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

### Identidad simulada en desarrollo

En los perfiles `dev` y `test` no es necesario tener un servidor OIDC en ejecución.
El proyecto expone una identidad mock configurable que emula la información básica
que entregaría el proveedor real.

Valores por defecto en modo `dev`:

- Usuario: `developer`
- Roles: `admin`, `user`
- Email: `developer@acofitec.local`
- Subject: `00000000-0000-0000-0000-000000000001`

Puedes modificar la identidad en caliente a través de los siguientes headers
HTTP al invocar los endpoints protegidos:

- `X-Mock-User`: nombre de usuario a utilizar.
- `X-Mock-Roles`: lista de roles separada por comas.
- `Authorization`: token (se acepta tanto un valor libre como `Bearer <token>`).

En caso de necesitar otros atributos puedes ajustarlos en `application.properties`
utilizando el prefijo `acofitec.security.mock`.

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

## SEO v1

El layout principal (`src/main/resources/templates/layout.qute.html`) incluye metadatos
SEO mínimos con valores por defecto:

- `title`: `Acofitec — Comunidad, Eventos y Open Source`.
- `description`: `Impulsamos la innovación con comunidad y colaboración.`
- `canonical`: `/`.
- `ogImage`: `/brand/acofitec-logo.svg`.

Puedes sobreescribir cualquiera de estas variables desde cada vista Qute con un bloque
`{#let}` al inicio del template o enviándolas desde el controlador en el modelo. Ejemplo
directo en el template:

```qute-html
{#let
  title='Acofitec — Comunidad, Eventos y Open Source'
  description='Conectamos personas, eventos y proyectos para aprender, crear y crecer.'
  canonical='/'
  ogImage='/brand/acofitec-logo.svg'
/}
<!-- resto del template -->
{/let}
```

Los archivos estáticos `sitemap.xml` y `robots.txt` se encuentran en
`src/main/resources/META-INF/resources/` y se sirven desde `/sitemap.xml` y
`/robots.txt` respectivamente. Por defecto se reutiliza `brand/acofitec-logo.svg`
como imagen OpenGraph/Twitter; puedes reemplazarla aportando una ruta diferente
mediante la variable `ogImage` o incorporando un asset propio en el mismo
directorio (agregándolo fuera de este PR si necesitas un PNG dedicado).

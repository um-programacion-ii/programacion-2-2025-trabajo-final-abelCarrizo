# 👨‍💻 Backend 

## 📋 Descripción

Backend desarrollado en **Spring Boot** con **arquitectura hexagonal** que permite:
- Registro y autenticación de usuarios
- Consulta de eventos desde el servicio de Cátedra
- Visualización de mapas de asientos
- Bloqueo y venta de asientos
- Gestión de sesiones de compra
- Sincronización automática via Kafka (a través del Proxy)

---

## 🏗️ Arquitectura

El proyecto sigue una **arquitectura hexagonal (puertos y adaptadores)**:

```
┌─────────────────────────────────────────────────────────────┐
│                     INFRAESTRUCTURA                          │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                    APLICACIÓN                        │    │
│  │  ┌─────────────────────────────────────────────┐    │    │
│  │  │                  DOMINIO                     │    │    │
│  │  │  Usuario, Evento, Asiento, Venta, Sesion    │    │    │
│  │  └─────────────────────────────────────────────┘    │    │
│  │  Puertos IN: 4 casos de uso                         │    │
│  │  Puertos OUT: 6 interfaces de dependencias          │    │
│  └─────────────────────────────────────────────────────┘    │
│  Adaptadores IN: Controllers REST (5)                       │
│  Adaptadores OUT: Persistencia, Cátedra, Proxy              │
└─────────────────────────────────────────────────────────────┘
```

### Capas

| Capa | Responsabilidad |
|------|-----------------|
| **Domain** | Modelos de negocio puros (sin dependencias externas) |
| **Application** | Casos de uso, puertos de entrada y salida |
| **Infrastructure** | Controllers, repositorios, clientes HTTP, configuración |

---

## 🛠️ Tecnologías

| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 17+ | Lenguaje principal |
| Spring Boot | 4.0.0 | Framework backend |
| Spring Security | - | Autenticación y autorización |
| Spring Data JPA | - | Persistencia |
| MariaDB | 10.x | Base de datos |
| JWT (jjwt) | 0.12.6 | Tokens de autenticación |
| Lombok | - | Reducción de boilerplate |
| Maven | - | Gestión de dependencias |

---

## 📁 Estructura del Proyecto

```
src/main/java/com/abel/eventos/
├── EventosApplication.java
├── domain/
│   └── model/                    # 9 modelos de dominio
│       ├── Usuario.java
│       ├── Evento.java
│       ├── EventoTipo.java
│       ├── Integrante.java
│       ├── Asiento.java
│       ├── AsientoEstado.java
│       ├── Venta.java
│       ├── Sesion.java
│       └── SesionEstado.java
├── application/
│   ├── port/
│   │   ├── in/                   # 4 casos de uso
│   │   │   ├── AutenticacionUseCase.java
│   │   │   ├── GestionEventosUseCase.java
│   │   │   ├── RealizarVentaUseCase.java
│   │   │   └── GestionSesionUseCase.java
│   │   └── out/                  # 6 puertos de salida
│   │       ├── UsuarioRepositoryPort.java
│   │       ├── EventoRepositoryPort.java
│   │       ├── VentaRepositoryPort.java
│   │       ├── SesionRepositoryPort.java
│   │       ├── CatedraServicePort.java
│   │       └── ProxyServicePort.java
│   └── service/                  # 4 servicios de aplicación
│       ├── AutenticacionService.java
│       ├── GestionEventosService.java
│       ├── GestionSesionService.java
│       └── RealizarVentaService.java
└── infrastructure/
    ├── config/                   # 5 configuraciones
    │   ├── JwtService.java
    │   ├── JwtAuthenticationFilter.java
    │   ├── CustomUserDetailsService.java
    │   ├── SecurityConfig.java
    │   └── RestTemplateConfig.java
    └── adapter/
        ├── in/web/               # 5 Controllers + 14 DTOs
        │   ├── dto/              # 14 DTOs
        │   ├── AuthController.java
        │   ├── EventoController.java
        │   ├── SesionController.java
        │   ├── VentaController.java
        │   └── InternalController.java  # ← Nuevo v1.4
        └── out/
            ├── persistence/      # JPA
            │   ├── entity/       # 9 entidades
            │   ├── repository/   # 7 repositorios
            │   └── adapter/      # 4 adaptadores
            ├── catedra/          # Cliente API Cátedra
            │   ├── dto/          # 9 DTOs
            │   └── CatedraServiceAdapter.java
            └── proxy/            # Cliente Proxy
                ├── dto/          # 4 DTOs
                └── ProxyServiceAdapter.java
```

---

## ⚙️ Configuración

### 1. Requisitos Previos

- Java 17 o superior
- MariaDB 10.x
- Maven 3.x
- Conexión a red ZeroTier (para acceso a Cátedra)
- Servicio Proxy corriendo (para consultas de asientos)

### 2. Crear Base de Datos

```sql
CREATE DATABASE eventos_BD;
CREATE USER 'abel'@'localhost' IDENTIFIED BY 'abel.DB';
GRANT ALL PRIVILEGES ON eventos_BD.* TO 'abel'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configurar Variables de Entorno

Crear archivo `.env` en la raíz del proyecto:

```properties
DB_NAME=eventos_BD
DB_USERNAME=nombre_usuario
DB_PASSWORD=contraseña
JWT_SECRET=MiClaveSecretaSuperLargaParaJWTQueDebeSerDeAlMenos64CaracteresParaQueSeaSegura2025
JWT_EXPIRATION=1800000
CATEDRA_TOKEN=tu_token_de_catedra_aqui
PROXY_URL=http://localhost:8081
```

### 4. Obtener Token de Cátedra

```bash
curl -X POST http://192.168.194.250:8080/api/v1/agregar_usuario \
  -H "Content-Type: application/json" \
  -d '{
    "username": "tu_usuario",
    "password": "tu_password",
    "firstName": "Nombre",
    "lastName": "Apellido",
    "email": "email@ejemplo.com",
    "nombreAlumno": "Nombre Completo",
    "descripcionProyecto": "Sistema de eventos"
  }'
```

Guardar el token obtenido en `CATEDRA_TOKEN` del archivo `.env`.

### 5. application.properties

```properties
# Nombre de la aplicacion
spring.application.name=eventos

# Base de datos
spring.datasource.url=jdbc:mariadb://localhost:3306/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Puerto
server.port=8080

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION}

# API Cátedra
catedra.api.base-url=http://192.168.194.250:8080
catedra.api.token=${CATEDRA_TOKEN}

# Proxy
proxy.url=${PROXY_URL:http://localhost:8081}

# Opcional: desactivar advertencias
spring.jpa.open-in-view=false
```

---

## 🚀 Ejecución

### Orden de Inicio

1. **Primero:** MariaDB
2. **Segundo:** Proxy (puerto 8081)
3. **Tercero:** Backend (puerto 8080)

### Desarrollo

```bash
# Cargar variables de entorno e iniciar
source .env && ./mvnw spring-boot:run
```

### Producción

```bash
# Compilar
./mvnw clean package -DskipTests

# Ejecutar JAR
java -jar target/eventos-0.0.1-SNAPSHOT.jar
```

El servidor iniciará en `http://localhost:8080`

---

## 🔌 API Endpoints

### Autenticación (públicos)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/auth/registro` | Registrar nuevo usuario |
| `POST` | `/api/auth/login` | Iniciar sesión |

### Eventos (requieren JWT)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/eventos` | Listar todos los eventos |
| `GET` | `/api/eventos/{id}` | Detalle de un evento |
| `GET` | `/api/eventos/{id}/asientos` | Mapa de asientos (via Proxy) |

### Sesiones (requieren JWT)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/sesion` | Obtener sesión actual |
| `POST` | `/api/sesion/iniciar` | Iniciar proceso de compra |
| `POST` | `/api/sesion/finalizar` | Finalizar sesión |

### Ventas (requieren JWT)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/ventas/seleccionar` | Seleccionar asientos (máx. 4) |
| `POST` | `/api/ventas/bloquear` | Bloquear asientos seleccionados |
| `POST` | `/api/ventas/asignar-personas` | Asignar nombres a asientos |
| `POST` | `/api/ventas/confirmar` | Confirmar compra |
| `POST` | `/api/ventas/cancelar` | Cancelar proceso |

### Internos (para Proxy)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/internal/sync` | Recibir notificaciones de cambios |

---

## 📝 Ejemplos de Uso

### Registro de Usuario

```bash
curl -X POST http://localhost:8080/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{
    "username": "juan",
    "password": "password123",
    "email": "juan@ejemplo.com",
    "nombre": "Juan",
    "apellido": "Pérez"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "juan",
    "password": "password123"
  }'
```

### Listar Eventos

```bash
curl -X GET http://localhost:8080/api/eventos \
  -H "Authorization: Bearer <tu_token>"
```

### Obtener Mapa de Asientos

```bash
curl -X GET http://localhost:8080/api/eventos/1/asientos \
  -H "Authorization: Bearer <tu_token>"
```

### Flujo Completo de Compra

```bash
# 1. Iniciar sesión de compra
curl -X POST http://localhost:8080/api/sesion/iniciar \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"eventoId": 1}'

# 2. Seleccionar asientos
curl -X POST http://localhost:8080/api/ventas/seleccionar \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "eventoId": 1,
    "asientos": [
      {"fila": 1, "columna": 1},
      {"fila": 1, "columna": 2}
    ]
  }'

# 3. Bloquear asientos
curl -X POST http://localhost:8080/api/ventas/bloquear \
  -H "Authorization: Bearer <token>"

# 4. Asignar personas
curl -X POST http://localhost:8080/api/ventas/asignar-personas \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "asientos": [
      {"fila": 1, "columna": 1, "persona": "Juan Pérez"},
      {"fila": 1, "columna": 2, "persona": "María García"}
    ]
  }'

# 5. Confirmar venta
curl -X POST http://localhost:8080/api/ventas/confirmar \
  -H "Authorization: Bearer <token>"
```

---

## 🔐 Seguridad

- **Autenticación:** JWT (JSON Web Tokens)
- **Expiración de token:** 30 minutos (configurable)
- **Endpoints públicos:** `/api/auth/**` y `/api/internal/**`
- **Encriptación de contraseñas:** BCrypt

---

## 🗄️ Base de Datos

### Tablas Principales

| Tabla | Descripción | @GeneratedValue |
|-------|-------------|-----------------|
| `usuarios` | Usuarios del sistema | ✅ Sí |
| `eventos` | Cache de eventos de Cátedra | ❌ No (ID de Cátedra) |
| `evento_tipos` | Tipos de evento | ✅ Sí |
| `integrantes` | Presentadores de eventos | ✅ Sí |
| `sesiones` | Sesiones de compra activas | ✅ Sí |
| `ventas` | Registro de ventas | ✅ Sí |

### Notas Importantes

- **EventoEntity:** El ID viene de Cátedra (sin `@GeneratedValue`)
- **EventoTipoEntity:** Cátedra NO envía ID, se busca por `nombre`
- **IntegranteEntity:** Cátedra NO envía ID, se busca por `nombre` + `apellido`
- **SesionEntity:** Solo guarda `eventoId` como Long (sin relación JPA)

---

## 🔄 Flujo de Compra

```
1. Login
   └─> POST /api/auth/login

2. Ver eventos (desde Cátedra)
   └─> GET /api/eventos

3. Seleccionar evento
   └─> GET /api/eventos/{id}
   └─> POST /api/sesion/iniciar

4. Ver asientos disponibles (via Proxy → Redis)
   └─> GET /api/eventos/{id}/asientos

5. Seleccionar asientos (máx. 4)
   └─> POST /api/ventas/seleccionar
   └─> Validaciones: rango, disponibilidad, evento coincide

6. Bloquear asientos (5 min) → Cátedra
   └─> POST /api/ventas/bloquear

7. Asignar nombres
   └─> POST /api/ventas/asignar-personas
   └─> Validación: asientos coinciden con selección

8. Confirmar compra → Cátedra + BD local
   └─> POST /api/ventas/confirmar
```

---

## 🌐 Integración con Servicios Externos

### API de Cátedra (Directa)

| Endpoint | Uso |
|----------|-----|
| `GET /api/endpoints/v1/eventos-resumidos` | Listar eventos |
| `GET /api/endpoints/v1/eventos` | Eventos completos (sincronización) |
| `GET /api/endpoints/v1/evento/{id}` | Detalle de evento |
| `POST /api/endpoints/v1/bloquear-asientos` | Bloquear asientos |
| `POST /api/endpoints/v1/realizar-venta` | Confirmar venta |

### Servicio Proxy (Indirecta)

| Operación | Endpoint Proxy | Fuente Real |
|-----------|----------------|-------------|
| Obtener asientos ocupados | `GET /api/proxy/eventos/{id}/asientos` | Redis Cátedra |
| Verificar disponibilidad | `POST /api/proxy/eventos/{id}/verificar` | Redis Cátedra |

### Sincronización (Kafka → Proxy → Backend)

1. Cátedra publica cambio en Kafka
2. Proxy recibe y llama a `POST /api/internal/sync`
3. Backend ejecuta `sincronizarEventos()` → actualiza cache local

---

## ✅ Validaciones Implementadas (v1.4)

| Validación | Descripción |
|------------|-------------|
| Máximo 4 asientos | No se pueden seleccionar más de 4 |
| Rango de filas/columnas | Debe estar dentro de las dimensiones del evento |
| Evento coincide con sesión | No se puede seleccionar asientos de otro evento |
| Asientos coinciden | Los asientos a asignar deben ser los seleccionados |
| Persona obligatoria | Cada asiento debe tener nombre asignado |
| Sesión activa requerida | Todas las operaciones de venta requieren sesión |
| Usuario del JWT | Se extrae correctamente del token (no hardcodeado) |

---

## 🧪 Pruebas

### Usando cURL

```bash
# Verificar que el servidor está corriendo
curl http://localhost:8080/api/auth/login -I
```

### Usando Bruno/Postman

Importar la colección de endpoints y configurar:
- `baseUrl`: `http://localhost:8080`
- `token`: Token obtenido del login

### Verificar Integración con Proxy

```bash
# Debe retornar mapa de asientos (requiere Proxy activo)
curl http://localhost:8080/api/eventos/1/asientos \
  -H "Authorization: Bearer <token>"
```

---

## ⚠️ Solución de Problemas

### Error: "No hay sesión activa"

**Causa:** Intentando operar sin iniciar sesión de compra.

**Solución:** Llamar primero a `POST /api/sesion/iniciar`

### Error: "El evento X no coincide con la sesión activa"

**Causa:** Intentando seleccionar asientos de un evento diferente al de la sesión.

**Solución:** Cancelar sesión e iniciar una nueva con el evento correcto.

### Error: "Fila X fuera de rango"

**Causa:** Seleccionando asiento que no existe en el evento.

**Solución:** Verificar dimensiones del evento (filaAsientos, columnaAsientos).

### Error al sincronizar eventos

**Causa:** Problema con EventoTipo o Integrante sin ID.

**Solución:** Verificar que `EventoTipoEntity` e `IntegranteEntity` tienen `@GeneratedValue`.

---

## 🔗 Componentes Relacionados

| Componente | Puerto | Descripción |
|------------|--------|-------------|
| **Backend** | 8080 | API principal para el cliente móvil |
| **Proxy** | 8081 | Intermediario con Kafka/Redis |
| **MariaDB** | 3306 | Base de datos local |
| **Cátedra API** | 192.168.194.250:8080 | Servicio externo de eventos |

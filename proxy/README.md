# 🔄 Proxy

Servicio intermediario que conecta el Backend con los servicios de Cátedra (Kafka y Redis), desarrollado como parte del proyecto final de Programación 2.

## 📋 Descripción

Proxy desarrollado en **Spring Boot** que actúa como intermediario entre el Backend del alumno y los servicios externos de la Cátedra:

- **Kafka:** Escucha notificaciones de cambios en eventos
- **Redis:** Consulta el estado de asientos (ocupados/bloqueados)
- **Backend:** Notifica cambios para sincronización

---

## 🏗️ Arquitectura

El Proxy es el **único servicio** con acceso directo a Kafka y Redis de la Cátedra:

```
┌─────────────────────────────────────────────────────────────────┐
│                    SERVICIOS DE CÁTEDRA                         │
│  ┌─────────────────┐              ┌─────────────────┐          │
│  │     Kafka       │              │     Redis       │          │
│  │  :9092          │              │    :6379        │          │
│  │  (eventos-      │              │  (evento_{id})  │          │
│  │  actualizacion) │              │                 │          │
│  └────────┬────────┘              └────────┬────────┘          │
└───────────│────────────────────────────────│───────────────────┘
            │                                │
            │         ┌──────────────┐       │
            └────────>│    PROXY     │<──────┘
                      │    :8081     │
                      └──────┬───────┘
                             │
                             │ POST /api/internal/sync
                             v
                      ┌──────────────┐
                      │   BACKEND    │
                      │    :8080     │
                      └──────────────┘
```

### Flujo de Datos

| Flujo | Descripción |
|-------|-------------|
| Kafka → Proxy | Recibe notificaciones de cambios en eventos |
| Proxy → Redis | Consulta asientos ocupados/bloqueados |
| Proxy → Backend | Notifica para sincronizar eventos |
| Backend → Proxy | Consulta disponibilidad de asientos |

---

## 🛠️ Tecnologías

| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 17+ | Lenguaje principal |
| Spring Boot | 4.0.0 | Framework backend |
| Spring Data Redis | - | Cliente Redis |
| Spring Kafka | - | Consumidor Kafka |
| Lombok | - | Reducción de boilerplate |
| Maven | - | Gestión de dependencias |

---

## 📁 Estructura del Proyecto

```
src/main/java/com/abel/proxy/
├── ProxyApplication.java
├── config/
│   ├── RedisConfig.java          # Configuración de RedisTemplate
│   ├── KafkaConfig.java          # Configuración del consumidor Kafka
│   └── RestTemplateConfig.java   # Cliente HTTP para Backend
├── dto/
│   ├── AsientoDTO.java           # Asiento con fila, columna, estado
│   ├── EventoAsientosDTO.java    # Lista de asientos por evento
│   ├── VerificacionRequest.java  # Request de verificación
│   └── NotificacionDTO.java      # Notificación al Backend
├── service/
│   ├── RedisService.java         # Consultas a Redis de Cátedra
│   ├── KafkaConsumerService.java # Escucha topic de Kafka
│   └── BackendNotifierService.java # Notifica cambios al Backend
└── controller/
    └── ProxyController.java      # API REST para el Backend
```

---

## ⚙️ Configuración

### 1. Requisitos Previos

- Java 17 o superior
- Maven 3.x
- Conexión a red ZeroTier (para acceso a Cátedra)
- Entrada en `/etc/hosts` para Kafka

### 2. Configurar /etc/hosts

**⚠️ IMPORTANTE:** Kafka devuelve el hostname "kafka" que debe resolverse:

```bash
sudo nano /etc/hosts

# Agregar esta línea:
192.168.194.250    kafka
```

### 3. Configurar Variables de Entorno

Crear archivo `.env` en la raíz del proyecto:

```properties
SERVER_PORT=8081
REDIS_HOST=192.168.194.250
REDIS_PORT=6379
KAFKA_BOOTSTRAP_SERVERS=192.168.194.250:9092
KAFKA_GROUP_ID=tu-nombre-apellido-proxy
BACKEND_URL=http://localhost:8080
```

**⚠️ IMPORTANTE:** El `KAFKA_GROUP_ID` debe ser **único por alumno** para evitar conflictos.

### 4. application.properties

```properties
spring.application.name=proxy

# Puerto del servidor
server.port=${SERVER_PORT:8081}

# REDIS - Conexión con Redis de Cátedra
spring.data.redis.host=${REDIS_HOST:192.168.194.250}
spring.data.redis.port=${REDIS_PORT:6379}
spring.data.redis.timeout=2000ms

# KAFKA - Conexión con Kafka de Cátedra
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:192.168.194.250:9092}
spring.kafka.consumer.group-id=${KAFKA_GROUP_ID:nombre-apellido-proxy}
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.listener.auto-startup=true

# BACKEND
backend.url=${BACKEND_URL:http://localhost:8080}

# LOGGING
logging.level.com.abel.proxy=DEBUG
logging.level.org.apache.kafka=WARN
```

---

## 🚀 Ejecución

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
java -jar target/proxy-0.0.1-SNAPSHOT.jar
```

El servidor iniciará en `http://localhost:8081`

**⚠️ IMPORTANTE:** El Proxy debe iniciarse **antes o junto con** el Backend.

---

## 🔌 API Endpoints

### Endpoints REST

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/proxy/health` | Health check del servicio |
| `GET` | `/api/proxy/eventos/{id}/asientos` | Obtener asientos ocupados/bloqueados |
| `POST` | `/api/proxy/eventos/{id}/verificar` | Verificar disponibilidad de asientos |

---

## 📝 Ejemplos de Uso

### Health Check

```bash
curl http://localhost:8081/api/proxy/health
```

**Respuesta:**
```json
{
  "status": "UP",
  "service": "proxy"
}
```

### Obtener Asientos Ocupados

```bash
curl http://localhost:8081/api/proxy/eventos/1/asientos
```

**Respuesta:**
```json
{
  "eventoId": 1,
  "asientos": [
    {
      "fila": 2,
      "columna": 3,
      "estado": "Vendido"
    },
    {
      "fila": 2,
      "columna": 4,
      "estado": "Bloqueado",
      "expira": "2025-12-15T20:30:00Z"
    }
  ]
}
```

**Nota:** Si no hay asientos ocupados, la lista estará vacía (todos libres).

### Verificar Disponibilidad

```bash
curl -X POST http://localhost:8081/api/proxy/eventos/1/verificar \
  -H "Content-Type: application/json" \
  -d '{
    "asientos": [
      {"fila": 1, "columna": 1},
      {"fila": 1, "columna": 2}
    ]
  }'
```

**Respuesta:**
```json
{
  "eventoId": 1,
  "disponible": true,
  "asientosConsultados": 2
}
```

---

## 🔄 Integración con Kafka

### Topic

- **Nombre:** `eventos-actualizacion`
- **Función:** Notifica cambios en eventos de Cátedra

### Comportamiento

1. Cátedra publica mensaje cuando hay cambios en eventos
2. Proxy recibe el mensaje via `@KafkaListener`
3. Proxy notifica al Backend via POST `/api/internal/sync`
4. Backend sincroniza eventos desde API de Cátedra

### Forzar Actualización (para pruebas)

```bash
curl -X POST "http://192.168.194.250:8080/api/endpoints/v1/forzar-actualizacion" \
  -H "Authorization: Bearer <CATEDRA_TOKEN>"
```

### Logs Esperados

```
=======================================================
  KafkaConsumerService INICIADO
  Escuchando topic: eventos-actualizacion
=======================================================

=======================================================
MENSAJE RECIBIDO DE KAFKA
Topic: eventos-actualizacion
Contenido: {"eventoId": 1, "tipo": "ACTUALIZADO", ...}
=======================================================
```

---

## 🗄️ Integración con Redis

### Formato de Claves

```
evento_{ID_EVENTO}
```

Ejemplo: `evento_1`, `evento_2`, etc.

### Estructura del Valor (JSON)

```json
{
  "eventoId": 1,
  "asientos": [
    {
      "fila": 1,
      "columna": 3,
      "estado": "Bloqueado",
      "expira": "2025-12-15T20:30:00Z"
    },
    {
      "fila": 2,
      "columna": 3,
      "estado": "Vendido"
    }
  ]
}
```

### Estados de Asientos

| Estado | Descripción |
|--------|-------------|
| `Bloqueado` | Temporalmente reservado (incluye campo `expira`) |
| `Vendido` | Permanentemente ocupado |
| *No presente* | Asiento **disponible** |

**⚠️ IMPORTANTE:** Redis de Cátedra **solo almacena asientos ocupados/bloqueados**. Si un asiento no está en Redis, está **LIBRE**.

---

## 🔗 Comunicación con Backend

### Notificación de Cambios

Cuando el Proxy recibe un mensaje de Kafka, notifica al Backend:

```
POST http://localhost:8080/api/internal/sync
Content-Type: application/json

{
  "tipo": "EVENTO_ACTUALIZADO",
  "mensaje": "<contenido del mensaje Kafka>",
  "timestamp": "2025-12-15T16:30:00Z"
}
```

### Consultas del Backend

El Backend consulta al Proxy para obtener información de asientos:

| Operación | Endpoint Proxy |
|-----------|----------------|
| Obtener asientos ocupados | `GET /api/proxy/eventos/{id}/asientos` |
| Verificar disponibilidad | `POST /api/proxy/eventos/{id}/verificar` |

---

## 🔄 Flujo Completo

```
┌─────────────────────────────────────────────────────────────┐
│ 1. SINCRONIZACIÓN (Kafka → Proxy → Backend)                 │
│                                                             │
│    Cátedra ──(Kafka)──> Proxy ──(HTTP)──> Backend           │
│      │                    │                  │              │
│      │  "evento_update"   │  POST /sync      │  sincronizar │
│      └────────────────────┴──────────────────┴──────────────│
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ 2. CONSULTA DE ASIENTOS (Backend → Proxy → Redis)           │
│                                                             │
│    Backend ──(HTTP)──> Proxy ──(Redis)──> Cátedra           │
│       │                  │                   │              │
│       │  GET /asientos   │  GET evento_1     │              │
│       └──────────────────┴───────────────────┴──────────────│
└─────────────────────────────────────────────────────────────┘
```

---

## 🧪 Pruebas

### Verificar Conexión a Redis

```bash
# Desde el Proxy, verificar que puede conectar
curl http://localhost:8081/api/proxy/eventos/1/asientos
```

### Verificar Conexión a Kafka

Revisar los logs del Proxy al iniciar:
```
KafkaConsumerService INICIADO
Escuchando topic: eventos-actualizacion
```

### Verificar Comunicación con Backend

1. Iniciar Backend en puerto 8080
2. Forzar actualización en Cátedra
3. Verificar logs del Proxy (mensaje recibido)
4. Verificar logs del Backend (sincronización ejecutada)

---

## ⚠️ Solución de Problemas

### Error: "Connection refused" a Kafka

**Causa:** El hostname "kafka" no se resuelve.

**Solución:**
```bash
sudo nano /etc/hosts
# Agregar: 192.168.194.250    kafka
```

### Error: "Connection refused" a Redis

**Causa:** Redis de Cátedra no accesible.

**Solución:**
1. Verificar conexión ZeroTier: `zerotier-cli status`
2. Verificar ping: `ping 192.168.194.250`

### Error: "Connection refused" al notificar Backend

**Causa:** Backend no está corriendo.

**Solución:**
1. Iniciar Backend primero
2. Verificar que está en puerto 8080

### Kafka no recibe mensajes

**Causa:** Group ID duplicado con otro alumno.

**Solución:**
- Usar un `KAFKA_GROUP_ID` único (ej: `abel-carrizo-proxy-v2`)

---

## 🔗 Componentes Relacionados

| Componente | Puerto | Descripción |
|------------|--------|-------------|
| **Backend** | 8080 | API principal para el cliente móvil |
| **Proxy** | 8081 | Intermediario con Kafka/Redis |
| **Cátedra API** | 192.168.194.250:8080 | Servicio externo de eventos |
| **Kafka** | 192.168.194.250:9092 | Notificaciones de cambios |
| **Redis** | 192.168.194.250:6379 | Estado de asientos |

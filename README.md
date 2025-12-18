[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/IEOUmR9z)

# 🎫 Trabajo Final - Sistema de Eventos

Sistema para registro de asistencia a eventos con selección de asientos, desarrollado como trabajo final de la materia **Programación 2**.

---

## 👨‍🎓 Información del Estudiante

| Campo | Información |
|-------|-------------|
| **Nombre** | Abel Carrizo |
| **Legajo** | 59164 |
| **Materia** | Programación 2 |

---

## 📋 Descripción General

Este proyecto implementa un sistema completo de venta de entradas para eventos, permitiendo:

- 🔐 Registro y autenticación de usuarios
- 📅 Visualización de eventos disponibles
- 🪑 Selección interactiva de asientos (hasta 4 por compra)
- 👥 Asignación de nombres a cada asiento
- 🛒 Confirmación y registro de ventas
- 🔄 Sincronización en tiempo real con servicios externos

El sistema se integra con servicios de la cátedra (Kafka para notificaciones, Redis para estado de asientos) y cumple con la arquitectura distribuida especificada en la consigna.

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────────┐
│                    SERVICIOS DE CÁTEDRA                         │
│  ┌─────────────────┐              ┌─────────────────┐          │
│  │   API Cátedra   │              │                 │          │
│  │     :8080       │              │                 │          │
│  └────────┬────────┘              │                 │          │
│           │                       │                 │          │
│  ┌────────┴────────┐              │                 │          │
│  │     Kafka       │              │     Redis       │          │
│  │     :9092       │              │     :6379       │          │
│  └────────┬────────┘              └────────┬────────┘          │
└───────────│────────────────────────────────│───────────────────┘
            │                                │
            │  ┌──────────────────────────┐  │
            └─►│         PROXY            │◄─┘
               │        :8081             │
               └────────────┬─────────────┘
                            │
                            │ HTTP
                            ▼
               ┌──────────────────────────┐
               │        BACKEND           │
               │        :8080             │
               └────────────┬─────────────┘
                            │
              ┌─────────────┼─────────────┐
              │             │             │
              ▼             │             ▼
        ┌──────────┐        │       ┌──────────┐
        │ MariaDB  │        │       │  Mobile  │
        │  :3306   │        │       │   App    │
        └──────────┘        │       └──────────┘
                            │
                            ▼
                    ┌──────────────┐
                    │   Usuario    │
                    │    Final     │
                    └──────────────┘
```

### Componentes

| Componente | Tecnología | Puerto | Descripción |
|------------|------------|--------|-------------|
| **Backend** | Spring Boot 4.0 | 8080 | API REST principal, arquitectura hexagonal |
| **Proxy** | Spring Boot 4.0 | 8081 | Intermediario con Kafka y Redis de Cátedra |
| **Mobile** | Kotlin Multiplatform | - | Cliente móvil con Compose Multiplatform |
| **MariaDB** | MariaDB 10.x | 3306 | Base de datos local |

---

## 📁 Estructura del Proyecto

```
proyecto/
├── backend/                    # API REST principal
│   ├── src/
│   ├── pom.xml
│   └── README.md              # 📖 Documentación del Backend
│
├── proxy/                      # Servicio intermediario
│   ├── src/
│   ├── pom.xml
│   └── README.md              # 📖 Documentación del Proxy
│
├── mobile/                     # Aplicación móvil
│   ├── composeApp/
│   ├── build.gradle.kts
│   └── README.md              # 📖 Documentación del Mobile
│
├── collections/                # Colecciones de API para pruebas
│   └── Bruno/                 # Endpoints para Bruno API Client
│
└── README.md                   # 📖 Este archivo
```

---

## 📖 Documentación por Componente

Cada componente cuenta con su propia documentación detallada:

| Componente | README | Contenido |
|------------|--------|-----------|
| **Backend** | [`backend/README.md`](backend/README.md) | Arquitectura hexagonal, endpoints, configuración, flujo de compra |
| **Proxy** | [`proxy/README.md`](proxy/README.md) | Integración Kafka/Redis, configuración, flujo de sincronización |
| **Mobile** | [`mobile/README.md`](mobile/README.md) | Pantallas, navegación, configuración Android, arquitectura MVVM |

---

## 🧪 Colección de Endpoints (Bruno API)

La carpeta `collections/` contiene las colecciones de endpoints utilizadas para probar el sistema con **Bruno API Client**.

### Contenido

```
collections/
└── Bruno/
    ├── Autenticación/
    ├── Eventos/
    ├── Health Checks/
    ├── Proxy Directo/
    ├── Sesión/
    ├── Sincronización/
    └── Ventas/
```

### Uso con Bruno

1. Descargar [Bruno API Client](https://www.usebruno.com/)
2. Abrir Bruno y seleccionar "Open Collection"
3. Navegar a `proyecto/collections/Bruno`
4. Configurar variables de entorno:
   - `baseUrl`: `http://localhost:8080`
   - `token`: Token obtenido del login

---

## 🚀 Guía de Ejecución Rápida

### Requisitos Previos

- Java 17+
- MariaDB 10.x
- Maven 3.x
- Android Studio (para mobile)
- Conexión ZeroTier (para acceso a Cátedra)

### Orden de Inicio

```bash
# 1. Iniciar MariaDB
sudo systemctl start mariadb

# 2. Iniciar Proxy (Terminal 1)
cd proxy
source .env && ./mvnw spring-boot:run

# 3. Iniciar Backend (Terminal 2)
cd backend
source .env && ./mvnw spring-boot:run

# 4. Iniciar Mobile (Android Studio)
# Abrir proyecto mobile/ y ejecutar en emulador
```

### Verificación

```bash
# Verificar Backend
curl http://localhost:8080/api/auth/login -I

# Verificar Proxy
curl http://localhost:8081/api/proxy/health

# Respuesta esperada del Proxy:
# {"status":"UP","service":"proxy"}
```

---

## 🔄 Flujo Principal del Sistema

```
┌──────────────────────────────────────────────────────────────────────┐
│                      FLUJO DE COMPRA DE ENTRADAS                     │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  1. LOGIN                                                            │
│     Mobile ──► Backend ──► Validar credenciales ──► JWT Token        │
│                                                                      │
│  2. LISTAR EVENTOS                                                   │
│     Mobile ──► Backend ──► API Cátedra ──► Lista de eventos          │
│                                                                      │
│  3. VER ASIENTOS                                                     │
│     Mobile ──► Backend ──► Proxy ──► Redis Cátedra ──► Mapa          │
│                                                                      │
│  4. SELECCIONAR (1-4 asientos)                                       │
│     Mobile ──► Backend ──► Guardar en sesión                         │
│                                                                      │
│  5. BLOQUEAR (5 minutos)                                             │
│     Mobile ──► Backend ──► API Cátedra ──► Bloqueo temporal          │
│                                                                      │
│  6. ASIGNAR NOMBRES                                                  │
│     Mobile ──► Backend ──► Guardar en sesión                         │
│                                                                      │
│  7. CONFIRMAR COMPRA                                                 │
│     Mobile ──► Backend ──► API Cátedra + BD Local ──► ¡Éxito!        │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

---

## ⚙️ Configuración de Red

### ZeroTier

El proyecto requiere conexión a la red ZeroTier de la cátedra para acceder a:

| Servicio | Dirección | Puerto |
|----------|-----------|--------|
| API Cátedra | 192.168.194.250 | 8080 |
| Kafka | 192.168.194.250 | 9092 |
| Redis | 192.168.194.250 | 6379 |

### /etc/hosts (Requerido para Kafka)

```bash
# Agregar esta línea para resolver el hostname de Kafka
192.168.194.250    kafka
```

---

## 📝 Notas Importantes

1. **Orden de inicio**: El Proxy debe estar activo antes de que el Backend consulte asientos

2. **Token de Cátedra**: Debe obtenerse registrándose en el servicio de la cátedra y guardarse en el `.env` del Backend

3. **Group ID de Kafka**: Debe ser único por alumno para evitar conflictos con otros estudiantes

4. **URL del Mobile**: Por defecto usa `10.0.2.2:8080` (emulador Android). Para dispositivo físico, cambiar por IP de red local

---

## 🔗 Enlaces Útiles

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Bruno API Client](https://www.usebruno.com/)
- [ZeroTier](https://www.zerotier.com/)

---

## 📄 Licencia

Este proyecto fue desarrollado como trabajo final académico para la materia Programación 2.

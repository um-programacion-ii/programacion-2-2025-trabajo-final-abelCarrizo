# 📱 Cliente Móvil

Aplicación móvil desarrollada en **Kotlin Multiplatform** con **Compose Multiplatform** para el sistema de registro de asistencia a eventos.

## 📋 Descripción

Esta aplicación permite a los usuarios:
- Registrarse e iniciar sesión
- Explorar eventos disponibles
- Seleccionar asientos en un mapa interactivo
- Ingresar datos de los asistentes
- Confirmar la compra de entradas

## 🛠️ Tecnologías Utilizadas

| Tecnología | Versión | Uso |
|------------|---------|-----|
| Kotlin | 2.0.21 | Lenguaje principal |
| Compose Multiplatform | 1.7.1 | UI declarativa |
| Ktor Client | 3.0.3 | Cliente HTTP |
| Kotlinx Serialization | 1.7.3 | Serialización JSON |
| Navigation Compose | 2.8.0 | Navegación entre pantallas |
| Lifecycle ViewModel | 2.8.4 | Manejo de estado |

## 📁 Estructura del Proyecto

```
mobile/
├── composeApp/
│   └── src/
│       ├── commonMain/kotlin/org/abel/mobile/
│       │   ├── data/                    # Capa de datos
│       │   │   ├── api/
│       │   │   │   └── EventosApiClient.kt    # Cliente HTTP
│       │   │   └── model/               # DTOs (10 archivos)
│       │   │       ├── LoginRequest.kt
│       │   │       ├── LoginResponse.kt
│       │   │       ├── EventoResumen.kt
│       │   │       ├── EventoDetalle.kt
│       │   │       ├── Asiento.kt
│       │   │       ├── SesionResponse.kt
│       │   │       ├── VentaResponse.kt
│       │   │       ├── IniciarSesionRequest.kt
│       │   │       ├── SeleccionarAsientosRequest.kt
│       │   │       ├── AsignarPersonasRequest.kt
│       │   │       └── MensajeResponse.kt
│       │   │
│       │   ├── ui/                      # Capa de presentación
│       │   │   ├── navigation/
│       │   │   │   └── AppNavigation.kt       # Rutas y NavHost
│       │   │   └── screens/
│       │   │       ├── login/
│       │   │       │   ├── LoginScreen.kt
│       │   │       │   └── LoginViewModel.kt
│       │   │       ├── registro/
│       │   │       │   ├── RegistroScreen.kt
│       │   │       │   └── RegistroViewModel.kt
│       │   │       ├── eventos/
│       │   │       │   ├── EventosListScreen.kt
│       │   │       │   └── EventosViewModel.kt
│       │   │       ├── detalle/
│       │   │       │   ├── EventoDetalleScreen.kt
│       │   │       │   └── EventoDetalleViewModel.kt
│       │   │       ├── asientos/
│       │   │       │   ├── SeleccionAsientosScreen.kt
│       │   │       │   └── SeleccionAsientosViewModel.kt
│       │   │       ├── datos/
│       │   │       │   ├── DatosPersonalesScreen.kt
│       │   │       │   └── DatosPersonalesViewModel.kt
│       │   │       └── confirmacion/
│       │   │           ├── ConfirmacionScreen.kt
│       │   │           └── ConfirmacionViewModel.kt
│       │   │
│       │   └── util/
│       │       └── SessionManager.kt    # Manejo de sesión JWT
│       │
│       └── androidMain/                 # Código específico Android
│
├── build.gradle.kts                     # Configuración Gradle
└── README.md                            # Este archivo
```

## 🖥️ Pantallas

### 1. Login (`/login`)
- Formulario de autenticación
- Validación de campos (usuario mín. 3 chars, contraseña mín. 4 chars)
- Enlace a registro

### 2. Registro (`/registro`)
- Formulario de creación de cuenta
- Campos: usuario, email, nombre, apellido, contraseña, confirmar contraseña
- Validación en tiempo real
- Auto-login después del registro exitoso

### 3. Lista de Eventos (`/eventos`)
- Lista scrolleable de eventos disponibles
- Muestra: título, fecha, precio, tipo de evento
- Botón de cerrar sesión
- Click para ver detalle

### 4. Detalle de Evento (`/detalle/{eventoId}`)
- Información completa del evento
- Imagen, descripción, ubicación
- Lista de presentadores
- Disponibilidad de asientos
- Botón "Seleccionar Asientos"

### 5. Selección de Asientos (`/asientos/{eventoId}`)
- Mapa visual de asientos (grid)
- Código de colores:
  - 🟢 Verde: Disponible
  - 🔵 Azul: Seleccionado por mí
  - 🔴 Rojo: Vendido
  - 🟡 Amarillo: Bloqueado por otro
- Selección de 1 a 4 asientos
- Diálogo de confirmación al salir

### 6. Datos Personales (`/datos`)
- Formulario para cada asiento seleccionado
- Campo de nombre completo por asistente
- Validación (mín. 3 caracteres)
- Diálogo de confirmación al salir

### 7. Confirmación (`/confirmacion`)
- Resumen de la compra
- Lista de asientos con nombres
- Precio total
- Botón "Confirmar Compra"
- Diálogo de confirmación al cancelar

## 🔄 Flujo de Navegación

```
┌──────────┐     ┌──────────┐
│  LOGIN   │◄───►│ REGISTRO │
└────┬─────┘     └──────────┘
     │
     ▼
┌──────────┐     ┌──────────┐     ┌──────────┐
│ EVENTOS  │────►│ DETALLE  │────►│ ASIENTOS │
│  LIST    │     │          │     │          │
└──────────┘     └──────────┘     └────┬─────┘
     ▲                                 │
     │                                 ▼
     │           ┌──────────┐     ┌──────────┐
     │           │CONFIRMAC.│◄────│  DATOS   │
     │           │          │     │PERSONALES│
     │           └────┬─────┘     └──────────┘
     │                │
     └────────────────┘
        (Compra exitosa)
```

## ⚙️ Configuración

### 1. URL del Backend

Editar `EventosApiClient.kt`:

```kotlin
// Para emulador Android
private val baseUrl = "http://10.0.2.2:8080"

// Para dispositivo físico (cambiar por tu IP)
// private val baseUrl = "http://192.168.x.x:8080"
```

### 2. Requisitos

- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17+
- Android SDK 24+ (mínimo) / 34 (target)
- Emulador Android o dispositivo físico

## 🚀 Ejecución

### Opción 1: Android Studio

1. Abrir el proyecto en Android Studio
2. Esperar sincronización de Gradle
3. Crear/iniciar un emulador Android
4. Click en **Run** (▶) o `Shift + F10`

### Opción 2: Línea de comandos

```bash
# Compilar
./gradlew build

# Instalar en dispositivo conectado
./gradlew installDebug

# Ejecutar
./gradlew :composeApp:assembleDebug
```

## 🔌 Endpoints del Backend

La app consume los siguientes endpoints:

### Autenticación
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/login` | Iniciar sesión |
| POST | `/api/auth/registro` | Crear cuenta |

### Eventos
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/eventos` | Listar eventos |
| GET | `/api/eventos/{id}` | Detalle de evento |
| GET | `/api/eventos/{id}/asientos` | Estado de asientos |

### Sesión de Compra
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/sesion` | Obtener sesión actual |
| POST | `/api/sesion/iniciar` | Iniciar sesión de compra |
| POST | `/api/sesion/finalizar` | Finalizar sesión |

### Ventas
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/ventas/seleccionar` | Seleccionar asientos |
| POST | `/api/ventas/bloquear` | Bloquear asientos |
| POST | `/api/ventas/asignar-personas` | Asignar nombres |
| POST | `/api/ventas/confirmar` | Confirmar compra |
| POST | `/api/ventas/cancelar` | Cancelar proceso |

## 📊 Arquitectura

La aplicación sigue el patrón **MVVM (Model-View-ViewModel)**:

```
┌─────────────────────────────────────────────────────────┐
│                         UI                              │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐ │
│  │   Screen    │◄──►│  ViewModel  │◄──►│  UiState    │ │
│  │  (Compose)  │    │             │    │  (sealed)   │ │
│  └─────────────┘    └──────┬──────┘    └─────────────┘ │
│                            │                            │
└────────────────────────────┼────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────┐
│                       DATA                              │
│  ┌─────────────────┐    ┌─────────────────────────────┐│
│  │ EventosApiClient│    │          DTOs               ││
│  │    (Ktor)       │    │  (Kotlinx Serialization)    ││
│  └────────┬────────┘    └─────────────────────────────┘│
│           │                                             │
└───────────┼─────────────────────────────────────────────┘
            │
            ▼
     ┌──────────────┐
     │   Backend    │
     │  (HTTP/JSON) │
     └──────────────┘
```

## ✨ Características

- ✅ Autenticación JWT
- ✅ Validación de formularios en tiempo real
- ✅ Diálogos de confirmación para acciones destructivas
- ✅ Manejo de errores con mensajes amigables
- ✅ Estados de carga (Loading spinners)
- ✅ Persistencia de sesión (SessionManager)
- ✅ Navegación con parámetros
- ✅ Diseño Material 3

## 📝 Notas de Desarrollo

### Estados de UI (Sealed Classes)

Cada pantalla usa un sealed class para representar sus estados:

```kotlin
sealed class LoginUiState {
    object Idle : LoginUiState()      // Esperando input
    object Loading : LoginUiState()   // Procesando
    object Success : LoginUiState()   // Éxito
    data class Error(val mensaje: String) : LoginUiState()
}
```

### Manejo de Sesión

El `SessionManager` guarda el token JWT en memoria:

```kotlin
object SessionManager {
    var token: String? = null
    var username: String? = null
    
    fun saveSession(token: String, username: String) { ... }
    fun clearSession() { ... }
    fun isLoggedIn(): Boolean = token != null
}
```

### Colores de Asientos

```kotlin
val color = when (estado) {
    "LIBRE" -> Color(0xFF4CAF50)      // Verde
    "VENDIDO" -> Color(0xFFF44336)    // Rojo
    "BLOQUEADO" -> Color(0xFFFF9800)  // Naranja
    else -> Color.Gray
}

// Seleccionado por el usuario actual
val colorSeleccionado = Color(0xFF2196F3)  // Azul
```

## 🐛 Solución de Problemas

### Error: "No target device found"
- Verificar que hay un emulador corriendo o dispositivo conectado
- Ejecutar `adb devices` para verificar

### Error: "Connection refused"
- Verificar que el Backend está corriendo en el puerto 8080
- Verificar la URL en `EventosApiClient.kt`
- Para emulador: usar `10.0.2.2` (no `localhost`)

### Error: "401 Unauthorized"
- El token JWT expiró
- Cerrar sesión e iniciar de nuevo

package org.abel.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Define todas las rutas de navegación de la app.
 * Usar un objeto evita errores de tipeo en los strings.
 */
object AppRoutes {
    const val LOGIN = "login"
    const val EVENTOS = "eventos"
    const val EVENTO_DETALLE = "detalle/{eventoId}"
    const val SELECCION_ASIENTOS = "asientos/{eventoId}"
    const val DATOS_PERSONALES = "datos"
    const val CONFIRMACION = "confirmacion"

    // Funciones helper para rutas con parámetros
    fun eventoDetalle(eventoId: Long) = "detalle/$eventoId"
    fun seleccionAsientos(eventoId: Long) = "asientos/$eventoId"
}

/**
 * Contenedor principal de navegación.
 * Define qué pantalla mostrar para cada ruta.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.LOGIN  // Pantalla inicial
    ) {
        // Pantalla de Login
        composable(AppRoutes.LOGIN) {
            // TODO: LoginScreen(navController)
            PlaceholderScreen("Login", navController)
        }

        // Pantalla de Lista de Eventos
        composable(AppRoutes.EVENTOS) {
            // TODO: EventosListScreen(navController)
            PlaceholderScreen("Lista de Eventos", navController)
        }

        // Pantalla de Detalle de Evento (con parámetro)
        composable(
            route = AppRoutes.EVENTO_DETALLE,
            arguments = listOf(
                navArgument("eventoId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val eventoId = backStackEntry.arguments?.getLong("eventoId") ?: 0L
            // TODO: EventoDetalleScreen(eventoId, navController)
            PlaceholderScreen("Detalle Evento #$eventoId", navController)
        }

        // Pantalla de Selección de Asientos (con parámetro)
        composable(
            route = AppRoutes.SELECCION_ASIENTOS,
            arguments = listOf(
                navArgument("eventoId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val eventoId = backStackEntry.arguments?.getLong("eventoId") ?: 0L
            // TODO: SeleccionAsientosScreen(eventoId, navController)
            PlaceholderScreen("Selección Asientos #$eventoId", navController)
        }

        // Pantalla de Datos Personales
        composable(AppRoutes.DATOS_PERSONALES) {
            // TODO: DatosPersonalesScreen(navController)
            PlaceholderScreen("Datos Personales", navController)
        }

        // Pantalla de Confirmación
        composable(AppRoutes.CONFIRMACION) {
            // TODO: ConfirmacionScreen(navController)
            PlaceholderScreen("Confirmación", navController)
        }
    }
}

/**
 * Pantalla temporal para probar la navegación.
 * Será reemplazada por las pantallas reales en las siguientes subfases.
 */
@Composable
fun PlaceholderScreen(
    screenName: String,
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = screenName,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botones de navegación para probar
        when (screenName) {
            "Login" -> {
                Button(onClick = { navController.navigate(AppRoutes.EVENTOS) }) {
                    Text("Ir a Eventos")
                }
            }
            "Lista de Eventos" -> {
                Button(onClick = { navController.navigate(AppRoutes.eventoDetalle(1)) }) {
                    Text("Ver Evento #1")
                }
            }
            else -> {
                if (screenName.contains("Detalle")) {
                    Button(onClick = {
                        val eventoId = screenName.filter { it.isDigit() }.toLongOrNull() ?: 1L
                        navController.navigate(AppRoutes.seleccionAsientos(eventoId))
                    }) {
                        Text("Seleccionar Asientos")
                    }
                }
                if (screenName.contains("Asientos")) {
                    Button(onClick = { navController.navigate(AppRoutes.DATOS_PERSONALES) }) {
                        Text("Ir a Datos Personales")
                    }
                }
                if (screenName == "Datos Personales") {
                    Button(onClick = { navController.navigate(AppRoutes.CONFIRMACION) }) {
                        Text("Ir a Confirmación")
                    }
                }
                if (screenName == "Confirmación") {
                    Button(onClick = {
                        navController.navigate(AppRoutes.EVENTOS) {
                            popUpTo(AppRoutes.EVENTOS) { inclusive = true }
                        }
                    }) {
                        Text("Volver a Eventos")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para volver atrás (si no es Login)
        if (screenName != "Login") {
            OutlinedButton(onClick = { navController.popBackStack() }) {
                Text("← Volver")
            }
        }
    }
}
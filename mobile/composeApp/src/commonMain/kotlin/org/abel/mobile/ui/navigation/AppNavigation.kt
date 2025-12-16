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
import org.abel.mobile.ui.screens.login.LoginScreen
import org.abel.mobile.ui.screens.eventos.EventosListScreen
import org.abel.mobile.ui.screens.detalle.EventoDetalleScreen
import org.abel.mobile.ui.screens.asientos.SeleccionAsientosScreen
import org.abel.mobile.ui.screens.datos.DatosPersonalesScreen
import org.abel.mobile.ui.screens.confirmacion.ConfirmacionScreen

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
            LoginScreen(navController)
        }

        // Pantalla de Lista de Eventos
        composable(AppRoutes.EVENTOS) {
            EventosListScreen(navController)
        }

        // Pantalla de Detalle de Evento (con parámetro)
        composable(
            route = AppRoutes.EVENTO_DETALLE,
            arguments = listOf(
                navArgument("eventoId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val eventoId = backStackEntry.arguments?.getLong("eventoId") ?: 0L
            EventoDetalleScreen(eventoId, navController)
        }

        // Pantalla de Selección de Asientos (con parámetro)
        composable(
            route = AppRoutes.SELECCION_ASIENTOS,
            arguments = listOf(
                navArgument("eventoId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val eventoId = backStackEntry.arguments?.getLong("eventoId") ?: 0L
            SeleccionAsientosScreen(eventoId, navController)
        }

        // Pantalla de Datos Personales
        composable(AppRoutes.DATOS_PERSONALES) {
            DatosPersonalesScreen(navController)
        }

        // Pantalla de Confirmación
        composable(AppRoutes.CONFIRMACION) {
            ConfirmacionScreen(navController)
        }
    }
}
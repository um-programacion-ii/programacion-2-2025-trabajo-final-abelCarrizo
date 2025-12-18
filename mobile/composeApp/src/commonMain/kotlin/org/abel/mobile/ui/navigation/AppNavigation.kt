package org.abel.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.abel.mobile.ui.screens.login.LoginScreen
import org.abel.mobile.ui.screens.registro.RegistroScreen
import org.abel.mobile.ui.screens.eventos.EventosListScreen
import org.abel.mobile.ui.screens.detalle.EventoDetalleScreen
import org.abel.mobile.ui.screens.asientos.SeleccionAsientosScreen
import org.abel.mobile.ui.screens.datos.DatosPersonalesScreen
import org.abel.mobile.ui.screens.confirmacion.ConfirmacionScreen

/**
 * Define todas las rutas de navegación de la app.
 */
object AppRoutes {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
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
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.LOGIN
    ) {
        // Pantalla de Login
        composable(AppRoutes.LOGIN) {
            LoginScreen(navController)
        }

        // Pantalla de Registro
        composable(AppRoutes.REGISTRO) {
            RegistroScreen(navController)
        }

        // Pantalla de Lista de Eventos
        composable(AppRoutes.EVENTOS) {
            EventosListScreen(navController)
        }

        // Pantalla de Detalle de Evento
        composable(
            route = AppRoutes.EVENTO_DETALLE,
            arguments = listOf(
                navArgument("eventoId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val eventoId = backStackEntry.arguments?.getLong("eventoId") ?: 0L
            EventoDetalleScreen(eventoId, navController)
        }

        // Pantalla de Selección de Asientos
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
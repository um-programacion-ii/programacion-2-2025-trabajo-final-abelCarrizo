package org.abel.mobile.ui.screens.asientos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.abel.mobile.ui.navigation.AppRoutes

/**
 * Pantalla de selección de asientos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionAsientosScreen(
    eventoId: Long,
    navController: NavHostController,
    viewModel: AsientosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Estado para mostrar diálogo de confirmación de salida
    var mostrarDialogoSalir by remember { mutableStateOf(false) }

    // Cargar asientos al entrar
    LaunchedEffect(eventoId) {
        viewModel.cargarAsientos(eventoId)
    }

    // Diálogo de confirmación de salida
    if (mostrarDialogoSalir) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoSalir = false },
            title = { Text("¿Abandonar selección?") },
            text = { Text("Si sales ahora, perderás los asientos que hayas seleccionado.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoSalir = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Sí, salir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoSalir = false }) {
                    Text("Continuar seleccionando")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar Asientos") },
                navigationIcon = {
                    IconButton(onClick = {
                        // Mostrar diálogo si hay asientos seleccionados
                        val state = uiState
                        if (state is AsientosUiState.Success && state.seleccionados.isNotEmpty()) {
                            mostrarDialogoSalir = true
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is AsientosUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is AsientosUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.cargarAsientos(eventoId) }) {
                            Text("Reintentar")
                        }
                    }
                }

                is AsientosUiState.Success -> {
                    AsientosContent(
                        state = state,
                        onAsientoClick = { viewModel.onAsientoClick(it) },
                        onConfirmar = {
                            viewModel.confirmarSeleccion(
                                onSuccess = {
                                    navController.navigate(AppRoutes.DATOS_PERSONALES)
                                },
                                onError = { mensaje ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(mensaje)
                                    }
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Contenido principal con el mapa de asientos.
 */
@Composable
fun AsientosContent(
    state: AsientosUiState.Success,
    onAsientoClick: (AsientoUi) -> Unit,
    onConfirmar: () -> Unit
) {
    val filas = state.evento.filaAsientos ?: 0
    val columnas = state.evento.columnaAsientos ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Título del evento
        Text(
            text = state.evento.titulo,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Leyenda de colores
        LeyendaAsientos()

        Spacer(modifier = Modifier.height(16.dp))

        // Indicador de escenario
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ESCENARIO",
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grilla de asientos
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items((1..filas).toList()) { fila ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Número de fila
                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$fila",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    // Asientos de esta fila
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items((1..columnas).toList()) { columna ->
                            val asiento = state.asientos.find {
                                it.fila == fila && it.columna == columna
                            }

                            asiento?.let {
                                AsientoItem(
                                    asiento = it,
                                    onClick = { onAsientoClick(it) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resumen de selección
        Text(
            text = "Seleccionados: ${state.seleccionados.size}/4",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Botón confirmar
        Button(
            onClick = onConfirmar,
            enabled = state.seleccionados.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar con ${state.seleccionados.size} asiento(s)")
        }
    }
}

/**
 * Leyenda que explica los colores.
 */
@Composable
fun LeyendaAsientos() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LeyendaItem(color = Color.LightGray, texto = "Libre")
        LeyendaItem(color = Color(0xFF4CAF50), texto = "Seleccionado")
        LeyendaItem(color = Color(0xFFFF5722), texto = "Vendido")
        LeyendaItem(color = Color(0xFFFF9800), texto = "Bloqueado")
    }
}

@Composable
fun LeyendaItem(color: Color, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/**
 * Representa un asiento individual en la grilla.
 */
@Composable
fun AsientoItem(
    asiento: AsientoUi,
    onClick: () -> Unit
) {
    val color = when (asiento.estado) {
        EstadoAsientoUi.LIBRE -> Color.LightGray
        EstadoAsientoUi.SELECCIONADO -> Color(0xFF4CAF50)  // Verde
        EstadoAsientoUi.VENDIDO -> Color(0xFFFF5722)       // Rojo
        EstadoAsientoUi.BLOQUEADO -> Color(0xFFFF9800)     // Naranja
    }

    val esClickeable = asiento.estado == EstadoAsientoUi.LIBRE ||
            asiento.estado == EstadoAsientoUi.SELECCIONADO

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .then(
                if (esClickeable) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .border(
                width = 1.dp,
                color = Color.DarkGray,
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${asiento.columna}",
            style = MaterialTheme.typography.labelSmall,
            color = if (asiento.estado == EstadoAsientoUi.SELECCIONADO) Color.White else Color.Black
        )
    }
}
package org.abel.mobile.ui.screens.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import org.abel.mobile.data.model.EventoDetalle
import org.abel.mobile.ui.navigation.AppRoutes

/**
 * Pantalla de detalle de un evento.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventoDetalleScreen(
    eventoId: Long,
    navController: NavHostController,
    viewModel: EventoDetalleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Cargar evento al entrar a la pantalla
    LaunchedEffect(eventoId) {
        viewModel.cargarEvento(eventoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Evento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is DetalleUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is DetalleUiState.Error -> {
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
                        Button(onClick = { viewModel.cargarEvento(eventoId) }) {
                            Text("Reintentar")
                        }
                    }
                }

                is DetalleUiState.Success -> {
                    EventoDetalleContent(
                        evento = state.evento,
                        onSeleccionarAsientos = {
                            navController.navigate(AppRoutes.seleccionAsientos(eventoId))
                        }
                    )
                }
            }
        }
    }
}

/**
 * Contenido principal con la información del evento.
 */
@Composable
fun EventoDetalleContent(
    evento: EventoDetalle,
    onSeleccionarAsientos: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = evento.titulo,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tipo de evento
        evento.tipoEvento?.let { tipo ->
            Text(
                text = tipo,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Descripción
        evento.descripcion?.let { descripcion ->
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Card con información
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Fecha
                evento.fecha?.let { fecha ->
                    InfoRow(label = "Fecha", value = fecha)
                }

                // Dirección
                evento.direccion?.let { direccion ->
                    InfoRow(label = "Dirección", value = direccion)
                }

                // Precio
                evento.precioEntrada?.let { precio ->
                    InfoRow(label = "Precio", value = "$$precio")
                }

                // Capacidad
                if (evento.filaAsientos != null && evento.columnaAsientos != null) {
                    val capacidad = evento.filaAsientos * evento.columnaAsientos
                    InfoRow(label = "Capacidad", value = "$capacidad asientos")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Integrantes
        if (!evento.integrantes.isNullOrEmpty()) {
            Text(
                text = "Presentadores",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            evento.integrantes.forEach { integrante ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "${integrante.identificacion ?: ""} ${integrante.nombre ?: ""} ${integrante.apellido ?: ""}".trim(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Botón para seleccionar asientos
        Button(
            onClick = onSeleccionarAsientos,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Seleccionar Asientos")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Fila de información (label: valor).
 */
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
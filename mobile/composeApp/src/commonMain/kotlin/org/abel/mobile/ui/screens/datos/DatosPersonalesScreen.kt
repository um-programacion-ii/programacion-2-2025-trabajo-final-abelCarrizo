package org.abel.mobile.ui.screens.datos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.abel.mobile.ui.navigation.AppRoutes

/**
 * Pantalla para ingresar datos personales de cada asiento.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatosPersonalesScreen(
    navController: NavHostController,
    viewModel: DatosPersonalesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Datos de los Asistentes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                is DatosUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is DatosUiState.Error -> {
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
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Volver")
                        }
                    }
                }

                is DatosUiState.Success -> {
                    DatosContent(
                        asientos = state.asientos,
                        onNombreChange = { fila, columna, nombre ->
                            viewModel.onNombreChange(fila, columna, nombre)
                        },
                        onContinuar = {
                            viewModel.confirmarDatos(
                                onSuccess = {
                                    navController.navigate(AppRoutes.CONFIRMACION)
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
 * Contenido principal con los formularios de nombre.
 */
@Composable
fun DatosContent(
    asientos: List<AsientoConNombre>,
    onNombreChange: (Int, Int, String) -> Unit,
    onContinuar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Ingresa el nombre de cada asistente",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Asientos seleccionados: ${asientos.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de asientos con campos de nombre
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(asientos) { asiento ->
                AsientoNombreCard(
                    asiento = asiento,
                    onNombreChange = { nombre ->
                        onNombreChange(asiento.fila, asiento.columna, nombre)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón continuar
        Button(
            onClick = onContinuar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar a Confirmación")
        }
    }
}

/**
 * Card con información del asiento y campo de nombre.
 */
@Composable
fun AsientoNombreCard(
    asiento: AsientoConNombre,
    onNombreChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Asiento",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Fila ${asiento.fila} - Columna ${asiento.columna}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = asiento.nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre completo") },
                placeholder = { Text("Ej: Juan Pérez") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
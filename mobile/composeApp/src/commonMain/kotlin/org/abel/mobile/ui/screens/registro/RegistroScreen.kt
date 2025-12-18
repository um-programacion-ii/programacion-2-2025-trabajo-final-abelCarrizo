package org.abel.mobile.ui.screens.registro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import org.abel.mobile.ui.navigation.AppRoutes

/**
 * Pantalla de Registro de nuevo usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    navController: NavHostController,
    viewModel: RegistroViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val nombre by viewModel.nombre.collectAsState()
    val apellido by viewModel.apellido.collectAsState()
    val email by viewModel.email.collectAsState()

    val usernameError by viewModel.usernameError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()
    val nombreError by viewModel.nombreError.collectAsState()
    val apellidoError by viewModel.apellidoError.collectAsState()
    val emailError by viewModel.emailError.collectAsState()

    // Navegar cuando el registro es exitoso
    LaunchedEffect(uiState) {
        if (uiState is RegistroUiState.Success) {
            navController.navigate(AppRoutes.EVENTOS) {
                popUpTo(AppRoutes.LOGIN) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Cuenta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Completa tus datos",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Campo Usuario
            OutlinedTextField(
                value = username,
                onValueChange = { viewModel.onUsernameChange(it) },
                label = { Text("Usuario") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is RegistroUiState.Loading,
                isError = usernameError != null,
                supportingText = {
                    usernameError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo Email
            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is RegistroUiState.Loading,
                isError = emailError != null,
                supportingText = {
                    emailError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Nombre y Apellido en una fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { viewModel.onNombreChange(it) },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is RegistroUiState.Loading,
                    isError = nombreError != null,
                    supportingText = {
                        nombreError?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                OutlinedTextField(
                    value = apellido,
                    onValueChange = { viewModel.onApellidoChange(it) },
                    label = { Text("Apellido") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is RegistroUiState.Loading,
                    isError = apellidoError != null,
                    supportingText = {
                        apellidoError?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Campo Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is RegistroUiState.Loading,
                isError = passwordError != null,
                supportingText = {
                    passwordError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo Confirmar Contraseña
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { viewModel.onConfirmPasswordChange(it) },
                label = { Text("Confirmar Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is RegistroUiState.Loading,
                isError = confirmPasswordError != null,
                supportingText = {
                    confirmPasswordError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar error general
            if (uiState is RegistroUiState.Error) {
                Text(
                    text = (uiState as RegistroUiState.Error).mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Botón de Registro o indicador de carga
            if (uiState is RegistroUiState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { viewModel.registrar() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear Cuenta")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Enlace a Login
            TextButton(
                onClick = { navController.popBackStack() }
            ) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
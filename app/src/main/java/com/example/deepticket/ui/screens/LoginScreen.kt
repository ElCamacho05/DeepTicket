package com.example.deepticket.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.deepticket.loginUsuario
import com.example.deepticket.ui.theme.AppColors
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onNavigateToRegister: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Variables para guardar lo que el usuario escribe
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "DeepTicket", style = MaterialTheme.typography.headlineLarge, color = AppColors.orangeAccent)
        Spacer(modifier = Modifier.height(32.dp))

        // Campo de Correo
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Campo de Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(), // Oculta los caracteres
            singleLine = true
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator(color = AppColors.orangeAccent)
        } else {
            // Botón de Iniciar Sesión
            Button(
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        val exito = loginUsuario(email, password)
                        isLoading = false
                        if (exito) {
                            onLoginSuccess() // Navegar a la siguiente pantalla
                        } else {
                            Toast.makeText(context, "Error de credenciales", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.orangeAccent)
            ) {
                Text("Iniciar Sesión", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Registro
            TextButton(onClick = { onNavigateToRegister() }) {
                Text("¿No tienes cuenta? Regístrate", color = Color.Gray)
            }
        }
    }
}
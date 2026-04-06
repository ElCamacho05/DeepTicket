package com.example.deepticket.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.deepticket.registrarUsuarioCompleto
import com.example.deepticket.ui.theme.AppColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onBackToLogin: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Estados para el Género (Dropdown)
    var expandedGenero by remember { mutableStateOf(false) }
    var generoSeleccionado by remember { mutableStateOf("") }
    val opcionesGenero = listOf("Hombre", "Mujer")

    // Estado para el DatePicker (Fecha)
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis?.let {
                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(it))
                    } ?: ""
                    fechaNacimiento = date
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Únete a la Red", style = MaterialTheme.typography.headlineLarge, color = AppColors.orangeAccent)
        Spacer(modifier = Modifier.height(24.dp))

        // Nombres y Apellidos
        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = apellidos, onValueChange = { apellidos = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        // Selector de Fecha (Día/Mes/Año)
        OutlinedTextField(
            value = fechaNacimiento,
            onValueChange = { },
            label = { Text("Fecha de Nacimiento (Día/Mes/Año)") },
            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
            enabled = false, // Para que solo se use el selector
            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.Gray, disabledLabelColor = Color.Gray)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Menú de Género (Hombre / Mujer)
        ExposedDropdownMenuBox(
            expanded = expandedGenero,
            onExpandedChange = { expandedGenero = !expandedGenero }
        ) {
            OutlinedTextField(
                value = generoSeleccionado,
                onValueChange = {},
                readOnly = true,
                label = { Text("Género") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGenero) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedGenero, onDismissRequest = { expandedGenero = false }) {
                opcionesGenero.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            generoSeleccionado = opcion
                            expandedGenero = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Correo con validación de Gmail
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo Gmail") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator(color = AppColors.orangeAccent)
        } else {
            Button(
                onClick = {
                    // --- VALIDACIONES ---
                    if (!email.endsWith("@gmail.com")) {
                        Toast.makeText(context, "Debes usar un correo @gmail.com", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (nombre.isBlank() || generoSeleccionado.isBlank() || fechaNacimiento.isBlank() || password.length < 6) {
                        Toast.makeText(context, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    coroutineScope.launch {
                        val exito = registrarUsuarioCompleto(email, password, nombre, apellidos, fechaNacimiento, generoSeleccionado)
                        isLoading = false
                        if (exito) {
                            onRegisterSuccess()
                        } else {
                            Toast.makeText(context, "Error al registrar", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.orangeAccent)
            ) {
                Text("Crear Cuenta", color = Color.White)
            }
        }
    }
}

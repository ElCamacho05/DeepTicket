package com.example.deepticket.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deepticket.data.ProductItem
import com.example.deepticket.ui.theme.AppColors
import io.github.jan.supabase.gotrue.auth

@Composable
fun ScreenApp() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var checkingSession by remember { mutableStateOf(true) }

    // NUEVO ESTADO PARA CAMBIAR ENTRE PANTALLAS DE AUTENTICACIÓN
    var showRegisterScreen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val session = com.example.deepticket.supabase.auth.currentSessionOrNull()
        isLoggedIn = session != null
        checkingSession = false
    }

    if (checkingSession) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AppColors.orangeAccent)
        }
    } else {
        if (!isLoggedIn) {
            // CONTROLADOR DE PANTALLAS DE ENTRADA
            if (showRegisterScreen) {
                RegisterScreen(
                    onRegisterSuccess = { showRegisterScreen = false }, // Al terminar, regresa al Login
                    onBackToLogin = { showRegisterScreen = false }      // Si se arrepiente, regresa al Login
                )
            } else {
                LoginScreen(
                    onLoginSuccess = { isLoggedIn = true },
                    onNavigateToRegister = { showRegisterScreen = true } // Abre el Registro
                )
            }
        } else {
            DeepTicketApp()
        }
    }
}






@Composable
fun AcordeonTicket(supermarket: String, date: String, products: List<ProductItem>) {
    var expandido by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { expandido = !expandido }, colors = CardDefaults.cardColors(containerColor = AppColors.elementBackground), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(AppColors.analysisCardBackground, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Receipt, contentDescription = null, tint = AppColors.orangeAccent, modifier = Modifier.size(20.dp)) }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) { Text(supermarket, color = AppColors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("$date • ${products.size} artículos", color = AppColors.greyText, fontSize = 12.sp) }
                Text("$${String.format("%.2f", products.sumOf { it.price })}", color = AppColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Icon(if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = AppColors.greyText)
            }
            AnimatedVisibility(visible = expandido) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = AppColors.background, thickness = 1.dp); Spacer(modifier = Modifier.height(12.dp))
                    products.forEach { product ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) { Text(product.name, color = Color.White, fontSize = 14.sp); Text(product.brand, color = AppColors.greyText, fontSize = 12.sp) }
                            Text("$${String.format("%.2f", product.price)}", color = AppColors.greyText, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AcordeonMarca(brand: String, products: List<ProductItem>) {
    var expandido by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { expandido = !expandido }, colors = CardDefaults.cardColors(containerColor = AppColors.elementBackground), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(AppColors.analysisCardBackground, CircleShape), contentAlignment = Alignment.Center) { Text(brand.take(1), color = AppColors.orangeAccent, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) { Text(brand, color = AppColors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("${products.size} compras", color = AppColors.greyText, fontSize = 12.sp) }
                Text("$${String.format("%.2f", products.sumOf { it.price })}", color = AppColors.orangeAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Icon(if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = AppColors.greyText)
            }
            AnimatedVisibility(visible = expandido) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = AppColors.background, thickness = 1.dp); Spacer(modifier = Modifier.height(12.dp))
                    products.sortedByDescending { it.date }.forEach { product ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) { Text(product.name, color = Color.White, fontSize = 14.sp); Text("Comprado: ${product.date} en ${product.supermarket}", color = AppColors.greyText, fontSize = 12.sp) }
                            Text("$${String.format("%.2f", product.price)}", color = AppColors.greyText, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}








package com.example.deepticket.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deepticket.data.ProductItem
import com.example.deepticket.ui.components.CardProducto
import com.example.deepticket.ui.components.Header
import com.example.deepticket.ui.components.SummaryCard
import com.example.deepticket.ui.theme.AppColors
import kotlin.collections.forEach

@Composable
fun HomeScreen(userName: String, productsList: List<ProductItem>, searchQuery: String, onQueryChange: (String) -> Unit, totalItemsInLastScan: Int, totalSpentInLastScan: Double, totalRastreados: Int, totalMarcas: Int, totalRecibos: Int, onVerReciboClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp)); Header(nombre = userName); Spacer(modifier = Modifier.height(20.dp))
        TextField(value = searchQuery, onValueChange = onQueryChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Buscar en mi historial...", color = AppColors.greyText) }, leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.greyText) }, singleLine = true, shape = RoundedCornerShape(25.dp), colors = TextFieldDefaults.colors(focusedContainerColor = AppColors.elementBackground, unfocusedContainerColor = AppColors.elementBackground, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = AppColors.orangeAccent))
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.FlashOn, contentDescription = null, tint = AppColors.orangeAccent, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("ÚLTIMO ANÁLISIS IA", color = AppColors.orangeAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp); Spacer(modifier = Modifier.weight(1f)); Text("Hace un momento", color = AppColors.greyText, fontSize = 12.sp) }
        Spacer(modifier = Modifier.height(12.dp))
        Card(colors = CardDefaults.cardColors(containerColor = AppColors.analysisCardBackground), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(modifier = Modifier.size(80.dp).background(Color.Black, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = AppColors.orangeAccent, modifier = Modifier.size(32.dp)) }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("ÚLTIMO ESCANEO - 21 MAR", color = AppColors.greyText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("$totalItemsInLastScan Productos", color = AppColors.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold); Text("Detectados", color = AppColors.orangeAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFFE31B23)).padding(2.dp), contentAlignment = Alignment.Center) { Text("L", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }; Spacer(modifier = Modifier.width(4.dp))
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF003F87)).padding(2.dp), contentAlignment = Alignment.Center) { Text("K", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row { Column { Text("TOTAL", color = AppColors.greyText, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text("$${String.format("%.2f", totalSpentInLastScan)}", color = AppColors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) }; Spacer(modifier = Modifier.width(24.dp)); Column { Text("TIENDA", color = AppColors.greyText, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text("Walmart", color = AppColors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) } }
                    Box(modifier = Modifier.clickable { onVerReciboClick() }.border(1.dp, AppColors.orangeAccent, RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 6.dp)) { Text("Ver >", color = AppColors.orangeAccent, fontSize = 14.sp) }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(totalRastreados.toString(), "Rastreados", Icons.Default.LocalMall, AppColors.purpleIcon, Modifier.weight(1f))
            SummaryCard(totalMarcas.toString(), "Marcas", Icons.Default.Star, AppColors.greenIcon, Modifier.weight(1f))
            SummaryCard(totalRecibos.toString(), "Recibos", Icons.Default.List, AppColors.orangeAccent, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Añadidos recientemente", color = AppColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text("Ver todo", color = AppColors.orangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.height(12.dp))
        if (productsList.isEmpty()) { Text("No se encontraron productos...", color = AppColors.greyText, modifier = Modifier.padding(16.dp)) } else { productsList.forEach { CardProducto(it) } }
        Spacer(modifier = Modifier.height(120.dp))
    }
}
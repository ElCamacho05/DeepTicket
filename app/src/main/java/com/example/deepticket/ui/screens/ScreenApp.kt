package com.example.deepticket.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deepticket.data.ProductItem
import com.example.deepticket.ui.theme.AppColors
import com.example.deepticket.ui.components.*

@Composable
fun HomeScreen(productsList: List<ProductItem>, searchQuery: String, onQueryChange: (String) -> Unit, totalItemsInLastScan: Int, totalSpentInLastScan: Double, totalRastreados: Int, totalMarcas: Int, totalRecibos: Int, onVerReciboClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp)); Header(); Spacer(modifier = Modifier.height(20.dp))
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

@Composable
fun HistorialScreen(products: List<ProductItem>) {
    var selectedFilter by remember { mutableStateOf("Todo") }
    val filters = listOf("Todo", "Esta semana", "Este mes", "Más antiguo", "Marcas")

    val filteredProducts = when (selectedFilter) {
        "Esta semana" -> products.filter { it.date >= "2026-03-15" }
        "Este mes" -> products.filter { it.date.startsWith("2026-03") }
        "Más antiguo" -> products.filter { it.date < "2026-03-01" }
        else -> products
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Historial", color = AppColors.textPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters.size) { index ->
                val filter = filters[index]
                val isSelected = filter == selectedFilter
                Surface(color = if (isSelected) AppColors.orangeAccent else AppColors.elementBackground, shape = RoundedCornerShape(20.dp), modifier = Modifier.clickable { selectedFilter = filter }.height(36.dp)) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) { Text(filter, color = if (isSelected) Color.White else AppColors.greyText, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            if (filteredProducts.isEmpty()) { Text("No hay recibos en este periodo.", color = AppColors.greyText) }
            else if (selectedFilter == "Marcas") { filteredProducts.groupBy { it.brand }.forEach { (brand, brandProducts) -> AcordeonMarca(brand = brand, products = brandProducts) } }
            else { val tickets = filteredProducts.groupBy { "${it.supermarket}_${it.date}" }; tickets.forEach { (_, ticketProducts) -> AcordeonTicket(supermarket = ticketProducts.first().supermarket, date = ticketProducts.first().date, products = ticketProducts) } }
        }
        Spacer(modifier = Modifier.height(120.dp))
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

@Composable
fun AnaliticasScreen(products: List<ProductItem>, totalSpent: Double) {
    val gastosPorMarca = products.groupBy { it.brand }.mapValues { entry -> entry.value.sumOf { it.price } }.entries.sortedByDescending { it.value }
    val gastosPorSuper = products.groupBy { it.supermarket }.mapValues { entry -> entry.value.sumOf { it.price } }.entries.sortedByDescending { it.value }
    val maxSuperGasto = gastosPorSuper.maxOfOrNull { it.value } ?: 1.0

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp)); Text("Analíticas", color = AppColors.textPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AppColors.elementBackground), shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Inversión por Marca", color = AppColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(24.dp))
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                    DonutChart(gastosPorMarca = gastosPorMarca.associate { it.key to it.value })
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("TOTAL", color = AppColors.greyText, fontSize = 12.sp, fontWeight = FontWeight.Bold); Text("$${String.format("%.2f", totalSpent)}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold) }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    gastosPorMarca.take(4).forEachIndexed { index, (brand, gasto) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(AppColors.chartColors[index % AppColors.chartColors.size])); Spacer(modifier = Modifier.width(8.dp)); Text(brand, color = Color.White, fontSize = 14.sp) }
                            Text("$${String.format("%.2f", gasto)}", color = AppColors.greyText, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AppColors.elementBackground), shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Lugares de Compra", color = AppColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(16.dp))
                gastosPorSuper.forEach { (supermarket, gasto) ->
                    val porcentaje = (gasto / maxSuperGasto).toFloat()
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(supermarket, color = Color.White, fontSize = 14.sp); Text("$${String.format("%.2f", gasto)}", color = AppColors.greenIcon, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.3f))) { Box(modifier = Modifier.fillMaxWidth(fraction = porcentaje).height(10.dp).clip(CircleShape).background(AppColors.greenIcon)) }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun PerfilScreen(totalRastreados: Int) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(40.dp))
        Box(modifier = Modifier.size(110.dp).background(AppColors.elementBackground, CircleShape).border(2.dp, AppColors.orangeAccent, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, contentDescription = null, tint = AppColors.greyText, modifier = Modifier.size(60.dp)) }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Carlos", color = AppColors.textPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("📍 Heroica Veracruz", color = AppColors.greyText, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(32.dp))
        Card(modifier = Modifier.fillMaxWidth().height(100.dp), colors = CardDefaults.cardColors(containerColor = AppColors.orangeAccent.copy(alpha = 0.1f)), shape = RoundedCornerShape(30.dp), border = BorderStroke(1.dp, AppColors.orangeAccent.copy(alpha = 0.3f))) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { Text(totalRastreados.toString(), color = AppColors.orangeAccent, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold); Text("Artículos Rastreados en Total", color = AppColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Configuración", color = AppColors.greyText, fontSize = 14.sp, modifier = Modifier.padding(bottom = 12.dp, start = 8.dp))
            ProfilePillButton(icon = Icons.Outlined.ManageAccounts, title = "Editar Perfil")
            ProfilePillButton(icon = Icons.Outlined.Notifications, title = "Notificaciones")
            ProfilePillButton(icon = Icons.Outlined.Lock, title = "Privacidad y Seguridad")
            ProfilePillButton(icon = Icons.Outlined.HelpOutline, title = "Ayuda y Soporte")
            Spacer(modifier = Modifier.height(8.dp))
            ProfilePillButton(icon = Icons.Outlined.Logout, title = "Cerrar Sesión", isDestructive = true)
        }
        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun ReceiptDetailsScreen(products: List<ProductItem>, totalSpent: Double, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(AppColors.background).padding(20.dp).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp)); Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White) }; Text("Detalle de Escaneo", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(16.dp)).background(AppColors.elementBackground), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = AppColors.greyText, modifier = Modifier.size(60.dp)); Spacer(modifier = Modifier.height(12.dp)); Text("Imagen del ticket capturada", color = AppColors.greyText) } }
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Productos Extraídos", color = AppColors.orangeAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("$${String.format("%.2f", totalSpent)}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.height(16.dp)); products.forEach { CardProducto(it) }; Spacer(modifier = Modifier.height(40.dp))
    }
}
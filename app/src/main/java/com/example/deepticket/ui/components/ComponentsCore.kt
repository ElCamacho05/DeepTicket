package com.example.deepticket.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.deepticket.data.ProductItem
import com.example.deepticket.ui.theme.AppColors

@Composable
fun CardProducto(producto: ProductItem) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), colors = CardDefaults.cardColors(containerColor = AppColors.elementBackground), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Icon(producto.iconId, contentDescription = null, tint = AppColors.orangeAccent, modifier = Modifier.size(20.dp)) }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) { Text(producto.name, color = AppColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium); Text("De ${producto.supermarket}", color = AppColors.greyText, fontSize = 12.sp) }
            Column(horizontalAlignment = Alignment.End) { Box(modifier = Modifier.border(1.dp, AppColors.orangeAccent, RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) { Text(producto.unit, color = AppColors.orangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold) }; Text("$${String.format("%.2f", producto.price)}", color = AppColors.greyText, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)) }
        }
    }
}

@Composable
fun BottomNav(selectedTab: String, onTabSelected: (String) -> Unit) {
    NavigationBar(containerColor = AppColors.elementBackground, modifier = Modifier.height(80.dp)) {
        listOf(Triple("Inicio", Icons.Outlined.Home, Icons.Filled.Home), Triple("Historial", Icons.Outlined.History, Icons.Filled.History), Triple("Analíticas", Icons.Outlined.QueryStats, Icons.Filled.QueryStats), Triple("Perfil", Icons.Outlined.Person, Icons.Filled.Person)).forEach { (title, outline, filled) ->
            val isSelected = selectedTab == title
            NavigationBarItem(selected = isSelected, onClick = { onTabSelected(title) }, icon = { Icon(if (isSelected) filled else outline, contentDescription = title) }, label = { Text(title) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = AppColors.orangeAccent, selectedTextColor = AppColors.orangeAccent, unselectedIconColor = AppColors.greyText, unselectedTextColor = AppColors.greyText, indicatorColor = Color.Transparent))
        }
    }
}

@Composable
fun SummaryCard(valor: String, etiqueta: String, icono: ImageVector, color: Color, modifier: Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = AppColors.elementBackground), shape = RoundedCornerShape(16.dp), modifier = modifier.height(100.dp)) { Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) { Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.weight(1f)); Text(valor, color = AppColors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text(etiqueta, color = AppColors.greyText, fontSize = 12.sp) } }
}

@Composable
fun Header() { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column { Text("Sábado, 21 de Marzo", color = AppColors.greyText, fontSize = 14.sp); Text("Hola, Carlos 👋", color = AppColors.textPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold) }; Surface(modifier = Modifier.size(48.dp), color = AppColors.elementBackground, shape = CircleShape) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Notifications, contentDescription = null, tint = AppColors.greyText); Box(modifier = Modifier.size(10.dp).align(Alignment.TopEnd).offset(x = (-8).dp, y = 8.dp).background(AppColors.orangeAccent, CircleShape)) } } } }

@Composable
fun ScanButton() { ExtendedFloatingActionButton(onClick = { /* Cámara */ }, containerColor = AppColors.orangeAccent, contentColor = Color.White, shape = RoundedCornerShape(30.dp), modifier = Modifier.height(60.dp).width(240.dp).offset(y = (-20).dp)) { Icon(Icons.Default.CameraAlt, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Escanear Recibo", fontSize = 18.sp, fontWeight = FontWeight.Bold) } }

@Composable
fun DonutChart(gastosPorMarca: Map<String, Double>) {
    val total = gastosPorMarca.values.sum().toFloat()
    var anguloInicio = -90f
    Canvas(modifier = Modifier.fillMaxSize()) {
        gastosPorMarca.values.forEachIndexed { index, gasto ->
            val anguloBarrido = (gasto.toFloat() / total) * 360f
            drawArc(color = AppColors.chartColors[index % AppColors.chartColors.size], startAngle = anguloInicio, sweepAngle = anguloBarrido, useCenter = false, style = Stroke(width = 40f, cap = StrokeCap.Butt))
            anguloInicio += anguloBarrido
        }
    }
}

@Composable
fun ProfilePillButton(icon: ImageVector, title: String, isDestructive: Boolean = false) {
    val tintColor = if (isDestructive) Color(0xFFEF4444) else Color.White
    val bgColor = if (isDestructive) Color(0xFFEF4444).copy(alpha = 0.1f) else AppColors.elementBackground
    Surface(color = bgColor, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { }.height(64.dp)) {
        Row(modifier = Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tintColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, color = tintColor, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = if (isDestructive) Color.Transparent else AppColors.greyText)
        }
    }
}
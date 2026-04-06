package com.example.deepticket.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deepticket.data.ProductItem
import com.example.deepticket.ui.components.DonutChart
import com.example.deepticket.ui.theme.AppColors
import kotlin.collections.component1
import kotlin.collections.component2

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
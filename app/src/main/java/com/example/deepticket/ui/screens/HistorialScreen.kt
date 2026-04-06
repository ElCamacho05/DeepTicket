package com.example.deepticket.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deepticket.data.ProductItem
import com.example.deepticket.ui.theme.AppColors
import kotlin.collections.component1
import kotlin.collections.component2

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
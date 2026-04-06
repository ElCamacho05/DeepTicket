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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.deepticket.ui.components.CardProducto
import com.example.deepticket.ui.theme.AppColors
import kotlin.collections.forEach

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
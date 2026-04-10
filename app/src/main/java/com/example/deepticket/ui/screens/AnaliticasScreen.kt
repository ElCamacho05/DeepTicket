package com.example.deepticket.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deepticket.data.ProductItem
import com.example.deepticket.data.RecommendedProduct
import com.example.deepticket.ui.components.DonutChart
import com.example.deepticket.ui.theme.AppColors
import kotlin.collections.component1
import kotlin.collections.component2

// Iconos por categoría para darle vida visual a las recomendaciones
private fun iconForCategory(category: String) = when (category.lowercase()) {
    "electronics", "tecnología", "technology" -> Icons.Default.Devices
    "food", "alimentos", "bebidas", "snacks"  -> Icons.Default.LocalGroceryStore
    "clothing", "ropa"                        -> Icons.Default.Checkroom
    "health", "salud", "beauty", "belleza"    -> Icons.Default.HealthAndSafety
    "sports", "deportes"                      -> Icons.Default.SportsSoccer
    "home", "hogar"                           -> Icons.Default.Home
    else                                      -> Icons.Default.Star
}

@Composable
fun AnaliticasScreen(
    products: List<ProductItem>,
    totalSpent: Double,
    userId: String,                                // ← NUEVO PARÁMETRO
    viewModel: RecommendationViewModel = viewModel()
) {
    val gastosPorMarca  = products.groupBy { it.brand }
        .mapValues { e -> e.value.sumOf { it.price } }
        .entries.sortedByDescending { it.value }

    val gastosPorSuper  = products.groupBy { it.supermarket }
        .mapValues { e -> e.value.sumOf { it.price } }
        .entries.sortedByDescending { it.value }

    val maxSuperGasto   = gastosPorSuper.maxOfOrNull { it.value } ?: 1.0

    // ─── Lanzar recomendaciones cuando cambie userId o products ─────────────
    val recommendationState by viewModel.state.collectAsState()
    LaunchedEffect(userId, products.size) {
        viewModel.loadRecommendations(userId, products)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Analíticas", color = AppColors.textPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        // ══════════════════════════════════════════════════
        // CARD 1 — Inversión por Marca (sin cambios)
        // ══════════════════════════════════════════════════
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.elementBackground),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Inversión por Marca", color = AppColors.textPrimary, fontSize = 16.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(24.dp))
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                    DonutChart(gastosPorMarca = gastosPorMarca.associate { it.key to it.value })
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL", color = AppColors.greyText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("$${String.format("%.2f", totalSpent)}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    gastosPorMarca.take(4).forEachIndexed { index, (brand, gasto) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).clip(CircleShape)
                                    .background(AppColors.chartColors[index % AppColors.chartColors.size]))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(brand, color = Color.White, fontSize = 14.sp)
                            }
                            Text("$${String.format("%.2f", gasto)}", color = AppColors.greyText, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ══════════════════════════════════════════════════
        // CARD 2 — Lugares de Compra (sin cambios)
        // ══════════════════════════════════════════════════
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.elementBackground),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Lugares de Compra", color = AppColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                gastosPorSuper.forEach { (supermarket, gasto) ->
                    val porcentaje = (gasto / maxSuperGasto).toFloat()
                    val animPct by animateFloatAsState(
                        targetValue = porcentaje,
                        animationSpec = tween(800),
                        label = "barAnim"
                    )
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(supermarket, color = Color.White, fontSize = 14.sp)
                            Text("$${String.format("%.2f", gasto)}", color = AppColors.greenIcon,
                                fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(10.dp)
                            .clip(CircleShape).background(Color.Black.copy(alpha = 0.3f))) {
                            Box(modifier = Modifier.fillMaxWidth(fraction = animPct).height(10.dp)
                                .clip(CircleShape).background(AppColors.greenIcon))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ══════════════════════════════════════════════════
        // CARD 3 — Recomendaciones KNN  ← NUEVO
        // ══════════════════════════════════════════════════
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.elementBackground),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // Encabezado con badge "IA"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Recomendado para ti", color = AppColors.textPrimary,
                            fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Basado en compras similares a las tuyas",
                            color = AppColors.greyText, fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier
                            .background(AppColors.orangeAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null,
                                tint = AppColors.orangeAccent, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("IA", color = AppColors.orangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ─── Estados de la recomendación ───────────────────
                when (val state = recommendationState) {

                    is RecommendationState.Loading -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = AppColors.orangeAccent, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Buscando tu tribu de compradores...",
                                color = AppColors.greyText, fontSize = 13.sp)
                        }
                    }

                    is RecommendationState.Empty -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null,
                                tint = AppColors.greyText, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Escanea más tickets para obtener recomendaciones",
                                color = AppColors.greyText, fontSize = 13.sp)
                        }
                    }

                    is RecommendationState.Error -> {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(Color(0xFF3B1212), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null,
                                tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("No se pudieron cargar las recomendaciones",
                                color = Color(0xFFEF4444), fontSize = 13.sp)
                        }
                    }

                    is RecommendationState.Success -> {
                        RecommendationList(items = state.items)
                    }

                    else -> { /* Idle – no mostrar nada */ }
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

// ─── Lista visual de productos recomendados ──────────────────────────────────
@Composable
private fun RecommendationList(items: List<RecommendedProduct>) {
    val maxScore = items.maxOfOrNull { it.score } ?: 1
    items.forEachIndexed { index, item ->
        if (index > 0) Spacer(modifier = Modifier.height(12.dp))
        RecommendationRow(item = item, rank = index + 1, maxScore = maxScore)
    }
}

@Composable
private fun RecommendationRow(item: RecommendedProduct, rank: Int, maxScore: Int) {
    val barFraction = (item.score.toFloat() / maxScore).coerceIn(0f, 1f)
    val animBar by animateFloatAsState(
        targetValue = barFraction,
        animationSpec = tween(600 + rank * 80),
        label = "recBar$rank"
    )
    val rankColor = when (rank) {
        1 -> Color(0xFFFBBF24)   // oro
        2 -> Color(0xFF94A3B8)   // plata
        3 -> Color(0xFFCD7F32)   // bronce
        else -> AppColors.greyText
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Número de ranking
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(rankColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("$rank", color = rankColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Icono de categoría
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(AppColors.analysisCardBackground, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconForCategory(item.category),
                contentDescription = null,
                tint = AppColors.orangeAccent,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Nombre + barra de popularidad
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.productName,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.category,
                color = AppColors.greyText,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = animBar)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(AppColors.orangeAccent)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Badge de popularidad
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${item.score}",
                color = AppColors.orangeAccent,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text("votos", color = AppColors.greyText, fontSize = 9.sp)
        }
    }
}
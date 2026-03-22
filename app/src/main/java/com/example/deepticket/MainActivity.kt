package com.example.deepticket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.deepticket.data.ProductItem
import com.example.deepticket.ui.components.*
import com.example.deepticket.ui.screens.*
import com.example.deepticket.ui.theme.AppColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = AppColors.background) {
                    DeepTicketApp()
                }
            }
        }
    }
}

@Composable
fun DeepTicketApp() {
    val mockDatabase = remember {
        listOf(
            ProductItem("1", Icons.Default.Opacity, "Lala Whole Milk", "Lala", "2 units", "Lácteos", 57.00, "2026-03-21", "Walmart"),
            ProductItem("2", Icons.Default.BakeryDining, "Corn Flakes", "Kellogg's", "1 box", "Cereales", 65.00, "2026-03-21", "Walmart"),
            ProductItem("3", Icons.Default.LocalCafe, "Coca-Cola 600ml", "Coca-Cola", "6 bottles", "Bebidas", 108.00, "2026-03-20", "Chedraui"),
            ProductItem("4", Icons.Default.WaterDrop, "Corn Oil", "Mazola", "1 liter", "Aceites", 45.50, "2026-03-20", "Chedraui"),
            ProductItem("5", Icons.Default.CleaningServices, "Sponge", "Scotch-Brite", "3 packs", "Limpieza", 30.00, "2026-03-16", "Oxxo"),
            ProductItem("6", Icons.Default.LocalCafe, "Coca Zero", "Coca-Cola", "2 bottles", "Bebidas", 38.00, "2026-02-15", "Soriana")
        )
    }

    var selectedTab by remember { mutableStateOf("Inicio") }
    var searchQuery by remember { mutableStateOf("") }
    var showReceiptDetails by remember { mutableStateOf(false) }

    val totalSpent = mockDatabase.sumOf { it.price }
    val totalTrackedItemsCount = mockDatabase.sumOf { it.unit.takeWhile { char -> char.isDigit() }.toIntOrNull() ?: 1 }

    if (showReceiptDetails) {
        ReceiptDetailsScreen(products = mockDatabase.filter { it.date == "2026-03-21" }, totalSpent = 122.00, onBack = { showReceiptDetails = false })
    } else {
        Scaffold(
            containerColor = AppColors.background,
            bottomBar = { BottomNav(selectedTab = selectedTab, onTabSelected = { selectedTab = it }) },
            floatingActionButton = { ScanButton() },
            floatingActionButtonPosition = FabPosition.Center
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (selectedTab) {
                    "Inicio" -> HomeScreen(
                        productsList = mockDatabase.filter { it.name.contains(searchQuery, true) || it.brand.contains(searchQuery, true) },
                        searchQuery = searchQuery, onQueryChange = { searchQuery = it },
                        totalItemsInLastScan = 2, totalSpentInLastScan = 122.00,
                        totalRastreados = totalTrackedItemsCount, totalMarcas = mockDatabase.map { it.brand }.distinct().size, totalRecibos = 4,
                        onVerReciboClick = { showReceiptDetails = true }
                    )
                    "Historial" -> HistorialScreen(products = mockDatabase)
                    "Analíticas" -> AnaliticasScreen(products = mockDatabase, totalSpent = totalSpent)
                    "Perfil" -> PerfilScreen(totalRastreados = totalTrackedItemsCount)
                }
            }
        }
    }
}
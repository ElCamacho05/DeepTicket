package com.example.deepticket.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.deepticket.data.ProductItem
import com.example.deepticket.ui.components.BottomNav
import com.example.deepticket.ui.components.ScanButton
import com.example.deepticket.ui.theme.AppColors
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@Composable
fun DeepTicketApp() {
    val database = remember { mutableStateListOf<ProductItem>() }
    var userName by remember { mutableStateOf("Usuario") }
    LaunchedEffect(Unit) {
        try {
            val user = com.example.deepticket.supabase.auth.currentUserOrNull()
            // Extraemos el nombre de los metadatos
            val nombreExtraido = user?.userMetadata?.get("nombre")?.toString()?.replace("\"", "")

            if (!nombreExtraido.isNullOrBlank()) {
                userName = nombreExtraido
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    // 2. Estados de navegación y UI
    var selectedTab by remember { mutableStateOf("Inicio") }
    var searchQuery by remember { mutableStateOf("") }
    var showReceiptDetails by remember { mutableStateOf(false) }
    var isCameraOpen by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 3. Lanzador para la GALERÍA
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            coroutineScope.launch {
                // Aquí llamarás a tu función de OCR próximamente
                Toast.makeText(context, "Imagen seleccionada. Iniciando escaneo...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 4. Lanzador para permisos de CÁMARA
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) isCameraOpen = true
        else Toast.makeText(context, "Permiso de cámara necesario", Toast.LENGTH_SHORT).show()
    }

    // 5. Lógica de pantallas
    if (isCameraOpen) {
        CameraScreen(onClose = { isCameraOpen = false })
    } else if (showReceiptDetails) {
        ReceiptDetailsScreen(
            products = database.toList(),
            totalSpent = database.sumOf { it.price },
            onBack = { showReceiptDetails = false }
        )
    } else {
        Scaffold(
            containerColor = AppColors.background,
            bottomBar = {
                BottomNav(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            },
            // QUITAMOS EL FLOATING ACTION BUTTON AUTOMÁTICO
        ) { padding ->
            // Usamos este Box principal para posicionar todo
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {

                // --- A) CONTENIDO DE LAS PANTALLAS ---
                val filteredList = database.filter {
                    it.name.contains(searchQuery, true) || it.brand.contains(searchQuery, true)
                }

                when (selectedTab) {
                    "Inicio" -> HomeScreen(
                        userName = userName,
                        productsList = filteredList,
                        searchQuery = searchQuery,
                        onQueryChange = { searchQuery = it },
                        totalItemsInLastScan = filteredList.size,
                        totalSpentInLastScan = filteredList.sumOf { it.price },
                        totalRastreados = database.size,
                        totalMarcas = database.map { it.brand }.distinct().size,
                        totalRecibos = 0,
                        onVerReciboClick = { showReceiptDetails = true }
                    )
                    "Historial" -> HistorialScreen(products = database.toList())
                    "Analíticas" -> AnaliticasScreen(products = database.toList(), totalSpent = database.sumOf { it.price })
                    "Perfil" -> PerfilScreen(userName = userName, totalRastreados = database.size)
                }

                // --- B) CONTENEDOR DE BOTONES INFERIORES ---
                // Los posicionamos manualmente en la parte inferior central
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter) // Alineamos al centro inferior del Box principal
                        .padding(bottom = 32.dp) // Separación del borde inferior (más o menos como estaba el FAB)
                        .padding(horizontal = 10.dp), // Padding lateral para que no peguen a los bordes
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center // Los botones se centrarán horizontalmente
                ) {
                    // 1. BOTÓN DE GALERÍA (A la izquierda)
                    SmallFloatingActionButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        containerColor = AppColors.elementBackground,
                        contentColor = AppColors.orangeAccent,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(56.dp) // Tamaño estándar de SmallFAB
                            .offset(y=(-15).dp)
                            .offset(x=(-25).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Cargar desde Galería",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp)) // Espacio entre los dos botones

                    // 2. BOTÓN DE CÁMARA (A la derecha, centrado)
                    // Usamos el ScanButton que ya tenías
                    Box(modifier = Modifier.offset(x = (-30).dp)){
                        ScanButton(onClick = {
                            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                isCameraOpen = true
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        })
                    }
                }
            }
        }
    }
}
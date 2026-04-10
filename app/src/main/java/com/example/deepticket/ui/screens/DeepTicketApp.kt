package com.example.deepticket.ui.screens

import androidx.compose.material.icons.filled.List
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

    // 1. VARIABLES DE SESIÓN (AQUÍ AGREGAMOS EL ID)
    var userName by remember { mutableStateOf("Usuario") }
    var userId by remember { mutableStateOf("") }

    // 2. EXTRAEMOS LOS DATOS CUANDO ABRE LA APP
    LaunchedEffect(Unit) {
        try {
            val user = com.example.deepticket.supabase.auth.currentUserOrNull()

            if (user != null) {
                // Guardamos el ID secreto para la base de datos
                userId = user.id

                // Extraemos el nombre para mostrarlo en la interfaz
                val nombreExtraido = user.userMetadata?.get("nombre")?.toString()?.replace("\"", "")
                if (!nombreExtraido.isNullOrBlank()) {
                    userName = nombreExtraido
                }
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
    // 3. Lanzador para la GALERÍA
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            coroutineScope.launch {
                Toast.makeText(context, "Analizando imagen de la galería...", Toast.LENGTH_LONG).show()

                // --- TRUCO PARA LA GALERÍA ---
                // Convertimos el enlace virtual (content://) a un archivo físico temporal
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile = java.io.File.createTempFile("galeria_", ".jpg", context.cacheDir)
                tempFile.outputStream().use { out -> inputStream?.copyTo(out) }
                val uriFisica = android.net.Uri.fromFile(tempFile)
                // -----------------------------

                // URL de tu servidor Python
                val miUrl = "https://knee-matchless-jittery.ngrok-free.dev/parse-ticket"

                // Llamamos a la misma función de red
                subirTicketAlServidor(
                    uriFoto = uriFisica,
                    urlServidor = miUrl,
                    userId = userId,
                    userName = userName
                ) { respuesta ->

                    // Regresamos al hilo principal de la pantalla
                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.Main) {

                        if (respuesta.startsWith("ÉXITO")) {
                            try {
                                // Leemos el JSON de Python
                                val jsonString = respuesta.substringAfter("ÉXITO: ")
                                val jsonObject = org.json.JSONObject(jsonString)

                                val datosExtraidos = jsonObject.getJSONObject("datos_extraidos")
                                val arrayProductos = datosExtraidos.getJSONArray("_productos")
                                val empresa = datosExtraidos.optString("Empresa", "Supermercado")

                                val nuevosProductos = mutableListOf<ProductItem>()

                                // Convertimos JSON a ProductItem
                                for (i in 0 until arrayProductos.length()) {
                                    val item = arrayProductos.getJSONObject(i)
                                    val precioLimpio = item.getString("Precio").replace("$", "").replace(",", "").toDoubleOrNull() ?: 0.0

                                    nuevosProductos.add(
                                        ProductItem(
                                            id = java.util.UUID.randomUUID().toString(),
                                            iconId = Icons.Default.List,
                                            name = item.getString("Producto"),
                                            brand = "Por clasificar",
                                            unit = item.getString("Cantidad") + " un.",
                                            category = "Nuevo",
                                            price = precioLimpio,
                                            date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                                            supermarket = empresa
                                        )
                                    )
                                }

                                // Agregamos a la base de datos de la UI
                                database.addAll(nuevosProductos)

                                Toast.makeText(context, "✅ Foto de Galería registrada. ${nuevosProductos.size} productos detectados.", Toast.LENGTH_LONG).show()

                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "⚠️ El ticket se procesó, pero hubo un error al leerlo en la App.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "❌ Error del servidor: $respuesta", Toast.LENGTH_LONG).show()
                        }
                    }
                }
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
        CameraScreen(
            onClose = { isCameraOpen = false },
            onPhotoTaken = { uriDeLaFoto ->
                // 1. Cierra la cámara inmediatamente
                isCameraOpen = false 
                
                // 2. Manda la foto y los datos del usuario al backend
                coroutineScope.launch {
                    Toast.makeText(context, "Analizando con IA...", Toast.LENGTH_LONG).show()
                    
                    val miUrl = "https://knee-matchless-jittery.ngrok-free.dev/parse-ticket"

                    subirTicketAlServidor(
                        uriFoto = uriDeLaFoto,
                        urlServidor = miUrl,
                        userId = userId,
                        userName = userName
                    ) { respuesta ->

                        // Como la petición de red ocurre en el fondo, regresamos al hilo principal para actualizar la interfaz
                        coroutineScope.launch(kotlinx.coroutines.Dispatchers.Main) {

                            if (respuesta.startsWith("ÉXITO")) {
                                try {
                                    // 1. Quitamos la palabra "ÉXITO: " para leer el JSON puro que mandó Python
                                    val jsonString = respuesta.substringAfter("ÉXITO: ")
                                    val jsonObject = org.json.JSONObject(jsonString)

                                    // 2. Extraemos los datos del ticket
                                    val datosExtraidos = jsonObject.getJSONObject("datos_extraidos")
                                    val arrayProductos = datosExtraidos.getJSONArray("_productos")
                                    val empresa = datosExtraidos.optString("Empresa", "Supermercado")

                                    val nuevosProductos = mutableListOf<ProductItem>()

                                    // 3. Convertimos el JSON de Python a objetos de Kotlin (ProductItem)
                                    for (i in 0 until arrayProductos.length()) {
                                        val item = arrayProductos.getJSONObject(i)

                                        // Limpiamos el precio (quitamos el signo de dólar y comas)
                                        val precioLimpio = item.getString("Precio").replace("$", "").replace(",", "").toDoubleOrNull() ?: 0.0

                                        nuevosProductos.add(
                                            ProductItem(
                                                id = java.util.UUID.randomUUID().toString(),
                                                iconId = Icons.Default.List,
                                                name = item.getString("Producto"),
                                                brand = "Por clasificar", // TheFuzz podría clasificar esto en el futuro
                                                unit = item.getString("Cantidad") + " un.",
                                                category = "Nuevo",
                                                price = precioLimpio,
                                                date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                                                supermarket = empresa
                                            )
                                        )
                                    }

                                    // 4. LA MAGIA DE COMPOSE: Agregamos los productos a la base de datos local
                                    // Esto hará que HomeScreen se actualice instantáneamente con los números correctos
                                    database.addAll(nuevosProductos)

                                    // 5. Mostramos el Toast de confirmación con la cuenta
                                    Toast.makeText(
                                        context,
                                        "✅ Ticket registrado. ${nuevosProductos.size} productos detectados.",
                                        Toast.LENGTH_LONG
                                    ).show()

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(context, "⚠️ El ticket fue procesado, pero hubo un error al leer los datos en la App.", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                // Si el servidor manda error 422 o 500
                                Toast.makeText(context, "❌ Error del servidor: $respuesta", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        )
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
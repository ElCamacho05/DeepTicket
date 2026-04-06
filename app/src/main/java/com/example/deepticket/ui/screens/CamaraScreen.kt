package com.example.deepticket.ui.screens

import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.deepticket.ui.theme.AppColors
import kotlinx.coroutines.launch

// Importamos la función que creamos en el archivo 2
import com.example.deepticket.validarTicket

@Composable
fun CameraScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                    } catch (exc: Exception) {
                        Toast.makeText(ctx, "Error cámara", Toast.LENGTH_SHORT).show()
                    }
                }, executor)
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = onClose,
            modifier = Modifier.padding(top = 40.dp, start = 20.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 50.dp)) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(8.dp)
                    .background(AppColors.orangeAccent, CircleShape)
                    .clickable {
                        Toast.makeText(context, "Validando...", Toast.LENGTH_SHORT).show()

                        coroutineScope.launch {
                            val codigoSimulado = "CA-2016-152156"
                            val ticket = validarTicket(codigoSimulado)

                            if (ticket != null) {
                                Toast.makeText(context, "✅ Válido: ${ticket.customername}", Toast.LENGTH_LONG).show()
                                // Aquí comprobamos que sí trae TODOS tus datos:
                                println("Edad: ${ticket.edad}, Género: ${ticket.genero}, Ingresos: $${ticket.ingresosanuales}, Producto: ${ticket.productname}")
                            } else {
                                Toast.makeText(context, "❌ TICKET INVÁLIDO", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
            )
        }
    }
}
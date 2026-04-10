package com.example.deepticket.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.deepticket.ui.theme.AppColors
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

@Composable
fun CameraScreen(onClose: () -> Unit, onPhotoTaken: (Uri) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // Capturador de imágenes real
    val imageCapture = remember { ImageCapture.Builder().build() }

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
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, imageCapture
                        )
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
                        Toast.makeText(context, "Tomando foto...", Toast.LENGTH_SHORT).show()

                        // Ejecutamos la toma de la foto
                        takePhoto(
                            context = context,
                            imageCapture = imageCapture,
                            executor = ContextCompat.getMainExecutor(context),
                            onImageSaved = { uri ->
                                // ¡LISTO! La foto se tomó, se la devolvemos a la pantalla principal
                                onPhotoTaken(uri) 
                            },
                            onError = {
                                Toast.makeText(context, "Error al tomar foto", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
            )
        }
    }
}

// -------------------------------------------------------------------
// FUNCIONES AUXILIARES (Van en el mismo archivo hasta abajo)
// -------------------------------------------------------------------

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onImageSaved: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val photoFile = File(
        context.externalCacheDir,
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onImageSaved(Uri.fromFile(photoFile))
            }
            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}

// Asegúrate de que la función NO sea "private" para que DeepTicketApp la pueda llamar.
// Cámbiala a "fun" en lugar de "private fun"
fun subirTicketAlServidor(
    uriFoto: Uri, 
    urlServidor: String, 
    userId: String,      // Nuevo parámetro
    userName: String,    // Nuevo parámetro
    onResult: (String) -> Unit
) {
    val client = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS) // Tiempo para conectar al server
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)   // Tiempo para enviar la foto
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)    // Tiempo para esperar a la IA
        .build()
    val file = File(uriFoto.path!!)

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "file",
            file.name,
            file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        )
        // AÑADIMOS LOS DATOS DEL USUARIO (Deben llamarse igual que en api.py: customer_id y customer_name)
        .addFormDataPart("customer_id", userId)
        .addFormDataPart("customer_name", userName)
        .build()

    val request = Request.Builder()
        .url(urlServidor)
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onResult("Error de red: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            val respuestaStr = response.body?.string() ?: "Sin respuesta"
            if (response.isSuccessful) {
                onResult("ÉXITO: $respuestaStr")
            } else {
                onResult("Error en servidor (${response.code}): $respuestaStr")
            }
        }
    })
}
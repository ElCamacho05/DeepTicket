package com.example.deepticket.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

// ─── Modelos para comunicarse con FastAPI (Python) ───────────────────────
@Serializable
data class RecommendationRequest(
    val user_id: String,
    val categorias_visibles: List<String>
)

@Serializable
data class RecommendationResponse(
    val recommendations: List<RecommendedProduct>
)

// ─── Resultado de una recomendación (Mantiene viva a tu UI actual) ────────
@Serializable
data class RecommendedProduct(
    val productName: String,
    val category: String,
    val score: Int
)

// ─── Repositorio Ligero ───────────────────────────────────────────────────
object RecommendationRepository {

    private val client = OkHttpClient()
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    // Configuramos el parser JSON para que no crashee si Python manda campos extra
    private val jsonParser = Json { ignoreUnknownKeys = true }

    suspend fun getRecommendations(
        userId: String,
        userProducts: List<ProductItem>,
        kNeighbors: Int = 10, // Se mantienen para que RecommendationViewModel no marque error
        topN: Int = 5         // Se mantienen para que RecommendationViewModel no marque error
    ): List<RecommendedProduct> = withContext(Dispatchers.IO) {
        try {
            // 1. Extraemos solo las categorías únicas del historial/pantalla actual del usuario
            val categoriasVisibles = userProducts.map { it.category }.distinct()

            // 2. Empaquetamos los datos en formato JSON para Python
            val requestData = RecommendationRequest(
                user_id = userId,
                categorias_visibles = categoriasVisibles
            )
            val jsonString = jsonParser.encodeToString(requestData)
            val requestBody = jsonString.toRequestBody(JSON_MEDIA_TYPE)

            // 3. DEFINIMOS LA URL (Asegúrate de poner aquí la misma de tu Ngrok activo)
            val miUrl = "https://knee-matchless-jittery.ngrok-free.dev/recommend"

            val request = Request.Builder()
                .url(miUrl)
                .post(requestBody)
                .build()

            // 4. Disparamos la petición a la Inteligencia Artificial
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBodyStr = response.body?.string()
                if (responseBodyStr != null) {
                    // 5. Convertimos la respuesta de Python en la lista para Android
                    val apiResponse = jsonParser.decodeFromString<RecommendationResponse>(responseBodyStr)
                    return@withContext apiResponse.recommendations
                }
            } else {
                println("❌ Error en el recomendador de Python: ${response.code}")
            }

            return@withContext emptyList()

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }
}
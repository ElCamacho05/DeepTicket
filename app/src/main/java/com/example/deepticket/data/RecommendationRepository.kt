package com.example.deepticket.data

import com.example.deepticket.supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.sqrt

// ─── Modelo de datos que mapea la tabla "tickets" de Supabase ───────────────
@Serializable
data class TicketRow(
    @SerialName("customer_id")    val customerId: String,
    @SerialName("product_name")   val productName: String,
    @SerialName("category")       val category: String,
    @SerialName("edad")           val edad: Int,
    @SerialName("genero")         val genero: String,
    @SerialName("ingresos_anuales") val ingresosAnuales: Double
)

// ─── Resultado de una recomendación ─────────────────────────────────────────
data class RecommendedProduct(
    val productName: String,
    val category: String,
    val score: Int          // veces que apareció en la "tribu" de vecinos
)

// ─── Repositorio principal ───────────────────────────────────────────────────
object RecommendationRepository {

    // Carga todos los tickets de Supabase (sin filtro de usuario para construir la matriz global)
    @OptIn(kotlinx.serialization.InternalSerializationApi::class)
    private suspend fun fetchAllTickets(): List<TicketRow> = withContext(Dispatchers.IO) {
        supabase.postgrest["tickets"]
            .select()
            .decodeList<TicketRow>()
    }

    // ─── Similitud coseno entre dos vectores (Map<String, Double>) ──────────
    private fun cosineSimilarity(a: Map<String, Double>, b: Map<String, Double>): Double {
        val keys = a.keys.intersect(b.keys)
        val dot   = keys.sumOf { (a[it] ?: 0.0) * (b[it] ?: 0.0) }
        val normA = sqrt(a.values.sumOf { it * it })
        val normB = sqrt(b.values.sumOf { it * it })
        return if (normA == 0.0 || normB == 0.0) 0.0 else dot / (normA * normB)
    }

    // ─── Normaliza un valor en [0,1] dado min y max ──────────────────────────
    private fun minMaxNorm(value: Double, min: Double, max: Double): Double =
        if (max == min) 0.0 else (value - min) / (max - min)

    /**
     * Función principal: dado el userId del usuario logueado y sus productos actuales,
     * devuelve una lista de RecommendedProduct usando KNN híbrido (compras + demográficos).
     *
     * @param userId          UUID del usuario logueado (supabase.auth user.id)
     * @param userProducts    Lista de ProductItem que el usuario ya tiene escaneados
     * @param kNeighbors      Cuántos vecinos consultar (default 10)
     * @param topN            Cuántas recomendaciones devolver (default 5)
     */
    suspend fun getRecommendations(
        userId: String,
        userProducts: List<ProductItem>,
        kNeighbors: Int = 10,
        topN: Int = 5
    ): List<RecommendedProduct> = withContext(Dispatchers.Default) {

        // 1. Traer datos de Supabase
        val allTickets = fetchAllTickets()
        if (allTickets.isEmpty()) return@withContext emptyList()

        // 2. Construir lista de categorías únicas (columnas de la matriz)
        val allCategories = allTickets.map { it.category }.distinct().sorted()

        // 3. Agrupar tickets por customer_id
        val byCustomer = allTickets.groupBy { it.customerId }

        // 4. Calcular rangos para normalización de edad e ingresos
        val ages    = byCustomer.values.map { rows -> rows.first().edad.toDouble() }
        val incomes = byCustomer.values.map { rows -> rows.first().ingresosAnuales }
        val ageMin = ages.min(); val ageMax = ages.max()
        val incMin = incomes.min(); val incMax = incomes.max()

        // 5. Construir vectores híbridos para cada cliente de la BD
        // Vector = categorías normalizadas + género (one-hot ×2) + edad_norm×2 + ingresos_norm×2
        val allGenders = allTickets.map { it.genero }.distinct()

        fun buildVector(
            categoryCounts: Map<String, Int>,   // cat → frecuencia
            edad: Double,
            ingresos: Double,
            genero: String,
            catMax: Map<String, Int>             // máximo por categoría (para normalizar)
        ): Map<String, Double> {
            val vec = mutableMapOf<String, Double>()
            // Compras normalizadas
            allCategories.forEach { cat ->
                val cnt = categoryCounts[cat] ?: 0
                val max = catMax[cat] ?: 1
                vec[cat] = cnt.toDouble() / max.toDouble()
            }
            // Demográficos (peso ×2 igual que el notebook)
            vec["Edad_Norm"]     = minMaxNorm(edad, ageMin, ageMax) * 2.0
            vec["Ingresos_Norm"] = minMaxNorm(ingresos, incMin, incMax) * 2.0
            allGenders.forEach { g ->
                vec["Gen_$g"] = (if (g == genero) 1.0 else 0.0) * 2.0
            }
            return vec
        }

        // Máximo de compras por categoría (para normalizar columnas de compras)
        val catMax: Map<String, Int> = allCategories.associateWith { cat ->
            byCustomer.values.maxOf { rows -> rows.count { it.category == cat } }
        }

        // Vectores de todos los clientes de la BD
        val customerVectors: Map<String, Map<String, Double>> = byCustomer.mapValues { (_, rows) ->
            val catCounts = rows.groupingBy { it.category }.eachCount()
            buildVector(catCounts, rows.first().edad.toDouble(), rows.first().ingresosAnuales, rows.first().genero, catMax)
        }

        // 6. Construir vector del usuario actual
        //    Sus categorías vienen de los ProductItems escaneados
        val userCatCounts = userProducts.groupingBy { it.category }.eachCount()

        // Datos demográficos del usuario: tomamos del primer ticket que tenga su customer_id
        //  (si no existe aún en la BD usamos valores medios para no excluirlo)
        val userRow = allTickets.firstOrNull { it.customerId == userId }
        val userEdad     = userRow?.edad?.toDouble()     ?: ((ageMin + ageMax) / 2)
        val userIngresos = userRow?.ingresosAnuales       ?: ((incMin + incMax) / 2)
        val userGenero   = userRow?.genero               ?: allGenders.firstOrNull() ?: ""

        val userVector = buildVector(userCatCounts, userEdad, userIngresos, userGenero, catMax)

        // 7. Calcular similitud coseno del usuario contra todos los clientes
        val similarities = customerVectors
            .filter { (cid, _) -> cid != userId }   // excluir al propio usuario
            .map { (cid, vec) -> cid to cosineSimilarity(userVector, vec) }
            .sortedByDescending { it.second }

        // 8. Tomar los K vecinos más cercanos
        val tribe = similarities.take(kNeighbors).map { it.first }

        // 9. Recolectar productos de la tribu que el usuario NO ha comprado
        val userProductNames = userProducts.map { it.name }.toSet()
        val tribeProducts = allTickets
            .filter { it.customerId in tribe && it.productName !in userProductNames }

        // 10. Votación: los más frecuentes en la tribu ganan
        val votes = tribeProducts
            .groupingBy { it.productName }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(topN)

        // 11. Mapear a RecommendedProduct con su categoría
        votes.map { (productName, score) ->
            val category = tribeProducts.first { it.productName == productName }.category
            RecommendedProduct(productName, category, score)
        }
    }
}
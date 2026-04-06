package com.example.deepticket.data

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class Ticket(
    val id: Int? = null,
    @SerialName("order_id") val orderid: String,
    @SerialName("customer_id") val customerid: String,
    @SerialName("customer_name") val customername: String,
    @SerialName("category") val category: String,
    @SerialName("sub_category") val subcategory: String,
    @SerialName("product_name") val productname: String,
    @SerialName("precio_total") val preciototal: Double,
    @SerialName("quantity") val quantity: Int,
    @SerialName("edad") val edad: Int,
    @SerialName("genero") val genero: String,
    @SerialName("ingresos_anuales") val ingresosanuales: Double,
    @SerialName("education") val education: String,
    @SerialName("marital_status") val maritalstatus: String,
    @SerialName("tipo_comercio") val tipocomercio: String
)
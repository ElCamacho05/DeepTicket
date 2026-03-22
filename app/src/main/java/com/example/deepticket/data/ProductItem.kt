package com.example.deepticket.data

import androidx.compose.ui.graphics.vector.ImageVector

data class ProductItem(
    val id: String,
    val iconId: ImageVector,
    val name: String,
    val brand: String,
    val unit: String,
    val category: String,
    val price: Double,
    val date: String,
    val supermarket: String
)
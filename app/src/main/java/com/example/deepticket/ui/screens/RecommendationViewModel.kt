package com.example.deepticket.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepticket.data.ProductItem
import com.example.deepticket.data.RecommendedProduct
import com.example.deepticket.data.RecommendationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RecommendationState {
    object Idle : RecommendationState()
    object Loading : RecommendationState()
    data class Success(val items: List<RecommendedProduct>) : RecommendationState()
    data class Error(val message: String) : RecommendationState()
    object Empty : RecommendationState()
}

class RecommendationViewModel : ViewModel() {

    private val _state = MutableStateFlow<RecommendationState>(RecommendationState.Idle)
    val state: StateFlow<RecommendationState> = _state

    fun loadRecommendations(userId: String, products: List<ProductItem>) {
        if (products.isEmpty()) {
            _state.value = RecommendationState.Empty
            return
        }
        viewModelScope.launch {
            _state.value = RecommendationState.Loading
            try {
                val results = RecommendationRepository.getRecommendations(
                    userId = userId,
                    userProducts = products,
                    kNeighbors = 10,
                    topN = 5
                )
                _state.value = if (results.isEmpty()) RecommendationState.Empty
                else RecommendationState.Success(results)
            } catch (e: Exception) {
                _state.value = RecommendationState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CartRepository
import kotlinx.coroutines.launch

class ARViewModel(private val cartRepository: CartRepository) : ViewModel() {

    // ── Model State ────────────────────────────────────────────────────────────

    sealed class ModelState {
        object Loading : ModelState()
        object Loaded : ModelState()
        data class Error(val message: String) : ModelState()
    }

    private val _modelState = MutableLiveData<ModelState>()
    val modelState: LiveData<ModelState> = _modelState

    private val _isModelPlaced = MutableLiveData<Boolean>(false)
    val isModelPlaced: LiveData<Boolean> = _isModelPlaced

    private val _trackingStatus = MutableLiveData<String>("")
    val trackingStatus: LiveData<String> = _trackingStatus

    // ── Cart Events ────────────────────────────────────────────────────────────

    sealed class CartEvent {
        object Success : CartEvent()
        data class Error(val message: String) : CartEvent()
    }

    private val _cartEvent = MutableLiveData<CartEvent>()
    val cartEvent: LiveData<CartEvent> = _cartEvent

    // ── Model Actions ──────────────────────────────────────────────────────────

    fun onModelLoading() {
        _modelState.value = ModelState.Loading
    }

    fun onModelLoaded() {
        _modelState.value = ModelState.Loaded
    }

    fun onModelError(message: String) {
        _modelState.value = ModelState.Error(message)
    }

    fun onModelPlaced() {
        _isModelPlaced.value = true
    }

    fun onModelReset() {
        _isModelPlaced.value = false
    }

    fun updateTrackingStatus(status: String) {
        _trackingStatus.value = status
    }

    // ── Cart Actions ───────────────────────────────────────────────────────────

    fun addToCart(productId: Int, quantity: Int) {
        viewModelScope.launch {
            try {
                cartRepository.addToCart(productId, quantity)
                _cartEvent.value = CartEvent.Success
            } catch (e: Exception) {
                _cartEvent.value = CartEvent.Error(e.message ?: "Failed to add to cart")
            }
        }
    }

    // ── Factory ────────────────────────────────────────────────────────────────

    class Factory(private val repository: CartRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ARViewModel::class.java)) {
                return ARViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

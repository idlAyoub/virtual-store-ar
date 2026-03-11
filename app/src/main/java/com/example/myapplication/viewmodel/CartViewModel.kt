package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CartRepository
import kotlinx.coroutines.launch

class CartViewModel(private val repository: CartRepository) : ViewModel() {
    val cartItems = repository.cartItems.asLiveData()
    val totalAmount = repository.cartItemCount.asLiveData()

    fun increaseQuantity(productId: Int, currentQuantity: Int) {
        viewModelScope.launch {
            repository.updateQuantity(productId, currentQuantity + 1)
        }
    }

    fun decreaseQuantity(productId: Int, currentQuantity: Int) {
        viewModelScope.launch {
            if (currentQuantity > 1) {
                repository.updateQuantity(productId, currentQuantity - 1)
            } else {
                repository.removeByProductId(productId)
            }
        }
    }

    fun removeItem(productId: Int) {
        viewModelScope.launch {
            repository.removeByProductId(productId)
        }
    }
}
package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Product
import com.example.myapplication.data.ProductRepository
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    private val productRepository: ProductRepository
    val favoriteProducts: LiveData<List<Product>>

    init {
        val db = AppDatabase.getDatabase(application)
        productRepository = ProductRepository(db.productDao())
        favoriteProducts = productRepository.getFavoriteProducts()
    }

    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            productRepository.toggleFavorite(product.id, !product.isFavorite)
        }
    }
}

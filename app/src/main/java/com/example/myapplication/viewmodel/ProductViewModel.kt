package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Product
import com.example.myapplication.data.ProductRepository
import kotlinx.coroutines.launch

/**
 * ProductViewModel - Responsible for managing product data and search logic
 *
 * This ViewModel:
 * - Fetches products from the ProductRepository
 * - Handles real-time search filtering
 * - Exposes product data as LiveData for UI observation
 * - Implements MVVM best practices for lifecycle-safe data delivery
 */
class ProductViewModel(application: Application) : AndroidViewModel(application) {

    // Repository instance for accessing product data
    private val productRepository: ProductRepository

    // LiveData holding the complete list of products from the database
    private val _allProducts: LiveData<List<Product>>

    // LiveData holding the filtered/searched product list
    private val _filteredProducts = MutableLiveData<List<Product>>()
    val filteredProducts: LiveData<List<Product>> = _filteredProducts

    // Search query state
    private val _searchQuery = MutableLiveData<String>("")

    init {
        // Initialize the database and repository
        val db = AppDatabase.getDatabase(application)
        productRepository = ProductRepository(db.productDao())

        // Get all products from repository
        _allProducts = productRepository.allProducts

        // Initialize filtered products with all products
        _allProducts.observeForever { products ->
            performSearch(products, _searchQuery.value ?: "")
        }
    }

    /**
     * Perform search filtering based on product name
     *
     * @param products The list of products to filter
     * @param query The search query string
     */
    private fun performSearch(products: List<Product>, query: String) {
        val filtered = if (query.isEmpty()) {
            products
        } else {
            products.filter { product ->
                product.name.contains(query, ignoreCase = true)
            }
        }
        _filteredProducts.value = filtered
    }

    /**
     * Update the search query and trigger filtering
     * This method is called from the Activity when the user types in the SearchView
     *
     * @param query The new search query
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        val allProducts = _allProducts.value ?: emptyList()
        performSearch(allProducts, query)
    }


    /**
     * Clear the search query and show all products
     */
    fun clearSearch() {
        setSearchQuery("")
    }
}


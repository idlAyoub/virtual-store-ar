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
 * ProductViewModel - Responsible for managing product data, search, and category filtering
 *
 * This ViewModel:
 * - Fetches products from the ProductRepository
 * - Handles real-time search filtering
 * - Handles category filtering (Tout, Chaises, Canapés, Tables, Éclairage)
 * - Exposes product data as LiveData for UI observation
 * - Implements MVVM best practices for lifecycle-safe data delivery
 */
class ProductViewModel(application: Application) : AndroidViewModel(application) {

    // Repository instance for accessing product data
    private val productRepository: ProductRepository

    // LiveData holding the complete list of products from the database
    private val _allProducts: LiveData<List<Product>>

    // Category filter state
    private val _selectedCategory = MutableLiveData<String>("Tout")
    val selectedCategory: LiveData<String> = _selectedCategory

    // LiveData holding the filtered/searched product list
    private val _filteredProducts = MutableLiveData<List<Product>>()
    val filteredProducts: LiveData<List<Product>> = _filteredProducts

    // Search query state
    private val _searchQuery = MutableLiveData<String>("")

    // Favorite products (observed separately)
    val favoriteProducts: LiveData<List<Product>>

    init {
        // Initialize the database and repository first
        val db = AppDatabase.getDatabase(application)
        productRepository = ProductRepository(db.productDao())

        // Now safe to initialize favoriteProducts
        favoriteProducts = productRepository.getFavoriteProducts()

        // Get all products
        _allProducts = productRepository.allProducts

        // Initialize filtered products
        _allProducts.observeForever { products ->
            performSearch(products, _searchQuery.value ?: "")
        }
    }

    /**
     * Perform search and category filtering
     *
     * @param products The list of products to filter
     * @param query The search query string
     */
    private fun performSearch(products: List<Product>, query: String) {
        val category = _selectedCategory.value ?: "Tout"

        // First filter by category
        var filtered = if (category == "Tout") {
            products
        } else {
            products.filter { product ->
                extractCategory(product.name) == category
            }
        }

        // Then filter by search query
        if (query.isNotEmpty()) {
            filtered = filtered.filter { product ->
                product.name.contains(query, ignoreCase = true)
            }
        }

        _filteredProducts.value = filtered
    }

    /**
     * Update the search query and trigger filtering
     *
     * @param query The new search query
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        val allProducts = _allProducts.value ?: emptyList()
        performSearch(allProducts, query)
    }

    /**
     * Update the selected category and trigger filtering
     *
     * @param category The selected category name
     */
    fun setCategory(category: String) {
        _selectedCategory.value = category
        val allProducts = _allProducts.value ?: emptyList()
        performSearch(allProducts, _searchQuery.value ?: "")
    }

    /**
     * Update product favorite status
     */
    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            productRepository.updateFavorite(product.id, !product.isFavorite)
        }
    }

    /**
     * Clear the search query and show all products
     */
    fun clearSearch() {
        setSearchQuery("")
    }

    /**
     * Toggle the favorite status of a product
     */
    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            productRepository.toggleFavorite(product.id, !product.isFavorite)
        }
    }

    /**
     * Extract category from product name
     */
    private fun extractCategory(productName: String): String {
        return when {
            productName.contains("Chair", ignoreCase = true) -> "Chaises"
            productName.contains("Sofa", ignoreCase = true) ||
                    productName.contains("Couch", ignoreCase = true) -> "Canapés"
            productName.contains("Lamp", ignoreCase = true) ||
                    productName.contains("Light", ignoreCase = true) -> "Éclairage"
            productName.contains("Table", ignoreCase = true) -> "Tables"
            else -> "Mobilier"
        }
    }
}

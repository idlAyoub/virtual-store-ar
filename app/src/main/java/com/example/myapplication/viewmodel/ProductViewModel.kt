package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Category
import com.example.myapplication.data.Product
import com.example.myapplication.data.ProductRepository
import kotlinx.coroutines.launch

/**
 * ProductViewModel - Responsible for managing product data, search, and category filtering
 *
 * This ViewModel:
 * - Fetches products from the ProductRepository
 * - Handles real-time search filtering
 * - Handles category filtering using database-backed Category entities
 * - Exposes product data as LiveData for UI observation
 * - Implements MVVM best practices for lifecycle-safe data delivery
 */
class ProductViewModel(application: Application) : AndroidViewModel(application) {

    // Repository instance for accessing product data
    private val productRepository: ProductRepository

    // LiveData holding the complete list of products from the database
    private val _allProducts: LiveData<List<Product>>

    // Categories loaded from the database
    val categories: LiveData<List<Category>>

    // Selected category ID — null means "All"
    private val _selectedCategoryId = MutableLiveData<Int?>(null)
    val selectedCategoryId: LiveData<Int?> = _selectedCategoryId

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

        // Load categories from database
        categories = db.categoryDao().getAllCategories()

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
        val categoryId = _selectedCategoryId.value

        // First filter by category (null = "All", show everything)
        var filtered = if (categoryId == null) {
            products
        } else {
            products.filter { product ->
                product.categoryId == categoryId
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
     * Update the selected category and trigger filtering.
     * Pass null to show all products ("All" chip).
     *
     * @param categoryId The selected category ID, or null for "All"
     */
    fun setCategory(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
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
}

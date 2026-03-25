package com.example.myapplication.data

import androidx.lifecycle.LiveData

class ProductRepository(private val productDao: ProductDao) {

    val allProducts: LiveData<List<Product>> = productDao.getProducts()

    fun getProductById(id: Int): LiveData<Product> {
        return productDao.getProductById(id)
    }

    fun searchProducts(query: String): LiveData<List<Product>> {
        return productDao.searchProducts(query)
    }

    suspend fun insertAll(products: List<Product>) {
        productDao.insertAll(products)
    }

    fun getFavoriteProducts(): LiveData<List<Product>> {
        return productDao.getFavoriteProducts()
    }

    suspend fun toggleFavorite(id: Int, isFavorite: Boolean) {
        productDao.updateFavorite(id, isFavorite)
    }
}
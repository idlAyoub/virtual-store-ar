package com.example.myapplication.data

import androidx.lifecycle.LiveData

class ProductRepository(private val productDao: ProductDao) {

    val allProducts: LiveData<List<Product>> = productDao.getProducts()

    fun getProductById(id: Int): LiveData<Product> {
        return productDao.getProductById(id)
    }

    suspend fun insertAll(products: List<Product>) {
        productDao.insertAll(products)
    }
}
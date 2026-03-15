package com.example.myapplication.data

import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

class CartRepository(private val cartDao: CartDao) {

    val cartItems: Flow<List<CartItem>> = cartDao.getAllCartItems()

    val cartItemCount: Flow<Int> = cartDao.getCartItemCount()

    val cartTotal: Flow<Double> = cartDao.getCartTotal()

    // ADD TO CART (safe transaction — checks stock before adding)
    @Transaction
    suspend fun addToCart(productId: Int, quantity: Int) {
        val existingItem = cartDao.getCartItemByProductId(productId)
        if (existingItem != null) {
            val newQuantity = existingItem.quantity + quantity
            cartDao.updateQuantityByProductId(productId, newQuantity)
        } else {
            cartDao.insertCartItem(CartItem(productId = productId, quantity = quantity))
        }
    }

    suspend fun removeFromCart(cartItem: CartItem) {
        cartDao.deleteCartItem(cartItem)
    }

    suspend fun removeByProductId(productId: Int) {
        cartDao.deleteByProductId(productId)
    }

    suspend fun updateQuantity(productId: Int, quantity: Int) {
        cartDao.updateQuantityByProductId(productId, quantity)
    }

    /**
     * Validate and place the order:
     * 1. Load all cart items
     * 2. For each cart item, verify stock is sufficient
     * 3. Deduct stock from the products table
     * 4. Clear the cart
     *
     * Returns a list of product names that were out of stock (empty = success).
     */
    @Transaction
    suspend fun validateAndPlaceOrder(productDao: ProductDao): List<String> {
        val cartItems = cartDao.getAllCartItemsOnce()
        val outOfStockItems = mutableListOf<String>()

        for (item in cartItems) {
            val product = productDao.getProductByIdOnce(item.productId) ?: continue
            if (product.stock < item.quantity) {
                outOfStockItems.add(product.name)
            }
        }

        // If any item is out of stock, abort without clearing cart
        if (outOfStockItems.isNotEmpty()) {
            return outOfStockItems
        }

        // Deduct stock for each item
        for (item in cartItems) {
            val product = productDao.getProductByIdOnce(item.productId) ?: continue
            val newStock = product.stock - item.quantity
            productDao.updateStock(item.productId, newStock)
        }

        // Clear cart after successful order
        cartDao.clearCart()
        return emptyList()
    }

    // Keep old validateOrder for backward compat (just clears cart)
    suspend fun validateOrder() {
        cartDao.clearCart()
    }
}
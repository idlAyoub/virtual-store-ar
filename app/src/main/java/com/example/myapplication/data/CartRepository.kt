package com.example.myapplication.data

import kotlinx.coroutines.flow.Flow

class CartRepository(private val cartDao: CartDao) {

    // READ DATA

    val cartItems: Flow<List<CartItem>> = cartDao.getAllCartItems()

    val totalPrice: Flow<Double> = cartDao.getCartTotal()

    val totalQuantity: Flow<Int> = cartDao.getTotalQuantity()

    val cartItemCount: Flow<Int> = cartDao.getCartItemCount()

    val isCartEmpty: Flow<Boolean> = cartDao.isCartEmpty()

    // ADD ITEM TO CART

    suspend fun addToCart(productId: Int, quantity: Int) {

        val existingItem = cartDao.getCartItemByProductId(productId)

        if (existingItem != null) {
            val newQuantity = existingItem.quantity + quantity
            cartDao.updateQuantityByProductId(productId, newQuantity)
        } else {
            val cartItem = CartItem(
                productId = productId,
                quantity = quantity
            )
            cartDao.insertCartItem(cartItem)
        }
    }

    // REMOVE ITEM

    suspend fun removeFromCart(cartItem: CartItem) {
        cartDao.deleteCartItem(cartItem)
    }

    suspend fun removeByProductId(productId: Int) {
        cartDao.deleteByProductId(productId)
    }

    // UPDATE QUANTITY

    suspend fun updateQuantity(productId: Int, quantity: Int) {
        cartDao.updateQuantityByProductId(productId, quantity)
    }

    // ORDER VALIDATION (Clear Cart)

    suspend fun validateOrder() {
        cartDao.clearCart()
    }
}
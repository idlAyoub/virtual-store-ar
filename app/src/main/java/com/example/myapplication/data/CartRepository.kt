package com.example.myapplication.data

import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

class CartRepository(private val cartDao: CartDao) {

    val cartItems: Flow<List<CartItem>> = cartDao.getAllCartItems()

    val cartItemCount: Flow<Int> = cartDao.getCartItemCount()


    val cartTotal: Flow<Double> = cartDao.getCartTotal()

    // ADD TO CART (transaction sécurisée)

    @Transaction
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

    suspend fun removeFromCart(cartItem: CartItem) {
        cartDao.deleteCartItem(cartItem)
    }

    suspend fun removeByProductId(productId: Int) {
        cartDao.deleteByProductId(productId)
    }

    suspend fun updateQuantity(productId: Int, quantity: Int) {
        cartDao.updateQuantityByProductId(productId, quantity)
    }

    suspend fun validateOrder() {
        cartDao.clearCart()
    }
}
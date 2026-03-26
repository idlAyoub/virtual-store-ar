package com.example.myapplication.ui.cart

import com.example.myapplication.data.CartItem
import com.example.myapplication.data.Product

/**
 * Pairs a [CartItem] with its resolved [Product] so the adapter has
 * everything it needs without extra DAO calls at bind time.
 */
data class CartItemUiModel(
    val cartItem: CartItem,
    val product: Product
)
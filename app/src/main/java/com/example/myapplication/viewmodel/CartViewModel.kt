package com.example.myapplication.ui.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CartItem
import com.example.myapplication.data.CartRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * CartViewModel – Task D2
 *
 * Exposes cart items, live total, and item count to the UI.
 * All writes go through the CartRepository; the UI only observes LiveData.
 */
class CartViewModel(private val repository: CartRepository) : ViewModel() {

    // ── Observed Data ──────────────────────────────────────────────────────────

    /** Raw list of CartItem rows. The adapter binds directly to this. */
    val cartItems: LiveData<List<CartItem>> = repository.cartItems.asLiveData()

    /** Total number of distinct product lines in the cart. */
    val itemCount: LiveData<Int> = repository.cartItemCount.asLiveData()

    /** Whether the cart has no items (drives empty-state visibility). */
    val isEmpty: LiveData<Boolean> = repository.cartItems
        .map { it.isEmpty() }
        .asLiveData()

    // ── Pricing ────────────────────────────────────────────────────────────────

    /**
     * Subtotal computed from CartRepository's JOIN query.
     * Reacts automatically whenever quantities or products change.
     */
    val subtotal: LiveData<Double> = repository.cartTotal.asLiveData()

    /**
     * Shipping cost (flat rate).
     */
    val shipping: LiveData<Double> = repository.cartTotal
        .map { FLAT_SHIPPING_COST }
        .asLiveData()

    /**
     * Tax calculated as percentage of subtotal.
     */
    val tax: LiveData<Double> = repository.cartTotal
        .map { sub -> sub * TAX_RATE }
        .asLiveData()

    /**
     * Grand total = subtotal + shipping + tax.
     * Re-emits whenever any component changes.
     */
    val grandTotal: LiveData<Double> = repository.cartTotal
        .map { sub -> sub + FLAT_SHIPPING_COST + (sub * TAX_RATE) }
        .asLiveData()

    // ── Cart Mutation Actions ──────────────────────────────────────────────────

    /** Increment quantity by 1. Capped at MAX_QUANTITY per item. */
    fun increaseQuantity(cartItem: CartItem) {
        if (cartItem.quantity >= MAX_QUANTITY) return
        viewModelScope.launch {
            repository.updateQuantity(cartItem.productId, cartItem.quantity + 1)
        }
    }

    /**
     * Decrement quantity by 1.
     * If quantity reaches 0 the item is removed from the cart entirely.
     */
    fun decreaseQuantity(cartItem: CartItem) {
        viewModelScope.launch {
            if (cartItem.quantity <= 1) {
                repository.removeByProductId(cartItem.productId)
            } else {
                repository.updateQuantity(cartItem.productId, cartItem.quantity - 1)
            }
        }
    }

    /** Remove a specific item regardless of quantity. */
    fun removeItem(cartItem: CartItem) {
        viewModelScope.launch {
            repository.removeFromCart(cartItem)
        }
    }

    /** Clear the entire cart (called after successful order placement). */
    fun clearCart() {
        viewModelScope.launch {
            repository.validateOrder()
        }
    }

    // ── Constants ──────────────────────────────────────────────────────────────

    companion object {
        const val FLAT_SHIPPING_COST = 10.0
        const val TAX_RATE = 0.10  // 10% tax
        private const val MAX_QUANTITY = 99
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory(private val repository: CartRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
                return CartViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
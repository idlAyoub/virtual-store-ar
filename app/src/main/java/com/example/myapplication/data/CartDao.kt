package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface CartDao {

    // INSERT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem): Long

    // DELETE
    @Delete
    suspend fun deleteCartItem(cartItem: CartItem)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteByProductId(productId: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    // UPDATE
    @Update
    suspend fun updateQuantity(cartItem: CartItem)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE productId = :productId")
    suspend fun updateQuantityByProductId(productId: Int, quantity: Int)

    // READ (Flow — for live UI observation)
    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartItem>>

    // READ (suspend — for one-shot operations like order validation)
    @Query("SELECT * FROM cart_items")
    suspend fun getAllCartItemsOnce(): List<CartItem>

    @Query("SELECT * FROM cart_items WHERE productId = :productId LIMIT 1")
    suspend fun getCartItemByProductId(productId: Int): CartItem?

    @Query("SELECT * FROM cart_items WHERE id = :cartItemId LIMIT 1")
    suspend fun getCartItemById(cartItemId: Int): CartItem?

    @Query("SELECT COUNT(*) FROM cart_items")
    fun getCartItemCount(): Flow<Int>

    // CALCULATIONS
    @Query("""
        SELECT COALESCE(SUM(p.price * c.quantity), 0.0) as total
        FROM cart_items c
        INNER JOIN products p ON c.productId = p.id
    """)
    fun getCartTotal(): Flow<Double>

    @Query("""
        SELECT c.productId, (p.price * c.quantity) as subtotal
        FROM cart_items c
        INNER JOIN products p ON c.productId = p.id
    """)
    fun getItemSubtotals(): Flow<List<CartItemSubtotal>>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart_items")
    fun getTotalQuantity(): Flow<Int>

    @Query("SELECT COUNT(*) = 0 FROM cart_items")
    fun isCartEmpty(): Flow<Boolean>
}


data class CartItemSubtotal(
    val productId: Int,
    val subtotal: Double
)
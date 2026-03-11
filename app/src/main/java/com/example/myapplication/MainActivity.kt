package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.CartRepository
import com.example.myapplication.data.DataSeeder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.content.Intent

class MainActivity : ComponentActivity() {

    private lateinit var repository: CartRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(this)
        repository = CartRepository(db.cartDao())

        lifecycleScope.launch {
            if (db.productDao().getProductCount() == 0) {
                db.productDao().insertAll(DataSeeder.getProductList())
            }
            runDatabaseValidation()
        }

        // TEMP TEST - remove after testing
        val testIntent = Intent(this, OrderSummaryActivity::class.java)
        testIntent.putExtra("ITEM_TOTAL", 180.0)
        startActivity(testIntent)
    }

    private fun logResult(testName: String, success: Boolean) {
        if (success) {
            Log.d("DB_TEST", "PASS: $testName")
        } else {
            Log.e("DB_TEST", "FAIL: $testName")
        }
    }

    private suspend fun runDatabaseValidation() {

        repository.validateOrder()

        val emptyCount = repository.cartItemCount.first()
        logResult("Cart starts empty", emptyCount == 0)

        repository.addToCart(1, 2)

        val countAfterInsert = repository.cartItemCount.first()
        logResult("Insert item", countAfterInsert == 1)

        repository.addToCart(1, 3)

        val items = repository.cartItems.first()
        val quantityCorrect = items.firstOrNull()?.quantity == 5
        logResult("Add same product increases quantity", quantityCorrect)

        repository.updateQuantity(1, 10)

        val updatedItems = repository.cartItems.first()
        val updateCorrect = updatedItems.firstOrNull()?.quantity == 10
        logResult("Update quantity", updateCorrect)

        repository.removeByProductId(1)

        val countAfterDelete = repository.cartItemCount.first()
        logResult("Delete item", countAfterDelete == 0)

        repository.addToCart(1, 1)
        repository.addToCart(2, 2)

        val multiCount = repository.cartItemCount.first()
        logResult("Multiple items inserted", multiCount == 2)

        repository.validateOrder()

        val finalCount = repository.cartItemCount.first()
        logResult("Clear cart", finalCount == 0)
    }



}
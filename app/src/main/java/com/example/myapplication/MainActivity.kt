package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.CartRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var repository: CartRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(this)
        repository = CartRepository(db.cartDao())

        runDatabaseValidation()
    }

    private fun logResult(testName: String, success: Boolean) {
        if (success) {
            Log.d("DB_TEST", "PASS: $testName")
        } else {
            Log.e("DB_TEST", "FAIL: $testName")
        }
    }

    private fun runDatabaseValidation() {

        lifecycleScope.launch {

            // Start with empty cart
            //repository.validateOrder()




            val countAfterDelete = repository.cartItemCount.first()
            logResult("Delete item", countAfterDelete == 0)

            // MULTIPLE INSERTS
            repository.addToCart(1, 1)
            repository.addToCart(2, 2)

            val multiCount = repository.cartItemCount.first()
            logResult("Multiple items inserted", multiCount == 2)

            repository.removeByProductId(1)


            val finalCount = repository.cartItemCount.first()
            logResult("Clear cart", finalCount == 0)
        }
    }
}
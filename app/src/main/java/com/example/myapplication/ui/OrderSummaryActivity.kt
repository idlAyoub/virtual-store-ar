package com.example.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.CartRepository
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import com.example.myapplication.R
import kotlinx.coroutines.launch

class OrderSummaryActivity : ComponentActivity() {

    private lateinit var cartRepository: CartRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_summary)

        val db = AppDatabase.getDatabase(this)
        cartRepository = CartRepository(db.cartDao())

        // Total passed from CartActivity (items only — no tax/discount)
        val total = intent.getDoubleExtra("ITEM_TOTAL", 0.0)
        val alreadyValidated = intent.getBooleanExtra("ORDER_ALREADY_VALIDATED", false)

        findViewById<TextView>(R.id.tvTotal).text = "$${"%.2f".format(total)}"

        // Place order: verify stock, deduct, clear cart
        lifecycleScope.launch {
            if (alreadyValidated) {
                // Already validated in CartActivity — proceed directly to clearing cart
                cartRepository.validateOrder()
                Toast.makeText(this@OrderSummaryActivity, "Order placed successfully!", Toast.LENGTH_SHORT).show()
            } else {
                // Run validation again (fallback for direct access to OrderSummary)
                val outOfStock = cartRepository.validateAndPlaceOrder(db.productDao())
                if (outOfStock.isNotEmpty()) {
                    // Some items ran out of stock — inform user and go back
                    Toast.makeText(
                        this@OrderSummaryActivity,
                        "Out of stock: ${outOfStock.joinToString(", ")}. Please update your cart.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                    return@launch
                }
                // Order placed successfully
                Toast.makeText(this@OrderSummaryActivity, "Order placed successfully!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btnContinueShopping).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
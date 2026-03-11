package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.CartRepository
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import kotlinx.coroutines.launch

class OrderSummaryActivity : ComponentActivity() {

    private lateinit var cartRepository: CartRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_summary)

        val db = AppDatabase.getDatabase(this)
        cartRepository = CartRepository(db.cartDao())

        // Get data passed from CartActivity
        val itemTotal = intent.getDoubleExtra("ITEM_TOTAL", 0.0)
        val tax = 5.0
        val discount = 0.0
        val total = itemTotal + tax - discount

        // Bind views
        findViewById<TextView>(R.id.tvItemTotal).text = "$${"%.2f".format(itemTotal)}"
        findViewById<TextView>(R.id.tvTax).text = "$${"%.2f".format(tax)}"
        findViewById<TextView>(R.id.tvDiscount).text = "$${"%.2f".format(discount)}"
        findViewById<TextView>(R.id.tvTotal).text = "$${"%.2f".format(total)}"

        // Clear cart when order is confirmed
        lifecycleScope.launch {
            cartRepository.validateOrder()
        }

        Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()

        findViewById<MaterialButton>(R.id.btnContinueShopping).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // View Order Details → stay on this screen for now
        // Will be wired up once other members finish their activities
        findViewById<MaterialButton>(R.id.btnViewOrderDetails).setOnClickListener {
            Toast.makeText(this, "Order details coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
}
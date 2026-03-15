package com.example.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.CartRepository
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class ProductDetailActivity : ComponentActivity() {

    private lateinit var cartRepository: CartRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val db = AppDatabase.getDatabase(this)
        cartRepository = CartRepository(db.cartDao())

        val productId = intent.getIntExtra("PRODUCT_ID", -1)
        if (productId == -1) {
            finish()
            return
        }

        // Back button
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        // Observe product from DB
        db.productDao().getProductById(productId).observe(this) { product ->
            if (product == null) return@observe

            // Image - Get drawable resource ID from image name
            val resourceId = this.resources.getIdentifier(
                product.imageResource,
                "drawable",
                this.packageName
            )

            if (resourceId != 0) {
                Glide.with(this)
                    .load(resourceId)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(findViewById(R.id.ivProductDetail))
            } else {
                Glide.with(this)
                    .load(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(findViewById(R.id.ivProductDetail))
            }

            // Name & Price
            findViewById<TextView>(R.id.tvDetailName).text = product.name
            findViewById<TextView>(R.id.tvDetailPrice).text = String.format("$%.2f", product.price)
            findViewById<TextView>(R.id.tvDetailDescription).text = product.description

            // Stock
            val tvStock = findViewById<TextView>(R.id.tvDetailStock)
            if (product.stock > 0) {
                tvStock.text = "In Stock (${product.stock} left)"
                tvStock.setTextColor(getColor(android.R.color.holo_green_dark))
            } else {
                tvStock.text = "Out of Stock"
                tvStock.setTextColor(getColor(android.R.color.holo_red_dark))
            }

            // AR Button — show only if model available
            val btnAR = findViewById<MaterialButton>(R.id.btnViewInAR)
            val arSection = findViewById<LinearLayout>(R.id.arSection)
            if (product.arModelResource.isNotEmpty()) {
                arSection.visibility = View.VISIBLE
                btnAR.setOnClickListener {
                    val intent = Intent(this, ARViewActivity::class.java)
                    intent.putExtra("AR_MODEL", product.arModelResource)
                    intent.putExtra("PRODUCT_NAME", product.name)
                    intent.putExtra("PRODUCT_ID", product.id)
                    startActivity(intent)
                }
            } else {
                arSection.visibility = View.GONE
            }

            // Add to Cart
            val btnAddToCart = findViewById<MaterialButton>(R.id.btnDetailAddToCart)
            btnAddToCart.isEnabled = product.stock > 0
            btnAddToCart.setOnClickListener {
                if (product.stock <= 0) {
                    Toast.makeText(this, "This product is out of stock.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                lifecycleScope.launch {
                    cartRepository.addToCart(product.id, 1)
                    Toast.makeText(
                        this@ProductDetailActivity,
                        "${product.name} added to cart!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
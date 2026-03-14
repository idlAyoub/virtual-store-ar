package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.CartRepository
import com.example.myapplication.data.DataSeeder
import com.example.myapplication.ui.adapter.ProductAdapter
import com.example.myapplication.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

/**
 * MainActivity - Modern e-commerce catalog with search and grid layout
 *
 * Features:
 * - Header with app title and cart badge
 * - Search bar
 * - 2-column product grid layout
 * - Real-time product filtering
 * - Cart item counter badge
 */
class MainActivity : ComponentActivity() {

    private lateinit var productViewModel: ProductViewModel
    private lateinit var productAdapter: ProductAdapter
    private lateinit var cartRepository: CartRepository
    private lateinit var tvCartBadge: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize database and repositories
        val db = AppDatabase.getDatabase(this)
        cartRepository = CartRepository(db.cartDao())

        // Initialize ViewModel
        productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)

        // Seed database with initial products if empty
        lifecycleScope.launch {
            if (db.productDao().getProductCount() == 0) {
                db.productDao().insertAll(DataSeeder.getProductList())
            }
        }

        // Set up UI components
        setupRecyclerView()
        setupSearchBar()
        setupCartIcon()
        observeProducts()
        observeCartUpdates()
    }

    /**
     * Initialize RecyclerView with 2-column GridLayoutManager
     */
    private fun setupRecyclerView() {
        val rvProducts = findViewById<RecyclerView>(R.id.rvProducts)
        productAdapter = ProductAdapter(
            onAddToCartClick = { product ->
                // Handle add to cart with quantity 1
                lifecycleScope.launch {
                    cartRepository.addToCart(product.id, 1)
                }
            }
        )
        rvProducts.adapter = productAdapter
        rvProducts.layoutManager = GridLayoutManager(this, 2)
    }

    /**
     * Set up search bar with EditText text change listener
     */
    private fun setupSearchBar() {
        val etSearch = findViewById<EditText>(R.id.etSearch)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Update ViewModel with search query
                productViewModel.setSearchQuery(s?.toString() ?: "")
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


    /**
     * Set up cart icon and badge
     */
    private fun setupCartIcon() {
        val cartIconContainer = findViewById<FrameLayout>(R.id.cartIconContainer)
        tvCartBadge = findViewById(R.id.tvCartBadge)

        cartIconContainer.setOnClickListener {
            // Navigate to cart (will be implemented by Member 4)
            // startActivity(Intent(this, CartActivity::class.java))
        }
    }

    /**
     * Observe filtered products from ViewModel
     */
    private fun observeProducts() {
        productViewModel.filteredProducts.observe(this) { filteredProducts ->
            productAdapter.updateProductList(filteredProducts)
        }
    }

    /**
     * Observe cart updates and update badge count
     */
    private fun observeCartUpdates() {
        lifecycleScope.launch {
            cartRepository.cartItemCount.collect { count ->
                updateCartBadge(count)
            }
        }
    }

    /**
     * Update cart badge with item count
     */
    private fun updateCartBadge(count: Int) {
        if (count > 0) {
            tvCartBadge.text = count.toString()
            tvCartBadge.visibility = android.view.View.VISIBLE
        } else {
            tvCartBadge.visibility = android.view.View.GONE
        }
    }

}
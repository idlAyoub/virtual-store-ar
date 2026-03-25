package com.example.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.CartRepository
import com.example.myapplication.data.DataSeeder
import com.example.myapplication.ui.adapter.ProductAdapter
import com.example.myapplication.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

/**
 * MainActivity - Modern e-commerce catalog with search, category filter, and grid layout
 *
 * Features:
 * - Header with app title and cart badge
 * - Search bar
 * - Category filter chips (Tout, Chaises, Canapés, Tables, Éclairage)
 * - 2-column product grid layout
 * - Real-time product filtering
 * - Cart item counter badge
 */
class MainActivity : ComponentActivity() {

    private lateinit var productViewModel: ProductViewModel
    private lateinit var productAdapter: ProductAdapter
    private lateinit var cartRepository: CartRepository
    private lateinit var tvCartBadge: TextView

    private val categories = listOf("Tout", "Chaises", "Canapés", "Tables", "Éclairage", "Mobilier")
    private val chipViews = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val settingsPrefs = getSharedPreferences("settings_prefs", android.content.Context.MODE_PRIVATE)
        val isDarkMode = settingsPrefs.getBoolean("dark_mode_enabled", false)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize database and repositories
        val db = AppDatabase.getDatabase(this)
        cartRepository = CartRepository(db.cartDao())

        // Initialize ViewModel
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        // Seed database with initial products if empty
        lifecycleScope.launch {
            if (db.productDao().getProductCount() == 0) {
                db.productDao().insertAll(DataSeeder.getProductList())
            }
        }

        // Set up UI components
        setupRecyclerView()
        setupSearchBar()
        setupCategoryChips()
        setupCartIcon()
        observeProducts()
        observeCartUpdates()

        // Setup Drawer Layout
        val drawerLayout = findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawerLayout)
        val navigationView = findViewById<com.google.android.material.navigation.NavigationView>(R.id.navigationView)

        findViewById<View>(R.id.btnMenu).setOnClickListener {
            drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
                    true
                }
                else -> false
            }
        }
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
            },
            onFavoriteClick = { product ->
                productViewModel.toggleFavorite(product)
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
     * Set up category filter chips dynamically
     */
    private fun setupCategoryChips() {
        val chipContainer = findViewById<LinearLayout>(R.id.chipContainer)

        categories.forEachIndexed { index, category ->
            val chip = TextView(this).apply {
                text = category
                textSize = 14f
                setPadding(
                    resources.getDimensionPixelSize(R.dimen.chip_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.chip_padding_vertical),
                    resources.getDimensionPixelSize(R.dimen.chip_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.chip_padding_vertical)
                )
                minWidth = resources.getDimensionPixelSize(R.dimen.chip_min_width)
                gravity = android.view.Gravity.CENTER
                isClickable = true
                isFocusable = true

                setOnClickListener {
                    productViewModel.setCategory(category)
                    updateChipSelection(category)
                }
            }

            // Add margin between chips
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            if (index > 0) {
                params.marginStart = resources.getDimensionPixelSize(R.dimen.chip_gap)
            }
            chip.layoutParams = params

            chipViews.add(chip)
            chipContainer.addView(chip)
        }

        // Set initial selection
        updateChipSelection("Tout")
    }

    /**
     * Update chip visual selection state
     */
    private fun updateChipSelection(selectedCategory: String) {
        chipViews.forEach { chip ->
            if (chip.text == selectedCategory) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected)
                chip.setTextColor(ContextCompat.getColor(this, R.color.color_white))
                chip.setTypeface(chip.typeface, android.graphics.Typeface.BOLD)
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected)
                chip.setTextColor(ContextCompat.getColor(this, R.color.color_text_primary))
                chip.setTypeface(chip.typeface, android.graphics.Typeface.NORMAL)
            }
        }
    }

    /**
     * Set up cart icon and badge
     */
    private fun setupCartIcon() {
        val cartIconContainer = findViewById<FrameLayout>(R.id.cartIconContainer)
        tvCartBadge = findViewById(R.id.tvCartBadge)

        cartIconContainer.setOnClickListener {
            // Navigate to cart
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    /**
     * Observe filtered products from ViewModel
     */
    private fun observeProducts() {
        val rvProducts = findViewById<RecyclerView>(R.id.rvProducts)
        val layoutEmptyState = findViewById<View>(R.id.layoutEmptyState)
        
        productViewModel.filteredProducts.observe(this) { filteredProducts ->
            productAdapter.updateProductList(filteredProducts)
            if (filteredProducts.isEmpty()) {
                rvProducts.visibility = View.GONE
                layoutEmptyState.visibility = View.VISIBLE
            } else {
                rvProducts.visibility = View.VISIBLE
                layoutEmptyState.visibility = View.GONE
            }
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
            tvCartBadge.visibility = View.VISIBLE
        } else {
            tvCartBadge.visibility = View.GONE
        }
    }

}
package com.example.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.transition.TransitionManager
import com.google.android.material.navigation.NavigationView
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
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tvAppTitle: TextView
    private lateinit var searchContainer: View
    private lateinit var etSearch: EditText
    private lateinit var emptyStateSearch: View
    private var isSearchExpanded = false

    private val categories = listOf("Tout", "Chaises", "Canapés", "Tables", "Éclairage", "Mobilier")
    private val chipViews = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
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
        setupNavigationDrawer()
        setupTopBar()
        setupRecyclerView()
        setupSearchBar()
        setupCategoryChips()
        setupCartIcon()
        observeProducts()
        observeCartUpdates()
    }

    /**
     * Set up the Navigation Drawer and its items
     */
    private fun setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.navView)

        // Setup Close Button in Drawer Header
        if (navView.headerCount > 0) {
            val headerView = navView.getHeaderView(0)
            headerView.findViewById<ImageView>(R.id.ivCloseDrawer)?.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                }
                R.id.nav_orders -> {
                    startActivity(Intent(this, CartActivity::class.java))
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    /**
     * Set up top bar icons and clicks with animation logic
     */
    private fun setupTopBar() {
        val ivMenu = findViewById<ImageView>(R.id.ivMenu)
        val ivSearchIcon = findViewById<ImageView>(R.id.ivSearchIcon)
        val ivFavorite = findViewById<ImageView>(R.id.ivFavorite)

        tvAppTitle = findViewById(R.id.tvAppTitle)
        searchContainer = findViewById(R.id.searchContainer)
        etSearch = findViewById(R.id.etSearch)
        emptyStateSearch = findViewById(R.id.emptyStateSearch)

        ivMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        ivSearchIcon.setOnClickListener {
            toggleSearch(!isSearchExpanded)
        }

        ivFavorite.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }
    }

    /**
     * Smoothly toggle between app title and search bar
     */
    private fun toggleSearch(expand: Boolean) {
        if (isSearchExpanded == expand) return
        isSearchExpanded = expand

        val ivSearchIcon = findViewById<ImageView>(R.id.ivSearchIcon)
        val middleSection = findViewById<ViewGroup>(R.id.middleSection)

        // Apply TransitionManager for smooth layout changes (width)
        TransitionManager.beginDelayedTransition(middleSection)

        if (expand) {
            // 1. App Title: Fade out and slide left
            tvAppTitle.animate()
                .alpha(0f)
                .translationX(-20f)
                .setDuration(250)
                .start()

            // 2. Search Container: Expand width and fade in
            searchContainer.visibility = View.VISIBLE
            val params = searchContainer.layoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            searchContainer.layoutParams = params

            searchContainer.animate()
                .alpha(1f)
                .setDuration(300)
                .start()

            // 3. Search Icon: Turn green
            ivSearchIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_success))

            // 4. Keyboard: Focus and show
            etSearch.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT)

        } else {
            // 1. App Title: Fade in and slide back
            tvAppTitle.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(250)
                .start()

            // 2. Search Container: Collapse width and fade out
            searchContainer.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    searchContainer.visibility = View.GONE
                    val params = searchContainer.layoutParams
                    params.width = 0
                    searchContainer.layoutParams = params
                }
                .start()

            // 3. Search Icon: Default color
            ivSearchIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_text_primary))

            // 4. Keyboard: Hide
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(etSearch.windowToken, 0)

            // Optional: clear search on close
            // etSearch.setText("")
        }
    }

    /**
     * Initialize RecyclerView with 2-column GridLayoutManager
     */
    private fun setupRecyclerView() {
        val rvProducts = findViewById<RecyclerView>(R.id.rvProducts)
        productAdapter = ProductAdapter(
            onAddToCartClick = { product ->
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
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    /**
     * Observe product data and update UI
     */
    private fun observeProducts() {
        productViewModel.filteredProducts.observe(this) { products ->
            productAdapter.updateProductList(products)

            if (products.isEmpty()) {
                emptyStateSearch.visibility = View.VISIBLE
            } else {
                emptyStateSearch.visibility = View.GONE
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
     * Update cart badge with item count and pulse animation
     */
    private fun updateCartBadge(count: Int) {
        val previousVisibility = tvCartBadge.visibility
        if (count > 0) {
            tvCartBadge.text = count.toString()
            tvCartBadge.visibility = View.VISIBLE
            
            // Animation: Pop pulse when count changes or badge first appears
            tvCartBadge.animate()
                .scaleX(1.4f)
                .scaleY(1.4f)
                .setDuration(150)
                .withEndAction {
                    tvCartBadge.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .start()
                }
                .start()
        } else {
            tvCartBadge.visibility = View.GONE
        }
    }

}
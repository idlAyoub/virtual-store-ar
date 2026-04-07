package com.example.myapplication.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.CartRepository
import com.example.myapplication.ui.adapter.ProductAdapter
import com.example.myapplication.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

class FavoritesActivity : ComponentActivity() {

    private lateinit var productViewModel: ProductViewModel
    private lateinit var productAdapter: ProductAdapter
    private lateinit var cartRepository: CartRepository
    private lateinit var emptyStateFavorites: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        // Initialize database and repository for cart (needed for adapter callback)
        val db = AppDatabase.getDatabase(this)
        cartRepository = CartRepository(db.cartDao())

        // Initialize ViewModel
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        // Set up UI components
        setupTopBar()
        setupRecyclerView()
        observeFavorites()
    }

    private fun setupTopBar() {
        val ivBack = findViewById<ImageView>(R.id.ivBack)
        emptyStateFavorites = findViewById(R.id.emptyStateFavorites)

        ivBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        val rvFavorites = findViewById<RecyclerView>(R.id.rvFavorites)
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
        rvFavorites.adapter = productAdapter
        rvFavorites.layoutManager = GridLayoutManager(this, 2)
    }

    private fun observeFavorites() {
        productViewModel.favoriteProducts.observe(this) { favorites ->
            productAdapter.updateProductList(favorites)
            
            // Show empty state if no favorites found
            if (favorites.isEmpty()) {
                emptyStateFavorites.visibility = View.VISIBLE
            } else {
                emptyStateFavorites.visibility = View.GONE
            }
        }
    }
}

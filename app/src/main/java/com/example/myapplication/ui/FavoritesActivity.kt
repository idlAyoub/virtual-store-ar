package com.example.myapplication.ui

import android.content.Intent
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
import com.example.myapplication.viewmodel.FavoritesViewModel
import kotlinx.coroutines.launch

class FavoritesActivity : ComponentActivity() {

    private lateinit var favoritesViewModel: FavoritesViewModel
    private lateinit var productAdapter: ProductAdapter
    private lateinit var cartRepository: CartRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        val db = AppDatabase.getDatabase(this)
        cartRepository = CartRepository(db.cartDao())

        favoritesViewModel = ViewModelProvider(this)[FavoritesViewModel::class.java]

        setupRecyclerView()
        observeFavorites()
        
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
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
                favoritesViewModel.toggleFavorite(product)
            }
        )
        rvFavorites.adapter = productAdapter
        rvFavorites.layoutManager = GridLayoutManager(this, 2)
    }

    private fun observeFavorites() {
        val rvFavorites = findViewById<RecyclerView>(R.id.rvFavorites)
        val layoutEmptyState = findViewById<View>(R.id.layoutEmptyState)

        favoritesViewModel.favoriteProducts.observe(this) { products ->
            productAdapter.updateProductList(products)
            if (products.isEmpty()) {
                rvFavorites.visibility = View.GONE
                layoutEmptyState.visibility = View.VISIBLE
            } else {
                rvFavorites.visibility = View.VISIBLE
                layoutEmptyState.visibility = View.GONE
            }
        }
    }
}

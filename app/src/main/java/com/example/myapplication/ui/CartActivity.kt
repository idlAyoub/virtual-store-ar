package com.example.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.myapplication.R
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.CartRepository
import com.example.myapplication.ui.cart.CartAdapter
import com.example.myapplication.ui.cart.CartItemUiModel
import com.example.myapplication.ui.cart.CartViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import kotlin.text.format

/**
 * CartActivity
 *
 * Hosts the shopping cart screen.  Observes [com.example.myapplication.ui.cart.CartViewModel] for all data;
 * delegates mutations (quantity changes, removals) back to the ViewModel.
 *
 * Navigation:
 *   ← Back  → previous screen (ProductDetailActivity / MainActivity)
 *   Checkout → OrderSummaryActivity (passes item total)
 */
class CartActivity : ComponentActivity() {

    private lateinit var viewModel: CartViewModel
    private lateinit var adapter: CartAdapter
    private lateinit var cartRepository: CartRepository

    // ── View references ────────────────────────────────────────────────────────
    private lateinit var rvCartItems: RecyclerView
    private lateinit var layoutEmptyCart: View
    private lateinit var tvCartCount: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvShipping: TextView
    private lateinit var tvTax: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnProceedToCheckout: MaterialButton

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        initViewModel()
        bindViews()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    // ── Initialisation ─────────────────────────────────────────────────────────

    private fun initViewModel() {
        val db = AppDatabase.Companion.getDatabase(this)
        cartRepository = CartRepository(db.cartDao())
        val factory = CartViewModel.Factory(cartRepository)
        viewModel = ViewModelProvider(this, factory)[CartViewModel::class.java]
    }

    private fun bindViews() {
        rvCartItems = findViewById(R.id.rvCartItems)
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart)
        tvCartCount = findViewById(R.id.tvCartCount)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvShipping = findViewById(R.id.tvShipping)
        tvTax = findViewById(R.id.tvTax)
        tvTotal = findViewById(R.id.tvTotal)
        btnProceedToCheckout = findViewById(R.id.btnProceedToCheckout)

        // Toolbar back button
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(
            onIncrease = { cartItem -> viewModel.increaseQuantity(cartItem) },
            onDecrease = { cartItem -> viewModel.decreaseQuantity(cartItem) },
            onRemove = { cartItem ->
                viewModel.removeItem(cartItem)
                Toast.makeText(this, "Item removed", Toast.LENGTH_SHORT).show()
            }
        )

        rvCartItems.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = this@CartActivity.adapter
            // Disable default change animations so quantity taps feel instant
            (itemAnimator as? SimpleItemAnimator)
                ?.supportsChangeAnimations = false
        }
    }

    private fun setupClickListeners() {
        // Checkout with validation
        btnProceedToCheckout.setOnClickListener {
            lifecycleScope.launch {
                // Validate stock before proceeding
                val db = AppDatabase.getDatabase(this@CartActivity)
                val outOfStockItems = cartRepository.validateAndPlaceOrder(db.productDao())
                
                if (outOfStockItems.isNotEmpty()) {
                    // Some items ran out of stock
                    Toast.makeText(
                        this@CartActivity,
                        "Out of stock: ${outOfStockItems.joinToString(", ")}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
                
                // All items are in stock - proceed to order summary
                val itemTotal = viewModel.subtotal.value ?: 0.0
                val intent = Intent(this@CartActivity, OrderSummaryActivity::class.java)
                intent.putExtra("ITEM_TOTAL", itemTotal)
                intent.putExtra("ORDER_ALREADY_VALIDATED", true)
                startActivity(intent)
            }
        }
    }

    // ── Observers ──────────────────────────────────────────────────────────────

    private fun observeViewModel() {

        // The adapter needs joined CartItem+Product data.
        // We observe cart items and then resolve products for each.
        viewModel.cartItems.observe(this) { cartItems ->
            if (cartItems.isEmpty()) return@observe

            // Resolve products for each cart item from the database.
            lifecycleScope.launch {
                val db = AppDatabase.Companion.getDatabase(this@CartActivity)
                val uiModels = cartItems.mapNotNull { cartItem ->
                    // getProductById returns LiveData; for a one-shot we use the DAO directly.
                    val product = db.productDao().getProductByIdOnce(cartItem.productId)
                    if (product != null) CartItemUiModel(cartItem, product)
                    else null
                }
                adapter.submitList(uiModels)
            }
        }

        // Empty state
        viewModel.isEmpty.observe(this) { empty ->
            layoutEmptyCart.visibility = if (empty) View.VISIBLE else View.GONE
            rvCartItems.visibility = if (empty) View.GONE else View.VISIBLE
            btnProceedToCheckout.isEnabled = !empty
            btnProceedToCheckout.alpha = if (empty) 0.5f else 1f
        }

        // Cart item count badge
        viewModel.itemCount.observe(this) { count ->
            tvCartCount.text = if (count == 1) "1 item" else "$count items"
        }

        // Subtotal
        viewModel.subtotal.observe(this) { sub ->
            tvSubtotal.text = "$%.2f".format(sub)
        }

        // Shipping
        viewModel.shipping.observe(this) { shipping ->
            tvShipping.text = "$%.2f".format(shipping)
        }

        // Tax
        viewModel.tax.observe(this) { taxAmount ->
            tvTax.text = "$%.2f".format(taxAmount)
        }

        // Grand total
        viewModel.grandTotal.observe(this) { total ->
            tvTotal.text = "$%.2f".format(total)
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
}
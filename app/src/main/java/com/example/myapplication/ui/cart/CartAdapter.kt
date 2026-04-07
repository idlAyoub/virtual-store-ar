package com.example.myapplication.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.myapplication.R
import com.example.myapplication.data.CartItem
import com.example.myapplication.data.Product

/**
 * CartAdapter – Task D3
 *
 * Displays a list of [CartItemUiModel] (a CartItem paired with its Product).
 * Each row exposes increase / decrease / remove callbacks wired to CartViewModel.
 *
 * Uses DiffUtil for efficient, animated updates whenever quantities change.
 */
class CartAdapter(
    private val onIncrease: (CartItem) -> Unit,
    private val onDecrease: (CartItem) -> Unit,
    private val onRemove: (CartItem) -> Unit
) : ListAdapter<CartItemUiModel, CartAdapter.CartViewHolder>(DIFF_CALLBACK) {

    // ── ViewHolder ─────────────────────────────────────────────────────────────

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvUnitPrice: TextView = itemView.findViewById(R.id.tvUnitPrice)
        private val tvItemSubtotal: TextView = itemView.findViewById(R.id.tvItemSubtotal)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val tvStockNote: TextView = itemView.findViewById(R.id.tvStockNote)
        private val btnIncrease: ImageButton = itemView.findViewById(R.id.btnIncreaseQuantity)
        private val btnDecrease: ImageButton = itemView.findViewById(R.id.btnDecreaseQuantity)
        private val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemoveItem)

        fun bind(model: CartItemUiModel) {
            val cartItem = model.cartItem
            val product = model.product

            // Product name
            tvProductName.text = product.name

            // Unit price
            tvUnitPrice.text = "$%.2f each".format(product.price)

            // Subtotal for this line
            val subtotal = product.price * cartItem.quantity
            tvItemSubtotal.text = "$%.2f".format(subtotal)

            // Quantity
            tvQuantity.text = cartItem.quantity.toString()

            // Stock note
            when {
                product.stock <= 0 -> {
                    tvStockNote.text = "Out of stock"
                    tvStockNote.setTextColor(
                        itemView.context.getColor(android.R.color.holo_red_light)
                    )
                }
                product.stock <= 5 -> {
                    tvStockNote.text = "Only ${product.stock} left"
                    tvStockNote.setTextColor(
                        itemView.context.getColor(android.R.color.holo_orange_dark)
                    )
                }
                else -> {
                    tvStockNote.text = "In Stock"
                    tvStockNote.setTextColor(
                        itemView.context.getColor(R.color.color_primary)
                    )
                }
            }

            // Disable increase button when at stock limit
            btnIncrease.isEnabled = cartItem.quantity < product.stock
            btnIncrease.alpha = if (btnIncrease.isEnabled) 1f else 0.35f

            // Product image via Glide
            // Check if it's HTTP URL or drawable resource
            if (product.imageResource.startsWith("http")) {
                // Load from HTTP URL
                Glide.with(itemView.context)
                    .load(product.imageResource)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(ivProductImage)
            } else {
                // Image names are stored as resource names (e.g. "img_product_1").
                // We resolve them to a drawable resource ID at runtime.
                val resId = itemView.context.resources.getIdentifier(
                    product.imageResource, "drawable", itemView.context.packageName
                )
                if (resId != 0) {
                    Glide.with(itemView.context)
                        .load(resId)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .centerCrop()
                        .into(ivProductImage)
                } else {
                    // Fallback placeholder when resource not found
                    Glide.with(itemView.context)
                        .load(R.color.color_secondary_bg)
                        .into(ivProductImage)
                }
            }

            // ── Click Listeners ────────────────────────────────────────────────

            btnIncrease.setOnClickListener {
                animateButton(it)
                onIncrease(cartItem)
            }

            btnDecrease.setOnClickListener {
                animateButton(it)
                onDecrease(cartItem)
            }

            btnRemove.setOnClickListener {
                onRemove(cartItem)
            }
        }

        /** Subtle scale-bounce animation on stepper button tap. */
        private fun animateButton(view: View) {
            view.animate()
                .scaleX(0.80f)
                .scaleY(0.80f)
                .setDuration(80)
                .withEndAction {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(80)
                        .start()
                }
                .start()
        }
    }

    // ── Adapter Overrides ──────────────────────────────────────────────────────

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // ── DiffUtil ───────────────────────────────────────────────────────────────

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CartItemUiModel>() {

            override fun areItemsTheSame(
                oldItem: CartItemUiModel,
                newItem: CartItemUiModel
            ): Boolean = oldItem.cartItem.id == newItem.cartItem.id

            override fun areContentsTheSame(
                oldItem: CartItemUiModel,
                newItem: CartItemUiModel
            ): Boolean = oldItem == newItem
        }
    }
}
package com.example.myapplication.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.myapplication.ui.ProductDetailActivity
import com.example.myapplication.R
import com.example.myapplication.data.Product
import com.example.myapplication.databinding.ItemProductBinding
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color

/**
 * ProductAdapter — 2-column grid of product cards.
 *
 * Clicking the card body → opens ProductDetailActivity
 * Clicking the "+" button → adds directly to cart
 */
class ProductAdapter(
    private var productList: List<Product> = emptyList(),
    private val onAddToCartClick: ((Product) -> Unit)? = null,
    private val onFavoriteClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = String.format("$%.2f", product.price)
            binding.tvCategory.text = extractCategory(product.name)

            // Get the drawable resource ID from the image name
            val context = binding.root.context
<<<<<<< Updated upstream
            val resourceId = context.resources.getIdentifier(
                product.imageResource,
                "drawable",
                context.packageName
            )

            // Load image using Glide with resource ID and optimized cache
            if (resourceId != 0) {
                com.bumptech.glide.Glide.with(context)
                    .load(resourceId)
=======
            
            if (product.imageResource.startsWith("http")) {
                Glide.with(context)
                    .load(product.imageResource)
>>>>>>> Stashed changes
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .into(binding.ivProductImage)
            } else {
<<<<<<< Updated upstream
                // If resource not found, show placeholder
                com.bumptech.glide.Glide.with(context)
                    .load(R.drawable.ic_launcher_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .into(binding.ivProductImage)
=======
                val resourceId = context.resources.getIdentifier(
                    product.imageResource,
                    "drawable",
                    context.packageName
                )

                // Load image using Glide with resource ID
                if (resourceId != 0) {
                    Glide.with(context)
                        .load(resourceId)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .centerCrop()
                        .into(binding.ivProductImage)
                } else {
                    // If resource not found, show placeholder
                    Glide.with(context)
                        .load(R.drawable.ic_launcher_background)
                        .centerCrop()
                        .into(binding.ivProductImage)
                }
>>>>>>> Stashed changes
            }

            // Show/hide AR badge based on AR model availability
            if (product.arModelResource.isNotEmpty()) {
                binding.arBadgeContainer.visibility = android.view.View.VISIBLE
                binding.arBadgeContainer.setOnClickListener {
                    val context = binding.root.context
                    val intent = Intent(context, com.example.myapplication.ui.ARViewActivity::class.java)
                    intent.putExtra("AR_MODEL", product.arModelResource)
                    intent.putExtra("PRODUCT_NAME", product.name)
                    intent.putExtra("PRODUCT_ID", product.id)
                    context.startActivity(intent)
                }
            } else {
                binding.arBadgeContainer.visibility = android.view.View.GONE
            }

<<<<<<< Updated upstream
            // Favorite icon (Interactive & Animated)
            updateFavoriteUI(product)
            
            binding.ivFavorite.setOnClickListener {
                // Perform pulse animation
                animateHeart(binding.ivFavorite)
                
                // Invoke callback
=======
            // Favorite button state and click listener
            if (product.isFavorite) {
                binding.btnFavorite.setImageResource(R.drawable.ic_heart_filled)
                binding.btnFavorite.setColorFilter(androidx.core.content.ContextCompat.getColor(context, R.color.color_primary))
            } else {
                binding.btnFavorite.setImageResource(R.drawable.ic_heart_outline)
                binding.btnFavorite.setColorFilter(androidx.core.content.ContextCompat.getColor(context, R.color.color_text_tertiary))
            }

            binding.btnFavorite.setOnClickListener {
>>>>>>> Stashed changes
                onFavoriteClick?.invoke(product)
            }

            // Card click → ProductDetailActivity
            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, ProductDetailActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                context.startActivity(intent)
            }

            // "+" button → add to cart directly (without opening detail)
            binding.btnAddToCart.setOnClickListener {
                if (product.stock <= 0) {
                    Toast.makeText(
                        binding.root.context,
                        "${product.name} is out of stock.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                onAddToCartClick?.invoke(product)
                Toast.makeText(
                    binding.root.context,
                    "${product.name} added to cart!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        private fun updateFavoriteUI(product: Product) {
            val context = binding.root.context
            if (product.isFavorite) {
                binding.ivFavorite.setImageResource(R.drawable.ic_heart) // Assuming ic_heart is filled
                binding.ivFavorite.setColorFilter(ContextCompat.getColor(context, R.color.color_success))
            } else {
                binding.ivFavorite.setImageResource(R.drawable.ic_heart_outline)
                binding.ivFavorite.setColorFilter(Color.parseColor("#BDBDBD"))
            }
        }

        private fun animateHeart(view: android.view.View) {
            val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.25f, 1.0f)
            val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.25f, 1.0f)
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(scaleX, scaleY)
            animatorSet.duration = 300
            animatorSet.start()
        }

        private fun extractCategory(productName: String): String {
            return when {
                productName.contains("Chair", ignoreCase = true) -> "Chaises"
                productName.contains("Sofa", ignoreCase = true) ||
                        productName.contains("Couch", ignoreCase = true) -> "Canapés"
                productName.contains("Lamp", ignoreCase = true) ||
                        productName.contains("Light", ignoreCase = true) -> "Éclairage"
                productName.contains("Table", ignoreCase = true) -> "Tables"
                else -> "Mobilier"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        if (position < productList.size) holder.bind(productList[position])
    }

    override fun getItemCount(): Int = productList.size

    fun updateProductList(newProductList: List<Product>) {
        val diffCallback = ProductDiffCallback(productList, newProductList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        productList = newProductList
        diffResult.dispatchUpdatesTo(this)
    }

    class ProductDiffCallback(
        private val oldList: List<Product>,
        private val newList: List<Product>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
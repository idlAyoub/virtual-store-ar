package com.example.myapplication.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.ui.ProductDetailActivity
import com.example.myapplication.R
import com.example.myapplication.data.Product
import com.example.myapplication.databinding.ItemProductBinding

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
            
            if (product.imageResource.startsWith("http")) {
                Glide.with(context)
                    .load(product.imageResource)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(binding.ivProductImage)
            } else {
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

            // Favorite button state and click listener
            if (product.isFavorite) {
                binding.btnFavorite.setImageResource(R.drawable.ic_heart_filled)
                binding.btnFavorite.setColorFilter(androidx.core.content.ContextCompat.getColor(context, R.color.color_primary))
            } else {
                binding.btnFavorite.setImageResource(R.drawable.ic_heart_outline)
                binding.btnFavorite.setColorFilter(androidx.core.content.ContextCompat.getColor(context, R.color.color_text_tertiary))
            }

            binding.btnFavorite.setOnClickListener {
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
        productList = newProductList
        notifyDataSetChanged()
    }
}
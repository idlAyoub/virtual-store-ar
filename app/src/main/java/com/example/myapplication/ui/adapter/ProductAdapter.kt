package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.data.Product
import com.example.myapplication.databinding.ItemProductBinding

/**
 * ProductAdapter - RecyclerView adapter for displaying products in a modern 2-column grid
 *
 * Features:
 * - Displays product cards with image, AR badge, category, name, price
 * - Circular add-to-cart button with click handling
 * - Click on card opens ProductDetailActivity with productId
 * - Glide integration for efficient image loading
 * - ViewBinding for optimal performance
 */
class ProductAdapter(
    private var productList: List<Product> = emptyList(),
    private val onItemClickListener: ((Product) -> Unit)? = null,
    private val onAddToCartClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    /**
     * ViewHolder for binding product data to the modern product card layout
     */
    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Bind product data to the UI views
         * Includes image loading, category, price, and button handlers
         */
        fun bind(product: Product) {
            // Set product name with ellipsis for long names
            binding.tvProductName.text = product.name

            // Format and set product price with currency
            binding.tvProductPrice.text = String.format("$%.2f", product.price)

            // Set category label (extract category from product or use default)
            binding.tvCategory.text = extractCategory(product.name)

            // Load product image using Glide with placeholders
            Glide.with(binding.root.context)
                .load(product.imageResource)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(binding.ivProductImage)


            // Handle circular add-to-cart button click
            binding.btnAddToCart.setOnClickListener {
                onAddToCartClick?.invoke(product)
                Toast.makeText(
                    binding.root.context,
                    "${product.name} added to cart!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        /**
         * Extract category from product name or return default category
         * This is a simple implementation - can be enhanced with actual category field
         */
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

    /**
     * Create a new ViewHolder instance
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    /**
     * Bind product data to the ViewHolder
     */
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        if (position < productList.size) {
            holder.bind(productList[position])
        }
    }

    /**
     * Get total number of products in the list
     */
    override fun getItemCount(): Int = productList.size

    /**
     * Update the product list and refresh RecyclerView
     * Called when search filters or product list changes
     */
    fun updateProductList(newProductList: List<Product>) {
        this.productList = newProductList
        notifyDataSetChanged()
    }
}


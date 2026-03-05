package com.example.myapplication.data

object DataSeeder {

    fun getProductList(): List<Product> {
        val products = mutableListOf<Product>()

        val names = listOf(
            "Sofa", "Chair", "Table", "Lamp", "Desk",
            "Shelf", "Bed", "Mirror", "Wardrobe", "Cabinet"
        )

        for (i in 1..100) {
            val name = names[(i - 1) % names.size]
            products.add(
                Product(
                    id = i,
                    name = "$name $i",
                    description = "High quality $name, perfect for modern interiors. Durable and stylish.",
                    price = (50 + i * 2).toDouble(),
                    imageResource = "img_product_${(i % 5) + 1}",
                    arModelResource = if (i % 3 == 0) "model_${name.lowercase()}" else ""
                )
            )
        }
        return products
    }
}
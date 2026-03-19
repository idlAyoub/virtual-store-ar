package com.example.myapplication.data

object DataSeeder {

    fun getProductList(): List<Product> {
        val products = mutableListOf<Product>()

        // Add actual products with real images
        products.add(
            Product(
                name = "iPhone 12 Pro Max",
                description = "The iPhone 12 Pro Max features a stunning 6.7-inch Super Retina XDR display, A14 Bionic chip for lightning-fast performance, and a pro camera system with LiDAR scanner for incredible low-light photography. With 5G capabilities and Ceramic Shield front cover, it's designed to keep you connected and protected in style.",
                price = 1099.00,
                stock = 8,
                imageResource = "iphone_12_pro",
                arModelResource = "models/iphone_12_pro.glb"
            )
        )

        products.add(
            Product(
                name = "GoPro HERO 2018",
                description = "Capture stunning 4K video and 12MP photos with the GoPro HERO. Waterproof up to 33 feet, featuring advanced stabilization and voice control. Perfect for adventure enthusiasts and content creators.",
                price = 349.99,
                stock = 12,
                imageResource = "gopro_hero_2018",
                arModelResource = "models/gopro_hero_2018.glb"
            )
        )

        products.add(
            Product(
                name = "PlayStation 5 Digital",
                description = "Experience next-gen gaming with PlayStation 5. All-digital console featuring ultra-high speed SSD, custom CPU and GPU, and 4K gaming capabilities. Access thousands of PS5 games through PlayStation Plus.",
                price = 499.99,
                stock = 6,
                imageResource = "playstation_5_digital",
                arModelResource = "models/playstation_5_digital.glb"
            )
        )

        val names = listOf(
            "Sofa", "Chair", "Table", "Lamp", "Desk",
            "Shelf", "Bed", "Mirror", "Wardrobe", "Cabinet"
        )

        for (i in 1..20) {
            val name = names[(i - 1) % names.size]
            products.add(
                Product(
                    name = "$name $i",
                    description = "High quality $name, perfect for modern interiors. Durable and stylish.",
                    price = (50 + i * 2).toDouble(),
                    stock = (5 + i % 20),
                    imageResource = "img_product_${(i % 5) + 1}",
                    arModelResource = if (i % 3 == 0) "model_${name.lowercase()}" else ""
                )
            )
        }

        return products
    }
}
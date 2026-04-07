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

        products.add(Product(name = "MacBook Pro 14", description = "Powerful laptop for professionals", price = 1999.0, stock = 5, imageResource = "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/mbp14-spacegray-select-202110", arModelResource = ""))
        products.add(Product(name = "Sony WH-1000XM4", description = "Noise canceling headphones", price = 298.0, stock = 20, imageResource = "https://www.bhphotovideo.com/images/images2500x2500/sony_wh1000xm4_b_wh_1000xm4_noise_canceling_overhead_headphones_1582925.jpg", arModelResource = ""))
        products.add(Product(name = "Sofa Nordic Gray", description = "Comfortable gray sofa", price = 450.0, stock = 8, imageResource = "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=400", arModelResource = ""))
        products.add(Product(name = "Sofa Velvet Blue", description = "Luxurious blue velvet sofa", price = 550.0, stock = 4, imageResource = "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?w=400", arModelResource = ""))
        products.add(Product(name = "Armchair Leather", description = "Classic leather armchair", price = 300.0, stock = 6, imageResource = "https://images.unsplash.com/photo-1506439773649-6e0eb8cfb237?w=400", arModelResource = ""))
        products.add(Product(name = "Office Chair Pro", description = "Ergonomic office chair", price = 199.0, stock = 12, imageResource = "https://images.unsplash.com/photo-1580480055273-228ff5388ef8?w=400", arModelResource = ""))
        products.add(Product(name = "Dining Chair Oak", description = "Solid oak dining chair", price = 89.0, stock = 24, imageResource = "https://images.unsplash.com/photo-1551298370-9d3d53740c72?w=400", arModelResource = ""))
        products.add(Product(name = "Coffee Table Marble", description = "Elegant marble coffee table", price = 250.0, stock = 7, imageResource = "https://images.unsplash.com/photo-1567538096630-e0c55bd6374c?w=400", arModelResource = ""))
        products.add(Product(name = "Dining Table Walnut", description = "Large walnut dining table", price = 600.0, stock = 3, imageResource = "https://images.unsplash.com/photo-1549187774-b4e9b0445b41?w=400", arModelResource = ""))
        products.add(Product(name = "TV Stand Modern", description = "Modern minimalist TV stand", price = 180.0, stock = 10, imageResource = "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=400", arModelResource = ""))
        products.add(Product(name = "Bookshelf White", description = "Tall white bookshelf", price = 120.0, stock = 14, imageResource = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400", arModelResource = ""))
        products.add(Product(name = "Bed Frame Queen", description = "Queen size wooden bed frame", price = 400.0, stock = 5, imageResource = "https://images.unsplash.com/photo-1505693314120-0d443867891c?w=400", arModelResource = ""))
        products.add(Product(name = "Wardrobe 3 Door", description = "Spacious 3-door wardrobe", price = 500.0, stock = 2, imageResource = "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400", arModelResource = ""))
        products.add(Product(name = "Desk L-Shape", description = "L-Shape corner desk", price = 220.0, stock = 9, imageResource = "https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=400", arModelResource = ""))
        products.add(Product(name = "Nightstand Oak", description = "Small oak nightstand", price = 60.0, stock = 18, imageResource = "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=400", arModelResource = ""))
        products.add(Product(name = "Cabinet Storage", description = "Storage cabinet", price = 140.0, stock = 6, imageResource = "https://images.unsplash.com/photo-1484101403633-562f891dc89a?w=400", arModelResource = ""))
        products.add(Product(name = "Side Table Round", description = "Round metal side table", price = 70.0, stock = 11, imageResource = "https://images.unsplash.com/photo-1538688525198-9b88f6f53126?w=400", arModelResource = ""))
        products.add(Product(name = "iPad Pro 12.9", description = "Apple iPad Pro 12.9 inch", price = 1099.0, stock = 8, imageResource = "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400", arModelResource = ""))
        products.add(Product(name = "DJI Mavic Mini 2", description = "Compact drone with 4K camera", price = 449.0, stock = 7, imageResource = "https://images.unsplash.com/photo-1473968512647-3e447244af8f?w=400", arModelResource = ""))

        return products
    }
}
package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


<<<<<<< HEAD
<<<<<<< Updated upstream
@Database(entities = [Product::class, CartItem::class], version = 1)
=======
@Database(entities = [Product::class, CartItem::class], version = 5, exportSchema = false)
>>>>>>> Stashed changes
=======
@Database(entities = [Product::class, CartItem::class], version = 2)
>>>>>>> origin/main
abstract class AppDatabase : RoomDatabase() {

    abstract fun cartDao(): CartDao

    abstract fun productDao(): ProductDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration()
                .build()

                INSTANCE = instance
                instance
            }
        }

    }

}
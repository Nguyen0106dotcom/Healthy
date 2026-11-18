package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DishEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        ReviewEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GreenBiteDatabase : RoomDatabase() {
    abstract fun dishDao(): DishDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var INSTANCE: GreenBiteDatabase? = null

        fun getInstance(context: Context): GreenBiteDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GreenBiteDatabase::class.java,
                    "greenbite_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

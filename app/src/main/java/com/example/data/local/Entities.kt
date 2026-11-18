package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

object DishCategories {
    const val ALL = "Tất cả"
    const val SALAD = "Salad"
    const val HEALTHY_BOWL = "Healthy Bowl"
    const val EAT_CLEAN_MAIN = "Món chính Eat Clean"
    const val SMOOTHIE_JUICE = "Smoothie & Nước ép"
    const val LOW_SUGAR_SNACK = "Snack ít đường"
    const val WEEKLY_COMBO = "Combo theo tuần"

    val list = listOf(
        SALAD,
        HEALTHY_BOWL,
        EAT_CLEAN_MAIN,
        SMOOTHIE_JUICE,
        LOW_SUGAR_SNACK,
        WEEKLY_COMBO
    )
}

object DietTags {
    const val VEGAN = "Vegan"
    const val LOW_CARB = "Low-carb"
    const val KETO = "Keto"
    const val GLUTEN_FREE = "Không gluten"
    const val LOW_SUGAR = "Ít đường"

    val all = listOf(VEGAN, LOW_CARB, KETO, GLUTEN_FREE, LOW_SUGAR)
}

object OrderStatuses {
    const val NEW = "Mới"
    const val PREPARING = "Đang chuẩn bị"
    const val DELIVERING = "Đang giao"
    const val COMPLETED = "Hoàn thành"

    val all = listOf(NEW, PREPARING, DELIVERING, COMPLETED)
}

@Entity(tableName = "dishes")
data class DishEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val price: Int,
    val originalPrice: Int? = null,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val tagsCsv: String,
    val description: String,
    val ingredientsCsv: String,
    val imageUrl: String,
    val fallbackKey: String,
    val isBestSeller: Boolean = false,
    val isComboDeal: Boolean = false
) {
    val tags: List<String>
        get() = tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    val ingredients: List<String>
        get() = ingredientsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dishId: Int,
    val dishName: String,
    val size: String,
    val unitPrice: Int,
    val calories: Int,
    val quantity: Int,
    val note: String,
    val imageUrl: String,
    val fallbackKey: String
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderCode: String,
    val customerName: String,
    val phone: String,
    val address: String,
    val deliveryTime: String,
    val note: String,
    val paymentMethod: String,
    val promoCode: String,
    val discountAmount: Int,
    val subtotal: Int,
    val totalAmount: Int,
    val totalCalories: Int,
    val status: String,
    val itemsSummary: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dishId: Int,
    val authorName: String,
    val rating: Int,
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
)

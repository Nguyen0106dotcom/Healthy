package com.example.data.repository

import com.example.data.local.CartDao
import com.example.data.local.CartItemEntity
import com.example.data.local.DishDao
import com.example.data.local.DishEntity
import com.example.data.local.OrderDao
import com.example.data.local.OrderEntity
import com.example.data.local.ReviewDao
import com.example.data.local.ReviewEntity
import com.example.data.local.SeedData
import kotlinx.coroutines.flow.Flow

class GreenBiteRepository(
    private val dishDao: DishDao,
    private val cartDao: CartDao,
    private val orderDao: OrderDao,
    private val reviewDao: ReviewDao
) {
    val dishes: Flow<List<DishEntity>> = dishDao.getAllDishes()
    val cartItems: Flow<List<CartItemEntity>> = cartDao.getCartItems()
    val orders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val reviews: Flow<List<ReviewEntity>> = reviewDao.getAllReviews()

    suspend fun ensureSeeded() {
        if (dishDao.countDishes() == 0) {
            dishDao.insertAll(SeedData.dishes)
        }
        if (reviewDao.countReviews() == 0) {
            reviewDao.insertAll(SeedData.reviews)
        }
        if (orderDao.countOrders() == 0) {
            orderDao.insertAll(SeedData.sampleOrders)
        }
    }

    suspend fun addToCart(
        dish: DishEntity,
        size: String,
        unitPrice: Int,
        unitCalories: Int,
        quantity: Int,
        note: String
    ) {
        val cleanNote = note.trim()
        val existing = cartDao.findMatchingItem(dish.id, size, cleanNote)
        if (existing != null) {
            cartDao.updateCartItem(existing.copy(quantity = existing.quantity + quantity))
        } else {
            cartDao.insertCartItem(
                CartItemEntity(
                    dishId = dish.id,
                    dishName = dish.name,
                    size = size,
                    unitPrice = unitPrice,
                    calories = unitCalories,
                    quantity = quantity,
                    note = cleanNote,
                    imageUrl = dish.imageUrl,
                    fallbackKey = dish.fallbackKey
                )
            )
        }
    }

    suspend fun addCustomPackageToCart(
        title: String,
        packagePrice: Int,
        dailyCalories: Int,
        note: String
    ) {
        cartDao.insertCartItem(
            CartItemEntity(
                dishId = -100 - (title.hashCode() and 0xFFF),
                dishName = title,
                size = "Gói",
                unitPrice = packagePrice,
                calories = dailyCalories,
                quantity = 1,
                note = note.trim(),
                imageUrl = "",
                fallbackKey = "hero"
            )
        )
    }

    suspend fun updateCartQuantity(item: CartItemEntity, newQuantity: Int) {
        if (newQuantity <= 0) {
            cartDao.deleteCartItem(item.id)
        } else {
            cartDao.updateCartItem(item.copy(quantity = newQuantity))
        }
    }

    suspend fun removeCartItem(id: Int) {
        cartDao.deleteCartItem(id)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }

    suspend fun placeOrder(order: OrderEntity) {
        orderDao.insertOrder(order)
        cartDao.clearCart()
    }

    suspend fun updateOrderStatus(orderCode: String, status: String) {
        orderDao.updateOrderStatus(orderCode, status)
    }

    suspend fun saveDish(dish: DishEntity) {
        if (dish.id == 0) {
            dishDao.insertDish(dish)
        } else {
            dishDao.updateDish(dish)
        }
    }

    suspend fun deleteDish(dishId: Int) {
        dishDao.deleteDishById(dishId)
    }

    suspend fun addReview(dishId: Int, authorName: String, rating: Int, comment: String) {
        reviewDao.insertReview(
            ReviewEntity(
                dishId = dishId,
                authorName = authorName.trim().ifEmpty { "Khách hàng GreenBite" },
                rating = rating.coerceIn(1, 5),
                comment = comment.trim()
            )
        )
    }
}

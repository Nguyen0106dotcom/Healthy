package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {
    @Query("SELECT * FROM dishes ORDER BY id ASC")
    fun getAllDishes(): Flow<List<DishEntity>>

    @Query("SELECT COUNT(*) FROM dishes")
    suspend fun countDishes(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dishes: List<DishEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDish(dish: DishEntity): Long

    @Update
    suspend fun updateDish(dish: DishEntity)

    @Query("DELETE FROM dishes WHERE id = :dishId")
    suspend fun deleteDishById(dishId: Int)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY id DESC")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE dishId = :dishId AND size = :size AND note = :note LIMIT 1")
    suspend fun findMatchingItem(dishId: Int, size: String, note: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun countOrders(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<OrderEntity>)

    @Query("UPDATE orders SET status = :status WHERE orderCode = :orderCode")
    suspend fun updateOrderStatus(orderCode: String, status: String)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>

    @Query("SELECT COUNT(*) FROM reviews")
    suspend fun countReviews(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reviews: List<ReviewEntity>)
}

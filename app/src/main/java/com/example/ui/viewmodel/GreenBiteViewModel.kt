package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CartItemEntity
import com.example.data.local.DishCategories
import com.example.data.local.DishEntity
import com.example.data.local.GreenBiteDatabase
import com.example.data.local.OrderEntity
import com.example.data.local.OrderStatuses
import com.example.data.local.ReviewEntity
import com.example.data.repository.GreenBiteRepository
import com.example.data.repository.NutritionAiRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

private const val TAG = "GreenBiteVM"

enum class PriceFilter(val label: String) {
    ALL("Tất cả giá"),
    UNDER_60("Dưới 60k"),
    BETWEEN_60_90("60k – 90k"),
    OVER_90("Trên 90k")
}

enum class CalorieFilter(val label: String) {
    ALL("Mọi mức calo"),
    UNDER_300("< 300 kcal"),
    BETWEEN_300_450("300 – 450 kcal"),
    OVER_450("> 450 kcal")
}

data class MenuFilterState(
    val searchQuery: String = "",
    val selectedCategory: String = DishCategories.ALL,
    val selectedTags: Set<String> = emptySet(),
    val priceFilter: PriceFilter = PriceFilter.ALL,
    val calorieFilter: CalorieFilter = CalorieFilter.ALL
)

data class WeeklyPlanState(
    val goal: String = "Giảm cân",
    val mealsPerDay: Int = 2,
    val days: Int = 5
) {
    val baseMealPrice: Int
        get() = when (goal) {
            "Tăng cơ" -> 68000
            "Giữ dáng" -> 62000
            else -> 58000
        }

    val discountPercent: Int
        get() = when {
            days >= 14 -> 15
            days >= 7 -> 10
            else -> 5
        }

    val totalMeals: Int get() = mealsPerDay * days

    val originalTotal: Int get() = baseMealPrice * totalMeals

    val finalPrice: Int
        get() = (originalTotal * (100 - discountPercent) / 100.0).roundToInt()

    val estimatedDailyKcal: Int
        get() = when (goal) {
            "Giảm cân" -> if (mealsPerDay == 2) 850 else 1250
            "Tăng cơ" -> if (mealsPerDay == 2) 1200 else 1750
            else -> if (mealsPerDay == 2) 1000 else 1480
        }
}

data class AiChatMessage(
    val id: Long = System.nanoTime(),
    val isUser: Boolean,
    val text: String,
    val dailyCalories: Int? = null,
    val macroSummary: String? = null,
    val recommendedDishes: List<Pair<DishEntity, String>> = emptyList()
)

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data object Success : AiUiState
    data class Error(val message: String) : AiUiState
}

data class CartSummary(
    val items: List<CartItemEntity> = emptyList(),
    val totalCount: Int = 0,
    val subtotal: Int = 0,
    val promoCode: String = "",
    val promoMessage: String? = null,
    val isPromoError: Boolean = false,
    val discountAmount: Int = 0,
    val finalTotal: Int = 0,
    val totalCalories: Int = 0
)

class GreenBiteViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GreenBiteDatabase.getInstance(application)
    private val repository = GreenBiteRepository(
        dishDao = database.dishDao(),
        cartDao = database.cartDao(),
        orderDao = database.orderDao(),
        reviewDao = database.reviewDao()
    )
    private val aiRepository = NutritionAiRepository()

    private val _isMenuLoading = MutableStateFlow(true)
    val isMenuLoading: StateFlow<Boolean> = _isMenuLoading.asStateFlow()

    private val _filterState = MutableStateFlow(MenuFilterState())
    val filterState: StateFlow<MenuFilterState> = _filterState.asStateFlow()

    val allDishes: StateFlow<List<DishEntity>> = repository.dishes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredDishes: StateFlow<List<DishEntity>> = combine(allDishes, _filterState) { dishes, filter ->
        dishes.filter { dish ->
            val matchesQuery = filter.searchQuery.isBlank() ||
                dish.name.contains(filter.searchQuery, ignoreCase = true) ||
                dish.description.contains(filter.searchQuery, ignoreCase = true) ||
                dish.tagsCsv.contains(filter.searchQuery, ignoreCase = true)

            val matchesCategory = filter.selectedCategory == DishCategories.ALL ||
                dish.category == filter.selectedCategory

            val matchesTags = filter.selectedTags.isEmpty() ||
                filter.selectedTags.all { tag -> dish.tags.contains(tag) }

            val matchesPrice = when (filter.priceFilter) {
                PriceFilter.ALL -> true
                PriceFilter.UNDER_60 -> dish.price < 60_000
                PriceFilter.BETWEEN_60_90 -> dish.price in 60_000..90_000
                PriceFilter.OVER_90 -> dish.price > 90_000
            }

            val matchesCalories = when (filter.calorieFilter) {
                CalorieFilter.ALL -> true
                CalorieFilter.UNDER_300 -> dish.calories < 300
                CalorieFilter.BETWEEN_300_450 -> dish.calories in 300..450
                CalorieFilter.OVER_450 -> dish.calories > 450
            }

            matchesQuery && matchesCategory && matchesTags && matchesPrice && matchesCalories
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _appliedPromo = MutableStateFlow("")
    private val _promoMessage = MutableStateFlow<Pair<String, Boolean>?>(null)

    val cartSummary: StateFlow<CartSummary> = combine(
        repository.cartItems,
        _appliedPromo,
        _promoMessage
    ) { items, promo, promoFeedback ->
        val subtotal = items.sumOf { it.unitPrice * it.quantity }
        val totalCount = items.sumOf { it.quantity }
        val totalCalories = items.sumOf { it.calories * it.quantity }
        val discount = when (promo) {
            "HEALTHY10" -> (subtotal * 0.10).roundToInt()
            "GREEN20" -> if (subtotal >= 100_000) 20_000 else 0
            else -> 0
        }
        val finalTotal = (subtotal - discount).coerceAtLeast(0)
        CartSummary(
            items = items,
            totalCount = totalCount,
            subtotal = subtotal,
            promoCode = promo,
            promoMessage = promoFeedback?.first,
            isPromoError = promoFeedback?.second ?: false,
            discountAmount = discount,
            finalTotal = finalTotal,
            totalCalories = totalCalories
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CartSummary())

    val orders: StateFlow<List<OrderEntity>> = repository.orders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reviews: StateFlow<List<ReviewEntity>> = repository.reviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _weeklyPlan = MutableStateFlow(WeeklyPlanState())
    val weeklyPlan: StateFlow<WeeklyPlanState> = _weeklyPlan.asStateFlow()

    private val _cartBounceSignal = MutableStateFlow(0)
    val cartBounceSignal: StateFlow<Int> = _cartBounceSignal.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _latestConfirmedOrder = MutableStateFlow<OrderEntity?>(null)
    val latestConfirmedOrder: StateFlow<OrderEntity?> = _latestConfirmedOrder.asStateFlow()

    private val _aiUiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val aiUiState: StateFlow<AiUiState> = _aiUiState.asStateFlow()

    private val _aiMessages = MutableStateFlow(
        listOf(
            AiChatMessage(
                isUser = false,
                text = "Xin chào! Mình là Trợ lý Dinh dưỡng GreenBite. Nhập chỉ số cơ thể hoặc mục tiêu của bạn, mình sẽ tính lượng calo phù hợp và gợi ý món ngon ngay trong thực đơn nhé!"
            )
        )
    )
    val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            delay(450L)
            _isMenuLoading.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(category: String) {
        _filterState.update { it.copy(selectedCategory = category) }
    }

    fun toggleDietTag(tag: String) {
        _filterState.update { state ->
            val updated = if (state.selectedTags.contains(tag)) {
                state.selectedTags - tag
            } else {
                state.selectedTags + tag
            }
            state.copy(selectedTags = updated)
        }
    }

    fun selectPriceFilter(priceFilter: PriceFilter) {
        _filterState.update { it.copy(priceFilter = priceFilter) }
    }

    fun selectCalorieFilter(calorieFilter: CalorieFilter) {
        _filterState.update { it.copy(calorieFilter = calorieFilter) }
    }

    fun resetFilters() {
        _filterState.value = MenuFilterState()
    }

    fun addDishToCart(
        dish: DishEntity,
        size: String = "M",
        quantity: Int = 1,
        note: String = ""
    ) {
        val multiplier = when (size) {
            "S" -> 0.85
            "L" -> 1.25
            else -> 1.0
        }
        val adjustedPrice = ((dish.price * multiplier) / 1000.0).roundToInt() * 1000
        val adjustedCalories = (dish.calories * multiplier).roundToInt()
        viewModelScope.launch {
            repository.addToCart(
                dish = dish,
                size = size,
                unitPrice = adjustedPrice,
                unitCalories = adjustedCalories,
                quantity = quantity,
                note = note
            )
            _cartBounceSignal.update { it + 1 }
            _snackbarMessage.value = "Đã thêm \"${dish.name}\" vào giỏ hàng"
        }
    }

    fun updateCartQuantity(item: CartItemEntity, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(item, newQuantity)
        }
    }

    fun removeCartItem(id: Int) {
        viewModelScope.launch {
            repository.removeCartItem(id)
        }
    }

    fun applyPromoCode(rawCode: String) {
        val code = rawCode.trim().uppercase()
        when (code) {
            "" -> {
                _appliedPromo.value = ""
                _promoMessage.value = null
            }
            "HEALTHY10" -> {
                _appliedPromo.value = code
                _promoMessage.value = "Áp dụng mã HEALTHY10: Giảm 10% đơn hàng" to false
            }
            "GREEN20" -> {
                if (cartSummary.value.subtotal >= 100_000) {
                    _appliedPromo.value = code
                    _promoMessage.value = "Áp dụng mã GREEN20: Giảm 20.000đ" to false
                } else {
                    _appliedPromo.value = ""
                    _promoMessage.value = "Mã GREEN20 áp dụng cho đơn từ 100.000đ" to true
                }
            }
            else -> {
                _appliedPromo.value = ""
                _promoMessage.value = "Mã giảm giá không hợp lệ (Thử: HEALTHY10)" to true
            }
        }
    }

    fun clearPromoCode() {
        _appliedPromo.value = ""
        _promoMessage.value = null
    }

    fun updateWeeklyGoal(goal: String) {
        _weeklyPlan.update { it.copy(goal = goal) }
    }

    fun updateWeeklyMealsPerDay(meals: Int) {
        _weeklyPlan.update { it.copy(mealsPerDay = meals) }
    }

    fun updateWeeklyDays(days: Int) {
        _weeklyPlan.update { it.copy(days = days) }
    }

    fun addWeeklyPlanToCart() {
        val plan = _weeklyPlan.value
        val title = "Gói Ăn ${plan.goal} (${plan.days} ngày • ${plan.mealsPerDay} bữa/ngày)"
        val note = "${plan.totalMeals} bữa chuẩn Eat Clean • ~${plan.estimatedDailyKcal} kcal/ngày"
        viewModelScope.launch {
            repository.addCustomPackageToCart(
                title = title,
                packagePrice = plan.finalPrice,
                dailyCalories = plan.estimatedDailyKcal,
                note = note
            )
            _cartBounceSignal.update { it + 1 }
            _snackbarMessage.value = "Đã thêm gói ăn theo tuần vào giỏ hàng"
        }
    }

    fun submitOrder(
        customerName: String,
        phone: String,
        address: String,
        deliveryTime: String,
        note: String,
        paymentMethod: String
    ) {
        val summary = cartSummary.value
        if (summary.items.isEmpty()) return

        val orderCode = "GB-${Random.nextInt(100000, 999999)}"
        val itemsText = summary.items.joinToString(", ") { "${it.quantity}x ${it.dishName} (${it.size})" }
        val order = OrderEntity(
            orderCode = orderCode,
            customerName = customerName.trim(),
            phone = phone.trim(),
            address = address.trim(),
            deliveryTime = deliveryTime,
            note = note.trim(),
            paymentMethod = paymentMethod,
            promoCode = summary.promoCode,
            discountAmount = summary.discountAmount,
            subtotal = summary.subtotal,
            totalAmount = summary.finalTotal,
            totalCalories = summary.totalCalories,
            status = OrderStatuses.NEW,
            itemsSummary = itemsText
        )

        viewModelScope.launch {
            repository.placeOrder(order)
            clearPromoCode()
            _latestConfirmedOrder.value = order
        }
    }

    fun dismissConfirmedOrder() {
        _latestConfirmedOrder.value = null
    }

    fun updateOrderStatus(orderCode: String, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderCode, status)
            _snackbarMessage.value = "Đã cập nhật đơn $orderCode -> $status"
        }
    }

    fun saveDish(dish: DishEntity) {
        viewModelScope.launch {
            repository.saveDish(dish)
            _snackbarMessage.value = if (dish.id == 0) "Đã thêm món mới: ${dish.name}" else "Đã cập nhật món: ${dish.name}"
        }
    }

    fun deleteDish(dish: DishEntity) {
        viewModelScope.launch {
            repository.deleteDish(dish.id)
            _snackbarMessage.value = "Đã xóa món: ${dish.name}"
        }
    }

    fun addReview(dishId: Int, authorName: String, rating: Int, comment: String) {
        if (comment.isBlank()) return
        viewModelScope.launch {
            repository.addReview(dishId, authorName, rating, comment)
            _snackbarMessage.value = "Cảm ơn đánh giá của bạn!"
        }
    }

    fun askAiNutritionist(
        heightCm: Int?,
        weightKg: Int?,
        age: Int?,
        goal: String,
        allergies: String,
        userMessage: String
    ) {
        val dishes = allDishes.value
        if (dishes.isEmpty()) return

        val displayPrompt = buildString {
            if (heightCm != null || weightKg != null || age != null) {
                append("Cao ${heightCm ?: "--"}cm, Nặng ${weightKg ?: "--"}kg, ${age ?: "--"} tuổi • Mục tiêu: $goal")
                if (allergies.isNotBlank()) append(" • Dị ứng: $allergies")
                if (userMessage.isNotBlank()) append("\n$userMessage")
            } else {
                append(userMessage.ifBlank { "Gợi ý thực đơn $goal cho hôm nay" })
            }
        }

        _aiMessages.update { it + AiChatMessage(isUser = true, text = displayPrompt) }
        _aiUiState.value = AiUiState.Loading

        viewModelScope.launch {
            try {
                val result = aiRepository.askNutritionAdvice(
                    heightCm = heightCm,
                    weightKg = weightKg,
                    age = age,
                    goal = goal,
                    allergies = allergies,
                    userMessage = userMessage,
                    availableDishes = dishes
                )
                val recommendedPairs = result.recommendations.mapNotNull { rec ->
                    dishes.find { it.id == rec.dishId }?.let { dish -> dish to rec.reason }
                }
                _aiMessages.update {
                    it + AiChatMessage(
                        isUser = false,
                        text = result.reply,
                        dailyCalories = result.dailyCalories,
                        macroSummary = result.macroSummary,
                        recommendedDishes = recommendedPairs
                    )
                }
                _aiUiState.value = AiUiState.Success
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI Assistant error", e)
                val errorText = e.localizedMessage ?: "Không thể kết nối trợ lý AI. Vui lòng thử lại."
                _aiUiState.value = AiUiState.Error(errorText)
            }
        }
    }

    fun clearAiError() {
        if (_aiUiState.value is AiUiState.Error) {
            _aiUiState.value = AiUiState.Idle
        }
    }

    fun consumeSnackbar() {
        _snackbarMessage.value = null
    }
}

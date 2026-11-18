package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.DishCategories
import com.example.data.local.DishEntity
import com.example.ui.components.GreenBiteBottomBar
import com.example.ui.components.GreenBiteNavRail
import com.example.ui.components.GreenBiteTopBar
import com.example.ui.components.MainTab
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AiNutritionAssistantSheet
import com.example.ui.screens.CartDrawerOverlay
import com.example.ui.screens.DishDetailSheet
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.WeeklyPlanScreen
import com.example.ui.viewmodel.GreenBiteViewModel

@Composable
fun GreenBiteApp(
    viewModel: GreenBiteViewModel = viewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: MainTab.HOME.route
    val currentTab = MainTab.entries.find { it.route == currentRoute } ?: MainTab.HOME

    val allDishes by viewModel.allDishes.collectAsStateWithLifecycle()
    val filteredDishes by viewModel.filteredDishes.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val isMenuLoading by viewModel.isMenuLoading.collectAsStateWithLifecycle()
    val cartSummary by viewModel.cartSummary.collectAsStateWithLifecycle()
    val cartBounceSignal by viewModel.cartBounceSignal.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val weeklyPlan by viewModel.weeklyPlan.collectAsStateWithLifecycle()
    val latestConfirmedOrder by viewModel.latestConfirmedOrder.collectAsStateWithLifecycle()
    val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val aiUiState by viewModel.aiUiState.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    var isCartOpen by remember { mutableStateOf(false) }
    var isAiSheetOpen by remember { mutableStateOf(false) }
    var selectedDishForDetail by remember { mutableStateOf<DishEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeSnackbar()
        }
    }

    fun navigateToTab(tab: MainTab) {
        navController.navigate(tab.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavRail = maxWidth >= 640.dp

            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    GreenBiteTopBar(
                        cartCount = cartSummary.totalCount,
                        cartBounceSignal = cartBounceSignal,
                        onCartClick = { isCartOpen = true },
                        onBrandClick = { navigateToTab(MainTab.HOME) }
                    )
                },
                bottomBar = {
                    if (!useNavRail) {
                        GreenBiteBottomBar(
                            currentTab = currentTab,
                            onSelectTab = ::navigateToTab
                        )
                    }
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = { isAiSheetOpen = true },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "Trợ lý dinh dưỡng AI"
                            )
                        },
                        text = {
                            Text("Trợ lý AI", fontWeight = FontWeight.Bold)
                        },
                        modifier = Modifier.testTag("ai_assistant_fab")
                    )
                },
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavRail) {
                        GreenBiteNavRail(
                            currentTab = currentTab,
                            onSelectTab = ::navigateToTab
                        )
                    }

                    NavHost(
                        navController = navController,
                        startDestination = MainTab.HOME.route,
                        modifier = Modifier.weight(1f)
                    ) {
                        composable(MainTab.HOME.route) {
                            HomeScreen(
                                dishes = allDishes,
                                reviews = reviews,
                                isLoading = isMenuLoading,
                                onOrderNowClick = {
                                    viewModel.selectCategory(DishCategories.ALL)
                                    navigateToTab(MainTab.MENU)
                                },
                                onSelectCategory = { category ->
                                    viewModel.selectCategory(category)
                                    navigateToTab(MainTab.MENU)
                                },
                                onOpenDishDetail = { dish -> selectedDishForDetail = dish },
                                onQuickAddDish = { dish -> viewModel.addDishToCart(dish) },
                                onOpenWeeklyPlan = { navigateToTab(MainTab.WEEKLY) },
                                onOpenAiAssistant = { isAiSheetOpen = true }
                            )
                        }

                        composable(MainTab.MENU.route) {
                            MenuScreen(
                                dishes = filteredDishes,
                                reviews = reviews,
                                filterState = filterState,
                                isLoading = isMenuLoading,
                                onSearchChange = viewModel::updateSearchQuery,
                                onSelectCategory = viewModel::selectCategory,
                                onToggleTag = viewModel::toggleDietTag,
                                onSelectPriceFilter = viewModel::selectPriceFilter,
                                onSelectCalorieFilter = viewModel::selectCalorieFilter,
                                onResetFilters = viewModel::resetFilters,
                                onOpenDishDetail = { dish -> selectedDishForDetail = dish },
                                onQuickAddDish = { dish -> viewModel.addDishToCart(dish) }
                            )
                        }

                        composable(MainTab.WEEKLY.route) {
                            WeeklyPlanScreen(
                                planState = weeklyPlan,
                                onSelectGoal = viewModel::updateWeeklyGoal,
                                onSelectMealsPerDay = viewModel::updateWeeklyMealsPerDay,
                                onSelectDays = viewModel::updateWeeklyDays,
                                onAddPlanToCart = viewModel::addWeeklyPlanToCart
                            )
                        }

                        composable(MainTab.ADMIN.route) {
                            AdminScreen(
                                orders = orders,
                                dishes = allDishes,
                                onUpdateOrderStatus = viewModel::updateOrderStatus,
                                onSaveDish = viewModel::saveDish,
                                onDeleteDish = viewModel::deleteDish
                            )
                        }
                    }
                }
            }
        }

        // Dish Detail & Review Bottom Sheet
        selectedDishForDetail?.let { dish ->
            val latestDish = allDishes.find { it.id == dish.id } ?: dish
            DishDetailSheet(
                dish = latestDish,
                reviews = reviews,
                onDismiss = { selectedDishForDetail = null },
                onAddToCart = { size, qty, note ->
                    viewModel.addDishToCart(latestDish, size = size, quantity = qty, note = note)
                },
                onSubmitReview = { author, rating, comment ->
                    viewModel.addReview(latestDish.id, author, rating, comment)
                }
            )
        }

        // AI Nutrition Assistant Modal Sheet
        if (isAiSheetOpen) {
            AiNutritionAssistantSheet(
                messages = aiMessages,
                aiUiState = aiUiState,
                onDismiss = { isAiSheetOpen = false },
                onSendAdviceRequest = viewModel::askAiNutritionist,
                onAddSuggestedDish = { dish -> viewModel.addDishToCart(dish) },
                onClearError = viewModel::clearAiError
            )
        }

        // Right-side Sliding Cart & Checkout Drawer
        CartDrawerOverlay(
            visible = isCartOpen,
            cartSummary = cartSummary,
            confirmedOrder = latestConfirmedOrder,
            onClose = { isCartOpen = false },
            onUpdateQuantity = viewModel::updateCartQuantity,
            onRemoveItem = viewModel::removeCartItem,
            onApplyPromo = viewModel::applyPromoCode,
            onClearPromo = viewModel::clearPromoCode,
            onSubmitOrder = viewModel::submitOrder,
            onDismissConfirmedOrder = viewModel::dismissConfirmedOrder,
            onExploreMenu = { navigateToTab(MainTab.MENU) }
        )
    }
}

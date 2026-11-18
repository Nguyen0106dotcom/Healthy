package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.DietTags
import com.example.data.local.DishCategories
import com.example.data.local.DishEntity
import com.example.data.local.ReviewEntity
import com.example.ui.components.DishCard
import com.example.ui.components.ShimmerDishCard
import com.example.ui.viewmodel.CalorieFilter
import com.example.ui.viewmodel.MenuFilterState
import com.example.ui.viewmodel.PriceFilter

@Composable
fun MenuScreen(
    dishes: List<DishEntity>,
    reviews: List<ReviewEntity>,
    filterState: MenuFilterState,
    isLoading: Boolean,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onToggleTag: (String) -> Unit,
    onSelectPriceFilter: (PriceFilter) -> Unit,
    onSelectCalorieFilter: (CalorieFilter) -> Unit,
    onResetFilters: () -> Unit,
    onOpenDishDetail: (DishEntity) -> Unit,
    onQuickAddDish: (DishEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val allCategories = listOf(DishCategories.ALL) + DishCategories.list
    val hasActiveFilters = filterState.searchQuery.isNotBlank() ||
        filterState.selectedCategory != DishCategories.ALL ||
        filterState.selectedTags.isNotEmpty() ||
        filterState.priceFilter != PriceFilter.ALL ||
        filterState.calorieFilter != CalorieFilter.ALL

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val columns = when {
            maxWidth >= 840.dp -> 3
            maxWidth >= 560.dp -> 2
            else -> 2
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 108.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(columns) }) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Search field
                    OutlinedTextField(
                        value = filterState.searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Tìm salad, ức gà, smoothie, keto...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Tìm kiếm"
                            )
                        },
                        trailingIcon = {
                            if (filterState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Xóa từ khóa"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("menu_search_input")
                    )

                    // Category chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allCategories) { category ->
                            val selected = filterState.selectedCategory == category
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectCategory(category) },
                                label = {
                                    Text(
                                        text = category,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    // Diet tags
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = "Chế độ ăn",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        items(DietTags.all) { tag ->
                            val selected = filterState.selectedTags.contains(tag)
                            FilterChip(
                                selected = selected,
                                onClick = { onToggleTag(tag) },
                                label = { Text(tag, style = MaterialTheme.typography.labelMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                )
                            )
                        }
                    }

                    // Price & Calorie range pills
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PriceFilter.entries) { priceFilter ->
                            val selected = filterState.priceFilter == priceFilter
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectPriceFilter(priceFilter) },
                                label = { Text(priceFilter.label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                        items(CalorieFilter.entries.filter { it != CalorieFilter.ALL }) { calFilter ->
                            val selected = filterState.calorieFilter == calFilter
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    onSelectCalorieFilter(
                                        if (selected) CalorieFilter.ALL else calFilter
                                    )
                                },
                                label = { Text(calFilter.label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hiển thị ${dishes.size} món ăn lành mạnh",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (hasActiveFilters) {
                            TextButton(onClick = onResetFilters) {
                                Text("Xóa bộ lọc")
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                items(6) {
                    ShimmerDishCard()
                }
            } else if (dishes.isEmpty()) {
                item(span = { GridItemSpan(columns) }) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SearchOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = "Không tìm thấy món ăn phù hợp",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Hãy thử từ khóa khác hoặc đặt lại bộ lọc để xem toàn bộ thực đơn GreenBite.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(onClick = onResetFilters, shape = RoundedCornerShape(16.dp)) {
                                Text("Đặt lại bộ lọc")
                            }
                        }
                    }
                }
            } else {
                items(dishes, key = { it.id }) { dish ->
                    val dishReviews = reviews.filter { it.dishId == dish.id }
                    val avg = if (dishReviews.isNotEmpty()) dishReviews.map { it.rating }.average() else null
                    DishCard(
                        dish = dish,
                        avgRating = avg,
                        reviewCount = dishReviews.size,
                        onCardClick = { onOpenDishDetail(dish) },
                        onQuickAdd = { onQuickAddDish(dish) }
                    )
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.formatVnd
import com.example.data.local.DishCategories
import com.example.data.local.DishEntity
import com.example.data.local.ReviewEntity
import com.example.ui.components.DishCard
import com.example.ui.components.DishPhoto
import com.example.ui.components.ShimmerDishCard

private data class CategoryTile(
    val name: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun HomeScreen(
    dishes: List<DishEntity>,
    reviews: List<ReviewEntity>,
    isLoading: Boolean,
    onOrderNowClick: () -> Unit,
    onSelectCategory: (String) -> Unit,
    onOpenDishDetail: (DishEntity) -> Unit,
    onQuickAddDish: (DishEntity) -> Unit,
    onOpenWeeklyPlan: () -> Unit,
    onOpenAiAssistant: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bestSellers = dishes.filter { it.isBestSeller }
    val comboDeals = dishes.filter { it.isComboDeal }

    val categories = listOf(
        CategoryTile(DishCategories.SALAD, "Tươi giòn", Icons.Filled.Eco, Color(0xFF4CAF50)),
        CategoryTile(DishCategories.HEALTHY_BOWL, "Đủ chất", Icons.Filled.RiceBowl, Color(0xFF2E7D32)),
        CategoryTile(DishCategories.EAT_CLEAN_MAIN, "Giàu đạm", Icons.Filled.LunchDining, Color(0xFFFF8A3D)),
        CategoryTile(DishCategories.SMOOTHIE_JUICE, "Detox mát", Icons.Filled.LocalDrink, Color(0xFF00897B)),
        CategoryTile(DishCategories.LOW_SUGAR_SNACK, "Nhẹ bụng", Icons.Filled.BakeryDining, Color(0xFF8D6E63)),
        CategoryTile(DishCategories.WEEKLY_COMBO, "Tiết kiệm", Icons.Filled.Spa, Color(0xFFE65100))
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // 1. Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .height(250.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner),
                    contentDescription = "Bữa ăn healthy tươi sạch từ GreenBite",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xE6142817),
                                    Color(0x991C3620),
                                    Color(0x22000000)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = Color(0xFFFF8A3D),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "BẾP EAT CLEAN • TÍNH CALO CHUẨN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Ăn sạch – Sống khỏe",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Nguyên liệu hữu cơ tươi mới mỗi sáng, cân bằng dinh dưỡng cho vóc dáng năng động.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.92f),
                            maxLines = 2
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onOrderNowClick,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("hero_order_now_button")
                        ) {
                            Text(
                                text = "Đặt món ngay",
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = onOpenAiAssistant,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White,
                                containerColor = Color.Black.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.testTag("hero_ai_consult_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Tư vấn AI", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // 2. Danh mục
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Danh mục lành mạnh",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onOrderNowClick) {
                        Text("Xem tất cả")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories) { tile ->
                        Surface(
                            onClick = { onSelectCategory(tile.name) },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .width(136.dp)
                                .testTag("home_category_${tile.name}")
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = tile.accentColor.copy(alpha = 0.14f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = tile.icon,
                                            contentDescription = tile.name,
                                            tint = tile.accentColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = tile.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = tile.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Món bán chạy
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Món bán chạy",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Được khách hàng GreenBite yêu thích nhất",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = onOrderNowClick) {
                        Text("Thực đơn")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (isLoading) {
                        items(3) {
                            ShimmerDishCard(modifier = Modifier.width(248.dp))
                        }
                    } else {
                        items(bestSellers, key = { it.id }) { dish ->
                            val dishReviews = reviews.filter { it.dishId == dish.id }
                            val avg = if (dishReviews.isNotEmpty()) dishReviews.map { it.rating }.average() else null
                            DishCard(
                                dish = dish,
                                avgRating = avg,
                                reviewCount = dishReviews.size,
                                onCardClick = { onOpenDishDetail(dish) },
                                onQuickAdd = { onQuickAddDish(dish) },
                                modifier = Modifier.width(248.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Combo ưu đãi
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalOffer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Combo ưu đãi hôm nay",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    comboDeals.forEach { combo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenDishDetail(combo) },
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DishPhoto(
                                    imageUrl = combo.imageUrl,
                                    fallbackKey = combo.fallbackKey,
                                    contentDescription = combo.name,
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        val saving = (combo.originalPrice ?: combo.price) - combo.price
                                        Text(
                                            text = if (saving > 0) "Tiết kiệm ${formatVnd(saving)}" else "Combo tiết kiệm",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = combo.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${combo.calories} kcal • P:${combo.protein}g",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = formatVnd(combo.price),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            if (combo.originalPrice != null) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = formatVnd(combo.originalPrice),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    textDecoration = TextDecoration.LineThrough,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Button(
                                            onClick = { onQuickAddDish(combo) },
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text("Đặt combo", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Weekly Meal Plan Banner
        item {
            Surface(
                onClick = onOpenWeeklyPlan,
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gói Ăn Theo Tuần",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Tùy chỉnh Giảm cân / Giữ dáng / Tăng cơ – Giảm đến 15%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Mở gói tuần",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

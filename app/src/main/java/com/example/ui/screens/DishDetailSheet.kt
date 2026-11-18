package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.formatDateTime
import com.example.core.formatVnd
import com.example.data.local.DishEntity
import com.example.data.local.ReviewEntity
import com.example.ui.components.DishPhoto
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DishDetailSheet(
    dish: DishEntity,
    reviews: List<ReviewEntity>,
    onDismiss: () -> Unit,
    onAddToCart: (size: String, quantity: Int, note: String) -> Unit,
    onSubmitReview: (authorName: String, rating: Int, comment: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedSize by remember(dish.id) { mutableStateOf("M") }
    var quantity by remember(dish.id) { mutableIntStateOf(1) }
    var note by remember(dish.id) { mutableStateOf("") }

    var reviewAuthor by remember(dish.id) { mutableStateOf("") }
    var reviewRating by remember(dish.id) { mutableIntStateOf(5) }
    var reviewComment by remember(dish.id) { mutableStateOf("") }

    val multiplier = when (selectedSize) {
        "S" -> 0.85
        "L" -> 1.25
        else -> 1.0
    }
    val adjustedPrice = ((dish.price * multiplier) / 1000.0).roundToInt() * 1000
    val adjustedCalories = (dish.calories * multiplier).roundToInt()
    val adjustedProtein = (dish.protein * multiplier).roundToInt()
    val adjustedCarbs = (dish.carbs * multiplier).roundToInt()
    val adjustedFat = (dish.fat * multiplier).roundToInt()

    val dishReviews = reviews.filter { it.dishId == dish.id }
    val avgRating = if (dishReviews.isNotEmpty()) dishReviews.map { it.rating }.average() else 5.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = dish.category,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Đóng chi tiết")
                }
            }

            DishPhoto(
                imageUrl = dish.imageUrl,
                fallbackKey = dish.fallbackKey,
                contentDescription = dish.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(22.dp))
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = dish.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (dishReviews.isEmpty()) "Chưa có đánh giá" else String.format("%.1f/5 (%d đánh giá)", avgRating, dishReviews.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = dish.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Diet tags
            if (dish.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dish.tags.forEach { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Nutrition breakdown card
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MacroStat("Calo", "$adjustedCalories kcal", MaterialTheme.colorScheme.secondary)
                    MacroStat("Protein", "${adjustedProtein}g", MaterialTheme.colorScheme.primary)
                    MacroStat("Carb", "${adjustedCarbs}g", MaterialTheme.colorScheme.onSurface)
                    MacroStat("Fat", "${adjustedFat}g", MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Ingredients
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Nguyên liệu tươi sạch",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dish.ingredients.forEach { ingredient ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = ingredient,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Size selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Chọn kích cỡ phần ăn",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        "S" to "Nhỏ (S)",
                        "M" to "Chuẩn (M)",
                        "L" to "Lớn (L)"
                    ).forEach { (code, label) ->
                        FilterChip(
                            selected = selectedSize == code,
                            onClick = { selectedSize = code },
                            label = { Text(label, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }

            // Note input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Ghi chú cho bếp (không bắt buộc)") },
                placeholder = { Text("Ví dụ: Không hành, ít sốt, để riêng sốt") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Quantity & Add to cart CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Remove, contentDescription = "Giảm số lượng")
                        }
                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = { quantity++ },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = "Tăng số lượng")
                        }
                    }
                }

                Button(
                    onClick = {
                        onAddToCart(selectedSize, quantity, note)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("detail_add_to_cart_button")
                ) {
                    Text(
                        text = "Thêm vào giỏ • ${formatVnd(adjustedPrice * quantity)}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Reviews Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Đánh giá từ khách hàng (${dishReviews.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Viết đánh giá của bạn",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            (1..5).forEach { star ->
                                Icon(
                                    imageVector = if (star <= reviewRating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Chấm $star sao",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clickable { reviewRating = star }
                                        .padding(2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$reviewRating/5 sao",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        OutlinedTextField(
                            value = reviewAuthor,
                            onValueChange = { reviewAuthor = it },
                            label = { Text("Tên của bạn") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = reviewComment,
                            onValueChange = { reviewComment = it },
                            label = { Text("Nhận xét về hương vị, độ tươi...") },
                            minLines = 2,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                onSubmitReview(reviewAuthor, reviewRating, reviewComment)
                                reviewComment = ""
                                reviewAuthor = ""
                            },
                            enabled = reviewComment.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Gửi đánh giá")
                        }
                    }
                }

                if (dishReviews.isEmpty()) {
                    Text(
                        text = "Hãy là người đầu tiên nhận xét món này!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    dishReviews.forEach { review ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = review.authorName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = formatDateTime(review.createdAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row {
                                    repeat(review.rating) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFB300),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = review.comment,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

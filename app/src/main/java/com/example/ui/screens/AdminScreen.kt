package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.formatDateTime
import com.example.core.formatVnd
import com.example.data.local.DietTags
import com.example.data.local.DishCategories
import com.example.data.local.DishEntity
import com.example.data.local.OrderEntity
import com.example.data.local.OrderStatuses
import com.example.ui.components.DishPhoto

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminScreen(
    orders: List<OrderEntity>,
    dishes: List<DishEntity>,
    onUpdateOrderStatus: (orderCode: String, status: String) -> Unit,
    onSaveDish: (DishEntity) -> Unit,
    onDeleteDish: (DishEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableIntStateOf(0) }
    var editingDish by remember { mutableStateOf<DishEntity?>(null) }
    var showDishDialog by remember { mutableStateOf(false) }
    var deletingDish by remember { mutableStateOf<DishEntity?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Quản trị GreenBite (/admin)",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = { Icon(Icons.Filled.ReceiptLong, contentDescription = null) }
                ) {
                    Text("Đơn hàng (${orders.size})")
                }
                SegmentedButton(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = { Icon(Icons.Filled.RestaurantMenu, contentDescription = null) }
                ) {
                    Text("Món ăn (${dishes.size})")
                }
            }
        }

        if (selectedSection == 0) {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (orders.isEmpty()) {
                    item {
                        Text(
                            text = "Chưa có đơn hàng nào.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                } else {
                    items(orders, key = { it.orderCode }) { order ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#${order.orderCode}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = formatDateTime(order.createdAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${order.customerName} • ${order.phone}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Địa chỉ: ${order.address} (${order.deliveryTime})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = order.itemsSummary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Thanh toán: ${order.paymentMethod}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatVnd(order.totalAmount),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                HorizontalDivider()
                                Text(
                                    text = "Cập nhật trạng thái:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OrderStatuses.all.forEach { status ->
                                        val isCurrent = order.status == status
                                        FilterChip(
                                            selected = isCurrent,
                                            onClick = { onUpdateOrderStatus(order.orderCode, status) },
                                            label = { Text(status, style = MaterialTheme.typography.labelSmall) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = when (status) {
                                                    OrderStatuses.COMPLETED -> MaterialTheme.colorScheme.primary
                                                    OrderStatuses.DELIVERING -> MaterialTheme.colorScheme.secondary
                                                    else -> MaterialTheme.colorScheme.primaryContainer
                                                },
                                                selectedLabelColor = if (status == OrderStatuses.COMPLETED || status == OrderStatuses.DELIVERING) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Button(
                        onClick = {
                            editingDish = null
                            showDishDialog = true
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_add_dish_button")
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Thêm món ăn mới", fontWeight = FontWeight.Bold)
                    }
                }

                items(dishes, key = { it.id }) { dish ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DishPhoto(
                                imageUrl = dish.imageUrl,
                                fallbackKey = dish.fallbackKey,
                                contentDescription = dish.name,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dish.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${dish.category} • ${formatVnd(dish.price)} • ${dish.calories} kcal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    editingDish = dish
                                    showDishDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Sửa món",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { deletingDish = dish }) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Xóa món",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDishDialog) {
        DishEditorDialog(
            initialDish = editingDish,
            onDismiss = { showDishDialog = false },
            onSave = { saved ->
                onSaveDish(saved)
                showDishDialog = false
            }
        )
    }

    deletingDish?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingDish = null },
            title = { Text("Xóa món ăn?") },
            text = { Text("Bạn có chắc muốn xóa \"${target.name}\" khỏi thực đơn không?") },
            confirmButton = {
                Button(onClick = {
                    onDeleteDish(target)
                    deletingDish = null
                }) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingDish = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DishEditorDialog(
    initialDish: DishEntity?,
    onDismiss: () -> Unit,
    onSave: (DishEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialDish?.name ?: "") }
    var category by remember { mutableStateOf(initialDish?.category ?: DishCategories.SALAD) }
    var priceText by remember { mutableStateOf((initialDish?.price ?: 65000).toString()) }
    var caloriesText by remember { mutableStateOf((initialDish?.calories ?: 350).toString()) }
    var proteinText by remember { mutableStateOf((initialDish?.protein ?: 28).toString()) }
    var carbsText by remember { mutableStateOf((initialDish?.carbs ?: 30).toString()) }
    var fatText by remember { mutableStateOf((initialDish?.fat ?: 12).toString()) }
    var selectedTags by remember { mutableStateOf(initialDish?.tags?.toSet() ?: setOf(DietTags.LOW_SUGAR)) }
    var description by remember { mutableStateOf(initialDish?.description ?: "") }
    var ingredients by remember { mutableStateOf(initialDish?.ingredientsCsv ?: "Rau hữu cơ, Sốt healthy") }
    var isBestSeller by remember { mutableStateOf(initialDish?.isBestSeller ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialDish == null) "Thêm món ăn mới" else "Chỉnh sửa món ăn") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên món *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Danh mục", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DishCategories.list.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                        label = { Text("Giá (VNĐ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = caloriesText,
                        onValueChange = { caloriesText = it.filter { c -> c.isDigit() } },
                        label = { Text("Calo (kcal)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = proteinText,
                        onValueChange = { proteinText = it.filter { c -> c.isDigit() } },
                        label = { Text("Protein (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = carbsText,
                        onValueChange = { carbsText = it.filter { c -> c.isDigit() } },
                        label = { Text("Carb (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fatText,
                        onValueChange = { fatText = it.filter { c -> c.isDigit() } },
                        label = { Text("Fat (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text("Nhãn chế độ ăn", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DietTags.all.forEach { tag ->
                        val active = selectedTags.contains(tag)
                        FilterChip(
                            selected = active,
                            onClick = {
                                selectedTags = if (active) selectedTags - tag else selectedTags + tag
                            },
                            label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả món ăn") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ingredients,
                    onValueChange = { ingredients = it },
                    label = { Text("Nguyên liệu (cách nhau dấu phẩy)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isBestSeller, onCheckedChange = { isBestSeller = it })
                    Text("Đánh dấu Món bán chạy", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fallbackKey = when (category) {
                        DishCategories.SALAD -> "salad"
                        DishCategories.SMOOTHIE_JUICE -> "smoothie"
                        DishCategories.HEALTHY_BOWL -> "bowl"
                        else -> "hero"
                    }
                    onSave(
                        DishEntity(
                            id = initialDish?.id ?: 0,
                            name = name.trim(),
                            category = category,
                            price = priceText.toIntOrNull() ?: 65000,
                            originalPrice = initialDish?.originalPrice,
                            calories = caloriesText.toIntOrNull() ?: 350,
                            protein = proteinText.toIntOrNull() ?: 25,
                            carbs = carbsText.toIntOrNull() ?: 30,
                            fat = fatText.toIntOrNull() ?: 10,
                            tagsCsv = selectedTags.joinToString(","),
                            description = description.trim().ifEmpty { "Món ăn lành mạnh chuẩn vị GreenBite." },
                            ingredientsCsv = ingredients.trim(),
                            imageUrl = initialDish?.imageUrl ?: "",
                            fallbackKey = initialDish?.fallbackKey ?: fallbackKey,
                            isBestSeller = isBestSeller,
                            isComboDeal = initialDish?.isComboDeal ?: false
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text("Lưu món")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

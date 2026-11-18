package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.core.formatVnd
import com.example.data.local.DishEntity
import com.example.ui.components.DishPhoto
import com.example.ui.viewmodel.AiChatMessage
import com.example.ui.viewmodel.AiUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiNutritionAssistantSheet(
    messages: List<AiChatMessage>,
    aiUiState: AiUiState,
    onDismiss: () -> Unit,
    onSendAdviceRequest: (
        heightCm: Int?,
        weightKg: Int?,
        age: Int?,
        goal: String,
        allergies: String,
        message: String
    ) -> Unit,
    onAddSuggestedDish: (DishEntity) -> Unit,
    onClearError: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()

    var showProfileForm by remember { mutableStateOf(true) }
    var heightText by remember { mutableStateOf("165") }
    var weightText by remember { mutableStateOf("58") }
    var ageText by remember { mutableStateOf("26") }
    var selectedGoal by remember { mutableStateOf("Giảm cân") }
    var allergiesText by remember { mutableStateOf("") }
    var chatInput by remember { mutableStateOf("") }

    LaunchedEffect(messages.size, aiUiState) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Trợ lý Dinh dưỡng AI",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tính calo & gợi ý món GreenBite",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showProfileForm = !showProfileForm }) {
                        Text(if (showProfileForm) "Ẩn chỉ số" else "Chỉ số")
                        Icon(
                            imageVector = if (showProfileForm) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Đóng trợ lý AI")
                    }
                }
            }

            // Collapsible profile calculator form
            AnimatedVisibility(visible = showProfileForm) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = heightText,
                                onValueChange = { heightText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Cao (cm)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = weightText,
                                onValueChange = { weightText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Nặng (kg)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = ageText,
                                onValueChange = { ageText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Tuổi") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(0.85f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Giảm cân", "Giữ dáng", "Tăng cơ").forEach { goal ->
                                FilterChip(
                                    selected = selectedGoal == goal,
                                    onClick = { selectedGoal = goal },
                                    label = { Text(goal, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = allergiesText,
                            onValueChange = { allergiesText = it },
                            label = { Text("Dị ứng / Lưu ý (vd: hải sản, đậu phộng)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                showProfileForm = false
                                onSendAdviceRequest(
                                    heightText.toIntOrNull(),
                                    weightText.toIntOrNull(),
                                    ageText.toIntOrNull(),
                                    selectedGoal,
                                    allergiesText,
                                    chatInput
                                )
                                chatInput = ""
                            },
                            enabled = aiUiState !is AiUiState.Loading,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_calculate_suggest_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tính calo & gợi ý món ăn ngay", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            HorizontalDivider()

            // Chat stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(6.dp)) }

                items(messages, key = { it.id }) { msg ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = if (msg.isUser) 18.dp else 4.dp,
                                bottomEnd = if (msg.isUser) 4.dp else 18.dp
                            ),
                            color = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = if (msg.isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth(0.92f)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (msg.dailyCalories != null) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.LocalFireDepartment,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Nhu cầu ước tính: ~${msg.dailyCalories} kcal/ngày",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                        }
                                    }
                                }

                                if (!msg.macroSummary.isNullOrBlank()) {
                                    Text(
                                        text = msg.macroSummary,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface
                                )

                                if (!msg.isUser && msg.recommendedDishes.isNotEmpty()) {
                                    Text(
                                        text = "Gợi ý dành riêng cho bạn:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    msg.recommendedDishes.forEach { (dish, reason) ->
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.background,
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
                                                        .size(56.dp)
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
                                                        text = "${dish.calories} kcal • ${formatVnd(dish.price)}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = reason,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Button(
                                                    onClick = { onAddSuggestedDish(dish) },
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.height(38.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.AddShoppingCart,
                                                        contentDescription = "Thêm vào giỏ",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Thêm", style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                        }
                                    }
                                    Text(
                                        text = "* Thông tin mang tính tham khảo dinh dưỡng hàng ngày, không thay thế tư vấn y tế chuyên sâu.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                if (aiUiState is AiUiState.Loading) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp
                                )
                                Text(
                                    text = "AI đang phân tích thực đơn phù hợp...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                if (aiUiState is AiUiState.Error) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Không thể nhận gợi ý từ AI",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = aiUiState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                TextButton(onClick = onClearError) {
                                    Text("Đóng thông báo")
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(6.dp)) }
            }

            // Bottom chat input bar
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 6.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Hỏi thêm: 'Bữa tối nhẹ dưới 350 kcal'...") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (chatInput.isNotBlank() && aiUiState !is AiUiState.Loading) {
                                val prompt = chatInput
                                chatInput = ""
                                onSendAdviceRequest(
                                    heightText.toIntOrNull(),
                                    weightText.toIntOrNull(),
                                    ageText.toIntOrNull(),
                                    selectedGoal,
                                    allergiesText,
                                    prompt
                                )
                            }
                        },
                        enabled = chatInput.isNotBlank() && aiUiState !is AiUiState.Loading,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Gửi tin nhắn",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

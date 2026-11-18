package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.formatDateTime
import com.example.core.formatVnd
import com.example.data.local.CartItemEntity
import com.example.data.local.OrderEntity
import com.example.ui.components.DishPhoto
import com.example.ui.viewmodel.CartSummary

private enum class DrawerStep {
    CART,
    CHECKOUT
}

@Composable
fun CartDrawerOverlay(
    visible: Boolean,
    cartSummary: CartSummary,
    confirmedOrder: OrderEntity?,
    onClose: () -> Unit,
    onUpdateQuantity: (CartItemEntity, Int) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onApplyPromo: (String) -> Unit,
    onClearPromo: () -> Unit,
    onSubmitOrder: (
        customerName: String,
        phone: String,
        address: String,
        deliveryTime: String,
        note: String,
        paymentMethod: String
    ) -> Unit,
    onDismissConfirmedOrder: () -> Unit,
    onExploreMenu: () -> Unit
) {
    var step by remember(visible) { mutableStateOf(DrawerStep.CART) }

    if (visible) {
        BackHandler {
            when {
                confirmedOrder != null -> {
                    onDismissConfirmedOrder()
                    onClose()
                }
                step == DrawerStep.CHECKOUT -> step = DrawerStep.CART
                else -> onClose()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (confirmedOrder != null) onDismissConfirmedOrder()
                            onClose()
                        }
                    )
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 440.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    when {
                        confirmedOrder != null -> {
                            OrderConfirmedContent(
                                order = confirmedOrder,
                                onDone = {
                                    onDismissConfirmedOrder()
                                    step = DrawerStep.CART
                                    onClose()
                                }
                            )
                        }
                        step == DrawerStep.CHECKOUT -> {
                            CheckoutFormContent(
                                cartSummary = cartSummary,
                                onBack = { step = DrawerStep.CART },
                                onSubmitOrder = onSubmitOrder
                            )
                        }
                        else -> {
                            CartDrawerContent(
                                cartSummary = cartSummary,
                                onClose = onClose,
                                onUpdateQuantity = onUpdateQuantity,
                                onRemoveItem = onRemoveItem,
                                onApplyPromo = onApplyPromo,
                                onClearPromo = onClearPromo,
                                onProceedCheckout = { step = DrawerStep.CHECKOUT },
                                onExploreMenu = {
                                    onClose()
                                    onExploreMenu()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartDrawerContent(
    cartSummary: CartSummary,
    onClose: () -> Unit,
    onUpdateQuantity: (CartItemEntity, Int) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onApplyPromo: (String) -> Unit,
    onClearPromo: () -> Unit,
    onProceedCheckout: () -> Unit,
    onExploreMenu: () -> Unit
) {
    var promoInput by remember(cartSummary.promoCode) { mutableStateOf(cartSummary.promoCode) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Giỏ hàng (${cartSummary.totalCount})",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                if (cartSummary.totalCalories > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tổng năng lượng: ${cartSummary.totalCalories} kcal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            IconButton(onClick = onClose, modifier = Modifier.testTag("close_cart_button")) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Đóng giỏ hàng")
            }
        }

        HorizontalDivider()

        if (cartSummary.items.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(84.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingBag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Giỏ hàng đang trống",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Chọn ngay salad tươi mát, healthy bowl hay nước ép detox cho bữa ăn lành mạnh hôm nay!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onExploreMenu,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Khám phá thực đơn")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(modifier = Modifier.height(6.dp)) }
                items(cartSummary.items, key = { it.id }) { item ->
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
                                imageUrl = item.imageUrl,
                                fallbackKey = item.fallbackKey,
                                contentDescription = item.dishName,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.dishName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Size ${item.size} • ${item.calories * item.quantity} kcal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (item.note.isNotBlank()) {
                                    Text(
                                        text = "Ghi chú: ${item.note}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatVnd(item.unitPrice * item.quantity),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                IconButton(
                                    onClick = { onRemoveItem(item.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = "Xóa món",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onUpdateQuantity(item, item.quantity - 1) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Remove,
                                                contentDescription = "Giảm",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = item.quantity.toString(),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                        IconButton(
                                            onClick = { onUpdateQuantity(item, item.quantity + 1) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Add,
                                                contentDescription = "Tăng",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(6.dp)) }
            }

            // Promo & Totals Footer
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = promoInput,
                            onValueChange = { promoInput = it },
                            label = { Text("Mã giảm giá (vd: HEALTHY10)") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("promo_code_input")
                        )
                        Button(
                            onClick = {
                                if (cartSummary.promoCode.isNotEmpty() && promoInput.equals(cartSummary.promoCode, ignoreCase = true)) {
                                    promoInput = ""
                                    onClearPromo()
                                } else {
                                    onApplyPromo(promoInput)
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text(if (cartSummary.promoCode.isNotEmpty() && promoInput.equals(cartSummary.promoCode, ignoreCase = true)) "Bỏ mã" else "Áp dụng")
                        }
                    }

                    if (!cartSummary.promoMessage.isNullOrBlank()) {
                        Text(
                            text = cartSummary.promoMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (cartSummary.isPromoError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tạm tính", style = MaterialTheme.typography.bodyMedium)
                        Text(formatVnd(cartSummary.subtotal), style = MaterialTheme.typography.bodyMedium)
                    }
                    if (cartSummary.discountAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Giảm giá (${cartSummary.promoCode})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "-${formatVnd(cartSummary.discountAmount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Tổng thanh toán",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            formatVnd(cartSummary.finalTotal),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = onProceedCheckout,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("proceed_checkout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCartCheckout,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tiến hành đặt hàng", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CheckoutFormContent(
    cartSummary: CartSummary,
    onBack: () -> Unit,
    onSubmitOrder: (String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var deliveryTime by remember { mutableStateOf("Giao ngay (30 phút)") }
    var paymentMethod by remember { mutableStateOf("COD") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val timeSlots = listOf(
        "Giao ngay (30 phút)",
        "11:30 - 12:00 Hôm nay",
        "17:30 - 18:00 Hôm nay",
        "08:00 - 08:30 Sáng mai"
    )
    val paymentOptions: List<Triple<String, String, ImageVector>> = listOf(
        Triple("COD", "Thanh toán khi nhận hàng (COD)", Icons.Filled.Payments),
        Triple("Chuyển khoản", "Chuyển khoản ngân hàng (VietQR)", Icons.Filled.AccountBalance),
        Triple("Ví điện tử", "Ví MoMo / ZaloPay (Giả lập)", Icons.Filled.Wallet)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại giỏ hàng"
                )
            }
            Text(
                text = "Thông tin giao hàng",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; validationError = null },
                label = { Text("Họ và tên *") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_name_input")
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; validationError = null },
                label = { Text("Số điện thoại *") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_phone_input")
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it; validationError = null },
                label = { Text("Địa chỉ giao hàng *") },
                minLines = 2,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_address_input")
            )

            Text(
                text = "Thời gian giao",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeSlots.forEach { slot ->
                    FilterChip(
                        selected = deliveryTime == slot,
                        onClick = { deliveryTime = slot },
                        label = { Text(slot, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Ghi chú giao hàng (không bắt buộc)") },
                placeholder = { Text("Ví dụ: Giao tại sảnh lễ tân, gọi khi tới") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Phương thức thanh toán",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                paymentOptions.forEach { (key, title, icon) ->
                    val selected = paymentMethod == key
                    Surface(
                        onClick = { paymentMethod = key },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selected, onClick = { paymentMethod = key })
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            if (validationError != null) {
                Text(
                    text = validationError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        when {
                            name.isBlank() -> validationError = "Vui lòng nhập họ và tên người nhận."
                            phone.isBlank() -> validationError = "Vui lòng nhập số điện thoại liên hệ."
                            address.isBlank() -> validationError = "Vui lòng nhập địa chỉ giao hàng."
                            else -> onSubmitOrder(name, phone, address, deliveryTime, note, paymentMethod)
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_order_button")
                ) {
                    Text(
                        text = "Xác nhận đặt hàng • ${formatVnd(cartSummary.finalTotal)}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderConfirmedContent(
    order: OrderEntity,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(84.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp)
                )
            }
        }

        Text(
            text = "Đặt hàng thành công!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Bếp GreenBite đã nhận đơn và đang chuẩn bị món ăn tươi ngon cho bạn.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mã đơn hàng", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "#${order.orderCode}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Thời gian đặt", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatDateTime(order.createdAt), style = MaterialTheme.typography.bodySmall)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Người nhận", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${order.customerName} (${order.phone})", fontWeight = FontWeight.Medium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Giao tới", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(order.address, textAlign = TextAlign.End, modifier = Modifier.weight(1f).padding(start = 12.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Thanh toán", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(order.paymentMethod, fontWeight = FontWeight.SemiBold)
                }
                HorizontalDivider()
                Text(
                    text = order.itemsSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tổng thanh toán", fontWeight = FontWeight.Bold)
                    Text(
                        text = formatVnd(order.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (order.paymentMethod != "COD") {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Hướng dẫn ${order.paymentMethod} (Giả lập)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Nội dung: ${order.orderCode} • Số tiền: ${formatVnd(order.totalAmount)} • Ngân hàng GreenBank - 0988886666",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(
            onClick = onDone,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("order_confirm_done_button")
        ) {
            Text("Tiếp tục mua sắm", fontWeight = FontWeight.Bold)
        }
    }
}

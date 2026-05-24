package com.example.pechka

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pechka.ui.DeliveryViewModel
import com.example.pechka.ui.RoundingScreen
import com.example.pechka.ui.RouteScreen
import com.example.pechka.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("pechka_prefs", Context.MODE_PRIVATE) }
    
    var firstName by remember { mutableStateOf(sharedPrefs.getString("first_name", "") ?: "") }
    var lastName by remember { mutableStateOf(sharedPrefs.getString("last_name", "") ?: "") }
    
    val isRegistered = firstName.isNotBlank() && lastName.isNotBlank()

    if (!isRegistered) {
        OnboardingScreen(
            onSaveName = { first, last ->
                sharedPrefs.edit()
                    .putString("first_name", first)
                    .putString("last_name", last)
                    .apply()
                firstName = first
                lastName = last
            }
        )
    } else {
        AppContentScreen(firstName = firstName, lastName = lastName)
    }
}

@Composable
fun OnboardingScreen(onSaveName: (String, String) -> Unit) {
    var firstNameInput by remember { mutableStateOf("") }
    var lastNameInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(PrimaryContainerGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = "Хлебный Путь",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ХЛЕБНЫЙ ПУТЬ",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryGreen
        )

        Text(
            text = "Приложение водителя-экспедитора",
            fontSize = 14.sp,
            color = SecondaryGray,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, OutlineVariant, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "РЕГИСТРАЦИЯ ВОДИТЕЛЯ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryGray
                )

                OutlinedTextField(
                    value = lastNameInput,
                    onValueChange = { lastNameInput = it },
                    label = { Text("Фамилия") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = OutlineGray
                    )
                )

                OutlinedTextField(
                    value = firstNameInput,
                    onValueChange = { firstNameInput = it },
                    label = { Text("Имя") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = OutlineGray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val isButtonEnabled = firstNameInput.isNotBlank() && lastNameInput.isNotBlank()
        Button(
            onClick = {
                if (isButtonEnabled) {
                    onSaveName(firstNameInput.trim(), lastNameInput.trim())
                }
            },
            enabled = isButtonEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryContainerGreen,
                disabledContainerColor = SecondaryGray.copy(alpha = 0.3f),
                contentColor = Color.White,
                disabledContentColor = SecondaryGray
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "НАЧАТЬ РАБОТУ",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

// ============================================================
// AppContentScreen — Full tab isolation via when + rememberSaveable
// ============================================================

@Composable
fun AppContentScreen(firstName: String, lastName: String) {
    val repository = PechkaApplication.instance.repository
    val viewModel: DeliveryViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
        DeliveryViewModel(repository)
    }

    val selectedTab = rememberSaveable { mutableStateOf("orders") }

    Scaffold(
        bottomBar = {
            Surface(
                color = SurfaceContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .border(1.dp, OutlineVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavBarItem(
                        icon = Icons.Default.ShoppingCart,
                        label = "Заказы",
                        isSelected = selectedTab.value == "orders",
                        onClick = { selectedTab.value = "orders" }
                    )
                    NavBarItem(
                        icon = Icons.Default.Route,
                        label = "Рейс",
                        isSelected = selectedTab.value == "route",
                        onClick = { selectedTab.value = "route" }
                    )
                    NavBarItem(
                        icon = Icons.Default.History,
                        label = "История",
                        isSelected = selectedTab.value == "history",
                        onClick = { selectedTab.value = "history" }
                    )
                    NavBarItem(
                        icon = Icons.Default.Person,
                        label = "Профиль",
                        isSelected = selectedTab.value == "profile",
                        onClick = { selectedTab.value = "profile" }
                    )
                }
            }
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Full isolation: only the active tab is composed
            when (selectedTab.value) {
                "orders" -> OrdersTabScreen(viewModel = viewModel)
                "route" -> {
                    val isRoundingActive by viewModel.isRoundingActive.collectAsState(initial = false)
                    if (isRoundingActive) {
                        RoundingScreen(viewModel = viewModel)
                    } else {
                        RouteScreen(viewModel = viewModel)
                    }
                }
                "history" -> HistoryTabScreen(viewModel = viewModel)
                "profile" -> ProfileTabScreen(
                    viewModel = viewModel,
                    firstName = firstName,
                    lastName = lastName
                )
            }
        }
    }
}

@Composable
fun NavBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) PrimaryContainerGreen else Color.Transparent
    val contentColor = if (isSelected) OnPrimaryGreen else SecondaryGray

    Box(
        modifier = Modifier
            .widthIn(min = 64.dp, max = 88.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )
        }
    }
}

// ============================================================
// TAB 1: OrdersTabScreen (Вечерний обзвон)
// ============================================================

@Composable
fun OrdersTabScreen(viewModel: DeliveryViewModel) {
    val points by viewModel.pointsWithOrderAndPaymentFlow.collectAsState(initial = emptyList())
    val prices by viewModel.activePrices.collectAsState()
    var expandedPointId by rememberSaveable { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Заказы",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen,
            maxLines = 1,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
        )
        Text(
            text = "Вечерний обзвон — введите количество по каждой точке",
            fontSize = 13.sp,
            color = SecondaryGray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(points, key = { it.point.id }) { item ->
                val isExpanded = expandedPointId == item.point.id
                val loaves = item.order?.loavesQtyPieces ?: 0
                val grey = item.order?.greyBreadQtyPieces ?: 0
                val lavash = item.order?.lavashQtyPieces ?: 0
                val totalCost = (loaves * prices.whiteBreadPrice) +
                        (grey * prices.greyBreadPrice) +
                        (lavash * prices.lavashPrice)
                val rotationState by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExpanded) SurfaceWhite else SurfaceWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .border(
                            width = if (isExpanded) 2.dp else 1.dp,
                            color = if (isExpanded) PrimaryGreen else OutlineVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            expandedPointId =
                                if (expandedPointId == item.point.id) null else item.point.id
                        }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Header row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.point.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (!isExpanded && (loaves > 0 || grey > 0 || lavash > 0)) {
                                    Text(
                                        text = "Б:$loaves  С:$grey  Л:$lavash  →  ${totalCost.toInt()} ₽",
                                        fontSize = 13.sp,
                                        color = SecondaryGray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                            if (totalCost > 0 && !isExpanded) {
                                Text(
                                    text = "${totalCost.toInt()} ₽",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryContainerGreen
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Раскрыть",
                                tint = SecondaryGray,
                                modifier = Modifier
                                    .rotate(rotationState)
                                    .size(24.dp)
                            )
                        }

                        // Accordion: input fields
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceContainerLow)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                var loavesVal by remember(loaves) { mutableStateOf(loaves.toString()) }
                                var greyVal by remember(grey) { mutableStateOf(grey.toString()) }
                                var lavashVal by remember(lavash) { mutableStateOf(lavash.toString()) }

                                // Loaves input
                                OutlinedTextField(
                                    value = loavesVal,
                                    onValueChange = { input ->
                                        if (input.isEmpty() || input.all { it.isDigit() }) {
                                            loavesVal = input
                                            viewModel.updateOrderQuantities(
                                                item.point.id,
                                                input.toIntOrNull() ?: 0,
                                                greyVal.toIntOrNull() ?: 0,
                                                lavashVal.toIntOrNull() ?: 0
                                            )
                                        }
                                    },
                                    label = { Text("Буханки (Белый), шт") },
                                    textStyle = TextStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreen
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryGreen,
                                        unfocusedBorderColor = OutlineGray
                                    )
                                )

                                // Grey bread input
                                OutlinedTextField(
                                    value = greyVal,
                                    onValueChange = { input ->
                                        if (input.isEmpty() || input.all { it.isDigit() }) {
                                            greyVal = input
                                            viewModel.updateOrderQuantities(
                                                item.point.id,
                                                loavesVal.toIntOrNull() ?: 0,
                                                input.toIntOrNull() ?: 0,
                                                lavashVal.toIntOrNull() ?: 0
                                            )
                                        }
                                    },
                                    label = { Text("Серый хлеб, шт") },
                                    textStyle = TextStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreen
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryGreen,
                                        unfocusedBorderColor = OutlineGray
                                    )
                                )

                                // Lavash input
                                OutlinedTextField(
                                    value = lavashVal,
                                    onValueChange = { input ->
                                        if (input.isEmpty() || input.all { it.isDigit() }) {
                                            lavashVal = input
                                            viewModel.updateOrderQuantities(
                                                item.point.id,
                                                loavesVal.toIntOrNull() ?: 0,
                                                greyVal.toIntOrNull() ?: 0,
                                                input.toIntOrNull() ?: 0
                                            )
                                        }
                                    },
                                    label = { Text("Лаваш, шт") },
                                    textStyle = TextStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreen
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryGreen,
                                        unfocusedBorderColor = OutlineGray
                                    )
                                )

                                // Cost summary
                                val currentCost = ((loavesVal.toIntOrNull() ?: 0) * prices.whiteBreadPrice) +
                                        ((greyVal.toIntOrNull() ?: 0) * prices.greyBreadPrice) +
                                        ((lavashVal.toIntOrNull() ?: 0) * prices.lavashPrice)

                                HorizontalDivider(color = OutlineVariant)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Итого:",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SecondaryGray
                                    )
                                    Text(
                                        text = "${currentCost.toInt()} ₽",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PrimaryContainerGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// TAB 3: HistoryTabScreen (История закрытых точек)
// ============================================================

@Composable
fun HistoryPointRow(
    item: com.example.pechka.data.PointWithOrderAndPayment,
    formatter: SimpleDateFormat
) {
    val timestamp = item.payment?.timestamp ?: 0L
    val timeStr = if (timestamp > 0) formatter.format(Date(timestamp)) else "--:--"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite, RoundedCornerShape(8.dp))
            .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.point.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PrimaryGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(SurfaceContainer, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = timeStr,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGray
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            val order = item.order
            if (order != null) {
                Text(
                    text = "Буханки: ${order.loavesQtyPieces} шт. • Серый: ${order.greyBreadQtyPieces} шт. • Лаваш: ${order.lavashQtyPieces} шт.",
                    fontSize = 12.sp,
                    color = SecondaryGray
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            val typeText = if (item.payment?.paymentType == "CASH") "Наличные" else "Безнал"
            Text(
                text = "Оплата: $typeText",
                fontSize = 12.sp,
                color = if (item.payment?.paymentType == "CASH") PrimaryContainerGreen else Color(0xFF1976D2)
            )
        }
        Text(
            text = "${item.payment?.amountPaid?.toInt() ?: 0} ₽",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = PrimaryContainerGreen
        )
    }
}

@Composable
fun HistoryTabScreen(
    viewModel: DeliveryViewModel
) {
    val points by viewModel.pointsWithOrderAndPaymentFlow.collectAsState(initial = emptyList())
    val pastHistory by viewModel.pastHistoryFlow.collectAsState(initial = emptyList())
    val formatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val completedPoints = remember(points) {
        points.filter { it.point.status == "COMPLETED" }
    }
    val pastRoutes = remember(pastHistory) {
        pastHistory
            .groupBy { it.routeId }
            .entries
            .sortedByDescending { it.key }
            .take(3)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "История рейса",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ТЕКУЩИЙ СТАТУС РЕЙСА",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Выполнено точек: ${completedPoints.size} из ${points.size}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            // Current Route Header
            item {
                Text(
                    text = "Текущий рейс",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryGray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (completedPoints.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceWhite, RoundedCornerShape(8.dp))
                            .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Нет закрытых точек в текущем рейсе.", color = SecondaryGray)
                    }
                }
            } else {
                items(completedPoints) { item ->
                    HistoryPointRow(item, formatter)
                }
            }

            if (pastRoutes.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Предыдущие рейсы (до 3-х дней)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                pastRoutes.forEach { (pastRouteId, pastPoints) ->
                    val totalSum = pastPoints.sumOf { it.amountPaid }
                    val dateStr = if (pastPoints.isNotEmpty()) {
                        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(pastPoints.first().timestamp))
                    } else {
                        "Дата неизвестна"
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Рейс №$pastRouteId ($dateStr)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PrimaryGreen
                                )
                                Text(
                                    text = "${totalSum.toInt()} ₽",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PrimaryContainerGreen
                                )
                            }
                        }
                    }

                    items(pastPoints) { pastItem ->
                        Box(modifier = Modifier.padding(start = 12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceWhite, RoundedCornerShape(8.dp))
                                    .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = pastItem.pointName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = PrimaryGreen
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(SurfaceContainer, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = formatter.format(Date(pastItem.timestamp)),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SecondaryGray
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Буханки: ${pastItem.loavesQty} шт. • Серый: ${pastItem.greyBreadQty} шт. • Лаваш: ${pastItem.lavashQty} шт.",
                                        fontSize = 12.sp,
                                        color = SecondaryGray
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val typeText = if (pastItem.paymentType == "CASH") "Наличные" else "Безнал"
                                    Text(
                                        text = "Оплата: $typeText",
                                        fontSize = 12.sp,
                                        color = if (pastItem.paymentType == "CASH") PrimaryContainerGreen else Color(0xFF1976D2)
                                    )
                                }
                                Text(
                                    text = "${pastItem.amountPaid.toInt()} ₽",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = PrimaryContainerGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// TAB 4: ProfileTabScreen (ФИО + Касса + Цены + Магазины + Сброс)
// ============================================================

@Composable
fun ProfileTabScreen(
    viewModel: DeliveryViewModel,
    firstName: String,
    lastName: String
) {
    val points by viewModel.pointsWithOrderAndPaymentFlow.collectAsState(initial = emptyList())
    val prices by viewModel.activePrices.collectAsState()
    val shiftState by viewModel.shiftStateFlow.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showAddPointDialog by remember { mutableStateOf(false) }
    var showShipmentDialog by remember { mutableStateOf(false) }
    var editingPoint by remember { mutableStateOf<com.example.pechka.data.PointWithOrderAndPayment?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var deletingPoint by remember { mutableStateOf<com.example.pechka.data.PointWithOrderAndPayment?>(null) }

    // Prices editing state
    var whitePriceInput by remember(prices) { mutableStateOf(prices.whiteBreadPrice.toInt().toString()) }
    var greyPriceInput by remember(prices) { mutableStateOf(prices.greyBreadPrice.toInt().toString()) }
    var lavashPriceInput by remember(prices) { mutableStateOf(prices.lavashPrice.toInt().toString()) }

    // Expenses + crates editing state
    var expensesInput by remember(shiftState) { mutableStateOf(shiftState.expenses.toInt().toString()) }
    var cratesCollectedWhiteInput by remember(shiftState) { mutableStateOf(shiftState.cratesCollectedWhite.toString()) }
    var cratesReturnedWhiteInput by remember(shiftState) { mutableStateOf(shiftState.cratesReturnedWhite.toString()) }
    var cratesCollectedGreyInput by remember(shiftState) { mutableStateOf(shiftState.cratesCollectedGrey.toString()) }
    var cratesReturnedGreyInput by remember(shiftState) { mutableStateOf(shiftState.cratesReturnedGrey.toString()) }

    // Revenue / cash calculations (with returns)
    val completed = remember(points) { points.filter { it.point.status == "COMPLETED" } }

    val whiteRevenue = completed.sumOf {
        val o = it.order ?: return@sumOf 0.0
        ((o.loavesQtyPieces - o.loavesReturnPieces).coerceAtLeast(0)) * prices.whiteBreadPrice
    }
    val greyRevenue = completed.sumOf {
        val o = it.order ?: return@sumOf 0.0
        ((o.greyBreadQtyPieces - o.greyReturnPieces).coerceAtLeast(0)) * prices.greyBreadPrice
    }
    val lavashRevenue = completed.sumOf {
        val o = it.order ?: return@sumOf 0.0
        ((o.lavashQtyPieces - o.lavashReturnPieces).coerceAtLeast(0)) * prices.lavashPrice
    }

    val cashSum = completed.filter { it.payment?.paymentType == "CASH" }.sumOf { it.payment?.amountPaid ?: 0.0 }
    val cardSum = completed.filter { it.payment?.paymentType == "CARD" }.sumOf { it.payment?.amountPaid ?: 0.0 }
    val totalSum = cashSum + cardSum
    val netCash = cashSum - shiftState.expenses

    // Confirm day reset dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = "Завершить день?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Оплаты будут очищены, статусы точек вернутся в «Ожидает». Количества заказов сохранятся как шаблон для следующего рейса.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.resetDay()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                ) {
                    Text("Завершить", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Отмена", color = SecondaryGray)
                }
            },
            containerColor = SurfaceWhite
        )
    }

    // Rename Dialog
    if (editingPoint != null) {
        AlertDialog(
            onDismissRequest = { editingPoint = null },
            title = { Text("Переименовать магазин") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("Название магазина") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val p = editingPoint
                    if (p != null && renameInput.isNotBlank()) {
                        viewModel.updatePointName(p.point.id, renameInput.trim())
                    }
                    editingPoint = null
                }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPoint = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Delete Dialog
    if (deletingPoint != null) {
        AlertDialog(
            onDismissRequest = { deletingPoint = null },
            title = { Text("Удалить магазин?") },
            text = { Text("Вы действительно хотите удалить магазин \"${deletingPoint?.point?.name}\" из системы?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val p = deletingPoint
                        if (p != null) {
                            viewModel.deletePoint(p.point.id)
                        }
                        deletingPoint = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                ) {
                    Text("Удалить", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingPoint = null }) {
                    Text("Отмена", color = SecondaryGray)
                }
            }
        )
    }

    // Shipment check dialog
    if (showShipmentDialog) {
        AlertDialog(
            onDismissRequest = { showShipmentDialog = false },
            title = {
                Text(
                    text = "Проверка отгрузки",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PrimaryGreen
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (points.isEmpty()) {
                        item {
                            Text(
                                "Маршрут пуст.",
                                color = SecondaryGray
                            )
                        }
                    } else {
                        items(points, key = { it.point.id }) { item ->
                            val b = item.order?.loavesQtyPieces ?: 0
                            val s = item.order?.greyBreadQtyPieces ?: 0
                            val l = item.order?.lavashQtyPieces ?: 0
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceContainerLow, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.point.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PrimaryGreen,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Б:$b | С:$s | Л:$l",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showShipmentDialog = false }) {
                    Text("Закрыть", fontWeight = FontWeight.Bold, color = PrimaryGreen)
                }
            },
            containerColor = SurfaceWhite
        )
    }

    // Add point dialog
    if (showAddPointDialog) {
        var newPointName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddPointDialog = false },
            title = {
                Text("Добавить торговую точку", fontWeight = FontWeight.Bold, color = PrimaryGreen)
            },
            text = {
                OutlinedTextField(
                    value = newPointName,
                    onValueChange = { newPointName = it },
                    label = { Text("Название точки") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPointName.isNotBlank()) {
                            viewModel.addPoint(newPointName.trim(), points.size + 1)
                            showAddPointDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainerGreen)
                ) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPointDialog = false }) {
                    Text("Отмена", color = SecondaryGray)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        // Driver info
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(PrimaryContainerGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Водитель",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$lastName $firstName",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
                Text(
                    text = "Водитель-экспедитор",
                    fontSize = 13.sp,
                    color = SecondaryGray
                )
            }
        }

        // === Блок «Выручка по товарам» ===
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ВЫРУЧКА ПО ТОВАРАМ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGray,
                        maxLines = 1
                    )
                    HorizontalDivider(color = OutlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Буханки белый:",
                            fontWeight = FontWeight.Bold,
                            color = SecondaryGray,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${whiteRevenue.toInt()} ₽",
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryGreen,
                            fontSize = 18.sp,
                            maxLines = 1
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Серый хлеб:",
                            fontWeight = FontWeight.Bold,
                            color = SecondaryGray,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${greyRevenue.toInt()} ₽",
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryGreen,
                            fontSize = 18.sp,
                            maxLines = 1
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Лаваш:",
                            fontWeight = FontWeight.Bold,
                            color = SecondaryGray,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${lavashRevenue.toInt()} ₽",
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryGreen,
                            fontSize = 18.sp,
                            maxLines = 1
                        )
                    }
                    HorizontalDivider(color = OutlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Итого выручка:", fontWeight = FontWeight.Bold, color = PrimaryGreen, maxLines = 1)
                        Text(
                            "${(whiteRevenue + greyRevenue + lavashRevenue).toInt()} ₽",
                            fontWeight = FontWeight.Black,
                            color = PrimaryContainerGreen,
                            fontSize = 20.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // === Блок «Касса» ===
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "КАССА (ПОТОКИ)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGray,
                        maxLines = 1
                    )
                    HorizontalDivider(color = OutlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Наличные:",
                            fontWeight = FontWeight.Bold,
                            color = SecondaryGray,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${cashSum.toInt()} ₽",
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryContainerGreen,
                            fontSize = 20.sp,
                            maxLines = 1
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Безнал (Карты):",
                            fontWeight = FontWeight.Bold,
                            color = SecondaryGray,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${cardSum.toInt()} ₽",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1976D2),
                            fontSize = 20.sp,
                            maxLines = 1
                        )
                    }
                    HorizontalDivider(color = OutlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Всего собрано:", fontWeight = FontWeight.Bold, color = PrimaryGreen, maxLines = 1)
                        Text(
                            "${totalSum.toInt()} ₽",
                            fontWeight = FontWeight.Black,
                            color = PrimaryGreen,
                            fontSize = 20.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // === Блок «Расходы и Чистая касса» ===
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "РАСХОДЫ И ЧИСТАЯ КАССА",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGray,
                        maxLines = 1
                    )
                    OutlinedTextField(
                        value = expensesInput,
                        onValueChange = { v ->
                            if (v.isEmpty() || v.all { it.isDigit() }) {
                                expensesInput = v
                                viewModel.updateExpenses(v.toDoubleOrNull() ?: 0.0)
                            }
                        },
                        label = { Text("Расходы за смену (₽)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ErrorRed,
                            unfocusedBorderColor = OutlineGray
                        )
                    )
                    HorizontalDivider(color = OutlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Чистая наличка\nв кармане:",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${netCash.toInt()} ₽",
                            fontWeight = FontWeight.Black,
                            color = if (netCash >= 0) PrimaryContainerGreen else ErrorRed,
                            fontSize = 24.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // === Блок «Тара» ===
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ТАРА (ЛОТКИ)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGray,
                        maxLines = 1
                    )
                    HorizontalDivider(color = OutlineVariant)
                    Text(
                        text = "Белый",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cratesCollectedWhiteInput,
                            onValueChange = { v ->
                                if (v.isEmpty() || v.all { it.isDigit() }) {
                                    cratesCollectedWhiteInput = v
                                    viewModel.updateCrates(
                                        v.toIntOrNull() ?: 0,
                                        cratesReturnedWhiteInput.toIntOrNull() ?: 0,
                                        cratesCollectedGreyInput.toIntOrNull() ?: 0,
                                        cratesReturnedGreyInput.toIntOrNull() ?: 0
                                    )
                                }
                            },
                            label = { Text("Собрано (шт)", fontSize = 11.sp, maxLines = 1) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        )
                        OutlinedTextField(
                            value = cratesReturnedWhiteInput,
                            onValueChange = { v ->
                                if (v.isEmpty() || v.all { it.isDigit() }) {
                                    cratesReturnedWhiteInput = v
                                    viewModel.updateCrates(
                                        cratesCollectedWhiteInput.toIntOrNull() ?: 0,
                                        v.toIntOrNull() ?: 0,
                                        cratesCollectedGreyInput.toIntOrNull() ?: 0,
                                        cratesReturnedGreyInput.toIntOrNull() ?: 0
                                    )
                                }
                            },
                            label = { Text("Сдано (шт)", fontSize = 11.sp, maxLines = 1) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                    HorizontalDivider(color = OutlineVariant)
                    Text(
                        text = "Серый",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cratesCollectedGreyInput,
                            onValueChange = { v ->
                                if (v.isEmpty() || v.all { it.isDigit() }) {
                                    cratesCollectedGreyInput = v
                                    viewModel.updateCrates(
                                        cratesCollectedWhiteInput.toIntOrNull() ?: 0,
                                        cratesReturnedWhiteInput.toIntOrNull() ?: 0,
                                        v.toIntOrNull() ?: 0,
                                        cratesReturnedGreyInput.toIntOrNull() ?: 0
                                    )
                                }
                            },
                            label = { Text("Собрано (шт)", fontSize = 11.sp, maxLines = 1) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        )
                        OutlinedTextField(
                            value = cratesReturnedGreyInput,
                            onValueChange = { v ->
                                if (v.isEmpty() || v.all { it.isDigit() }) {
                                    cratesReturnedGreyInput = v
                                    viewModel.updateCrates(
                                        cratesCollectedWhiteInput.toIntOrNull() ?: 0,
                                        cratesReturnedWhiteInput.toIntOrNull() ?: 0,
                                        cratesCollectedGreyInput.toIntOrNull() ?: 0,
                                        v.toIntOrNull() ?: 0
                                    )
                                }
                            },
                            label = { Text("Сдано (шт)", fontSize = 11.sp, maxLines = 1) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // === Кнопка «Проверить отгрузку» ===
        item {
            Button(
                onClick = { showShipmentDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryContainerGray,
                    contentColor = OnSecondaryContainerGray
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = "Проверить отгрузку",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "ПРОВЕРИТЬ ОТГРУЗКУ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }
            }
        }

        // Prices section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ЦЕНЫ (₽/ШТ)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGray
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = whitePriceInput,
                            onValueChange = { whitePriceInput = it },
                            label = { Text("Буханка", fontSize = 11.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        )
                        OutlinedTextField(
                            value = greyPriceInput,
                            onValueChange = { greyPriceInput = it },
                            label = { Text("Серый", fontSize = 11.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        )
                        OutlinedTextField(
                            value = lavashPriceInput,
                            onValueChange = { lavashPriceInput = it },
                            label = { Text("Лаваш", fontSize = 11.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                    Button(
                        onClick = {
                            val w = whitePriceInput.toDoubleOrNull() ?: prices.whiteBreadPrice
                            val g = greyPriceInput.toDoubleOrNull() ?: prices.greyBreadPrice
                            val l = lavashPriceInput.toDoubleOrNull() ?: prices.lavashPrice
                            viewModel.updatePrices(w, g, l)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainerGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("СОХРАНИТЬ ЦЕНЫ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Store management section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "НАСТРОЙКИ МАГАЗИНОВ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryGray
                        )
                        IconButton(onClick = { showAddPointDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Добавить",
                                tint = PrimaryContainerGreen
                            )
                        }
                    }

                    points.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceContainerLow, RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.point.sequenceOrder}. ${item.point.name}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Row {
                                IconButton(
                                    onClick = {
                                        editingPoint = item
                                        renameInput = item.point.name
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Редактировать",
                                        tint = PrimaryContainerGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { deletingPoint = item },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Удалить",
                                        tint = ErrorRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Day reset button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { showConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "СБРОСИТЬ ДЕНЬ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

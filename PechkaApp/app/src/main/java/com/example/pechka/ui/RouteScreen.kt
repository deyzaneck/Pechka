package com.example.pechka.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pechka.data.PointWithOrderAndPayment
import com.example.pechka.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(
    viewModel: DeliveryViewModel,
    modifier: Modifier = Modifier
) {
    val points by viewModel.pointsWithOrderAndPaymentFlow.collectAsState(initial = emptyList())
    val isRouteStarted by viewModel.isRouteStarted.collectAsState(initial = false)
    val prices by viewModel.activePrices.collectAsState()
    var showDetailDialogPointId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = "Рейс",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = if (isRouteStarted) "Маршрутный лист" else "Загрузка",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = PrimaryGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite
                )
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        if (!isRouteStarted) {
            LoadingSummaryPhase(
                points = points,
                viewModel = viewModel,
                modifier = modifier.padding(innerPadding)
            )
        } else {
            RouteSheetPhase(
                points = points,
                prices = prices,
                showDetailDialogPointId = showDetailDialogPointId,
                onShowDetails = { showDetailDialogPointId = it },
                onReopenPoint = { viewModel.reopenPoint(it) },
                modifier = modifier.padding(innerPadding)
            )
        }

        // Detail Dialog (Phase 3)
        if (showDetailDialogPointId != null) {
            val detailPoint = points.find { it.point.id == showDetailDialogPointId }
            if (detailPoint != null) {
                DetailDialog(
                    point = detailPoint,
                    prices = prices,
                    onDismiss = { showDetailDialogPointId = null },
                    onSaveReturn = { white, grey, lavash ->
                        viewModel.saveReturn(detailPoint.point.id, white, grey, lavash)
                    },
                    onClosePoint = { paymentType, amount, white, grey, lavash ->
                        viewModel.saveReturn(detailPoint.point.id, white, grey, lavash)
                        viewModel.closePoint(detailPoint.point.id, paymentType, amount)
                        showDetailDialogPointId = null
                    }
                )
            }
        }
    }
}

// ============================================================
// PHASE 1: Loading Summary (before route starts)
// ============================================================

@Composable
fun LoadingSummaryPhase(
    points: List<PointWithOrderAndPayment>,
    viewModel: DeliveryViewModel,
    modifier: Modifier = Modifier
) {
    val totalWhite = points.sumOf { it.order?.loavesQtyPieces ?: 0 }
    val totalGrey = points.sumOf { it.order?.greyBreadQtyPieces ?: 0 }
    val totalLavash = points.sumOf { it.order?.lavashQtyPieces ?: 0 }

    val whiteTrays = totalWhite / 16.0
    val greyTrays = totalGrey / 16.0

    val needsRounding = (totalWhite % 16 != 0) || (totalGrey % 16 != 0)

    fun formatTrays(trays: Double): String {
        return if (trays % 1.0 == 0.0) {
            trays.toInt().toString()
        } else {
            String.format(java.util.Locale.US, "%.1f", trays)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (points.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = SecondaryGray.copy(alpha = 0.35f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Нет заказов",
                    color = SecondaryGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "Внесите количество на вкладке «Заказы»",
                    color = SecondaryGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
                )
            }
        } else {
            Text(
                text = "СВОДНЫЙ ОТЧЁТ ДЛЯ СКЛАДА",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SecondaryGray
            )

            // White bread summary
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Буханки (Белый)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PrimaryGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${formatTrays(whiteTrays)} л. × 16 шт.",
                            fontSize = 13.sp,
                            color = SecondaryGray,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$totalWhite шт",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = PrimaryGreen
                    )
                }
            }

            // Grey bread summary
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Серый хлеб",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PrimaryGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${formatTrays(greyTrays)} л. × 16 шт.",
                            fontSize = 13.sp,
                            color = SecondaryGray,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$totalGrey шт",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = PrimaryGreen
                    )
                }
            }

            // Lavash summary
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Лаваш",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PrimaryGreen,
                            maxLines = 1
                        )
                        Text(
                            text = "Поштучно",
                            fontSize = 13.sp,
                            color = SecondaryGray,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$totalLavash шт",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = PrimaryGreen
                    )
                }
            }

            if (needsRounding) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = WarningOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠ Лотки дробные — потребуется округление",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp),
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val hasOrders = totalWhite > 0 || totalGrey > 0 || totalLavash > 0
            Button(
                onClick = {
                    viewModel.startRoute()
                },
                enabled = hasOrders,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryContainerGreen,
                    disabledContainerColor = SecondaryGray.copy(alpha = 0.3f),
                    contentColor = Color.White,
                    disabledContentColor = SecondaryGray
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "СФОРМИРОВАТЬ ЗАГРУЗКУ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
            }
        }
    }
}

// ============================================================
// PHASE 3: Route Sheet (pieces only, no trays)
// ============================================================

@Composable
fun RouteSheetPhase(
    points: List<PointWithOrderAndPayment>,
    prices: com.example.pechka.data.PriceEntity,
    showDetailDialogPointId: Long?,
    onShowDetails: (Long) -> Unit,
    onReopenPoint: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalWhite = points.sumOf { it.order?.loavesQtyPieces ?: 0 }
    val totalGrey = points.sumOf { it.order?.greyBreadQtyPieces ?: 0 }
    val totalLavash = points.sumOf { it.order?.lavashQtyPieces ?: 0 }
    val whiteTrays = totalWhite / 16.0
    val greyTrays = totalGrey / 16.0

    fun formatTrays(trays: Double): String {
        return if (trays % 1.0 == 0.0) trays.toInt().toString()
        else String.format(java.util.Locale.US, "%.1f", trays)
    }

    val totalCompleted = points.count { it.point.status == "COMPLETED" }

    Column(modifier = modifier.fillMaxSize()) {
        // Sticky top summary bar (Phase 3)
        if (points.isNotEmpty()) {
            Surface(
                color = PrimaryContainerGreen,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ЗАГРУЖЕНО В МАШИНУ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$totalCompleted / ${points.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                    val summaryText = buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)) {
                            append("Белый: ")
                        }
                        withStyle(SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)) {
                            append("${formatTrays(whiteTrays)} л.")
                        }
                        withStyle(SpanStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)) {
                            append("  |  Серый: ")
                        }
                        withStyle(SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)) {
                            append("${formatTrays(greyTrays)} л.")
                        }
                        withStyle(SpanStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)) {
                            append("  |  Лаваш: ")
                        }
                        withStyle(SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)) {
                            append("$totalLavash шт")
                        }
                    }
                    Text(
                        text = summaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp)
        ) {
            if (points.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 56.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = SecondaryGray.copy(alpha = 0.35f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Маршрут пуст",
                            color = SecondaryGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Добавьте точки в Профиле",
                            color = SecondaryGray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                items(points, key = { it.point.id }) { point ->
                    RoutePointCard(
                        point = point,
                        prices = prices,
                        onShowDetails = { onShowDetails(point.point.id) },
                        onReopenPoint = { onReopenPoint(point.point.id) }
                    )
                }
            }
        }
    }
}

// ============================================================
// RoutePointCard — FIXED: single-line text, no vertical wrapping
// ============================================================

@Composable
fun ProductQtyBadge(label: String, qty: Int, color: Color) {
    Surface(
        color = Color(0xFFFFF8F4), // soft cream
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.border(1.dp, OutlineVariant, RoundedCornerShape(6.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SecondaryGray
            )
            Text(
                text = "$qty шт",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}

@Composable
fun RoutePointCard(
    point: PointWithOrderAndPayment,
    prices: com.example.pechka.data.PriceEntity,
    onShowDetails: () -> Unit,
    onReopenPoint: () -> Unit
) {
    val isCompleted = point.point.status == "COMPLETED"
    val loaves = point.order?.loavesQtyPieces ?: 0
    val grey = point.order?.greyBreadQtyPieces ?: 0
    val lavash = point.order?.lavashQtyPieces ?: 0

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) SurfaceContainerLow else SurfaceWhite
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineVariant, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual left-border accent status indicator
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(if (isCompleted) PrimaryGreen else WarningOrange)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Сдано",
                        tint = PrimaryContainerGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Text block — takes all remaining space via weight(1f)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Store name — single line, ellipsis on overflow
                    Text(
                        text = point.point.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) SecondaryGray else PrimaryGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )

                    // Product chip badges row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProductQtyBadge(label = "Бел", qty = loaves, color = PrimaryGreen)
                        ProductQtyBadge(label = "Сер", qty = grey, color = PrimaryGreen)
                        ProductQtyBadge(label = "Лав", qty = lavash, color = PrimaryGreen)
                    }

                    // Status badge
                    if (isCompleted) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SecondaryContainerGray)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "СДАНО",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = OnSecondaryContainerGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Action button — fixed size, never overlaps text
                if (!isCompleted) {
                    Button(
                        onClick = onShowDetails,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryContainerGreen,
                            contentColor = OnPrimaryGreen
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .widthIn(min = 92.dp, max = 110.dp)
                    ) {
                        Text(
                            text = "ОТКРЫТЬ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.widthIn(min = 64.dp)
                    ) {
                        Text(
                            text = (point.payment?.amountPaid?.toInt() ?: 0).asRubles(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryContainerGreen,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Вернуть",
                            color = ErrorRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onReopenPoint() }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// DetailDialog — Close point dialog (payment selection)
// ============================================================

@Composable
fun ReturnStepperRow(
    label: String,
    maxQty: Int,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color(0xFFFFFDFB), RoundedCornerShape(8.dp))
            .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = OnSecondaryContainerGray,
            modifier = Modifier.width(64.dp),
            maxLines = 1
        )

        IconButton(
            onClick = {
                val current = value.toIntOrNull() ?: 0
                if (current > 0) {
                    val newVal = current - 1
                    onValueChange(if (newVal > 0) newVal.toString() else "")
                }
            },
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFFBEBDB), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Уменьшить",
                tint = PrimaryContainerGreen,
                modifier = Modifier.size(16.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (value.isEmpty()) "0" else value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = PrimaryGreen
            )
        }

        IconButton(
            onClick = {
                val current = value.toIntOrNull() ?: 0
                if (current < maxQty) {
                    val newVal = current + 1
                    onValueChange(newVal.toString())
                }
            },
            modifier = Modifier
                .size(36.dp)
                .background(PrimaryContainerGreen, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Увеличить",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun DetailDialog(
    point: PointWithOrderAndPayment,
    prices: com.example.pechka.data.PriceEntity,
    onDismiss: () -> Unit,
    onSaveReturn: (Int, Int, Int) -> Unit,
    onClosePoint: (String, Double, Int, Int, Int) -> Unit
) {
    val order = point.order
    val loavesQty = order?.loavesQtyPieces ?: 0
    val greyQty = order?.greyBreadQtyPieces ?: 0
    val lavashQty = order?.lavashQtyPieces ?: 0

    val initialWhiteReturn = point.returns?.whiteReturnQty ?: 0
    val initialGreyReturn = point.returns?.greyReturnQty ?: 0
    val initialLavashReturn = point.returns?.lavashReturnQty ?: 0

    var whiteReturnInput by remember(point.point.id) { mutableStateOf(if (initialWhiteReturn > 0) initialWhiteReturn.toString() else "") }
    var greyReturnInput by remember(point.point.id) { mutableStateOf(if (initialGreyReturn > 0) initialGreyReturn.toString() else "") }
    var lavashReturnInput by remember(point.point.id) { mutableStateOf(if (initialLavashReturn > 0) initialLavashReturn.toString() else "") }

    val whiteReturn = (whiteReturnInput.toIntOrNull() ?: 0).coerceIn(0, loavesQty)
    val greyReturn = (greyReturnInput.toIntOrNull() ?: 0).coerceIn(0, greyQty)
    val lavashReturn = (lavashReturnInput.toIntOrNull() ?: 0).coerceIn(0, lavashQty)

    val calculatedSum = ((loavesQty - whiteReturn) * prices.whiteBreadPrice) +
            ((greyQty - greyReturn) * prices.greyBreadPrice) +
            ((lavashQty - lavashReturn) * prices.lavashPrice)

    var paymentType by remember { mutableStateOf("CASH") }
    var amountInput by remember(calculatedSum) { mutableStateOf(calculatedSum.toInt().toString()) }
    val enteredAmount = amountInput.toDoubleOrNull() ?: 0.0
    val isCloseEnabled = amountInput.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = point.point.name,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = PrimaryGreen,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Receipt Check Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OutlineVariant, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ДЕТАЛИ ЗАКАЗА",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryGray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        // White bread row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Хлеб Б:", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = "$loavesQty шт × ${prices.whiteBreadPrice.toInt()} ₽",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }
                        
                        // Grey bread row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Серый:", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = "$greyQty шт × ${prices.greyBreadPrice.toInt()} ₽",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }

                        val breadSum = ((loavesQty - whiteReturn) * prices.whiteBreadPrice) +
                                ((greyQty - greyReturn) * prices.greyBreadPrice)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Хлеб итого: ${breadSum.asRubles()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryGray
                            )
                        }

                        HorizontalDivider(color = OutlineVariant, thickness = 1.dp)

                        // Lavash row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Лаваш:", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = "$lavashQty шт × ${prices.lavashPrice.toInt()} ₽",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }
                        
                        val lavashSum = (lavashQty - lavashReturn) * prices.lavashPrice
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Лаваш итого: ${lavashSum.asRubles()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryGray
                            )
                        }

                        // Total sum highlighted banner
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = PrimaryGreen.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ИТОГО К ОПЛАТЕ:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryContainerGreen
                                )
                                Text(
                                    text = calculatedSum.asRubles(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryContainerGreen
                                )
                            }
                        }
                    }
                }

                // Returns section ( circular steppers )
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9F4)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WarningOrange.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ВОЗВРАТЫ (ПОШТУЧНО)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSecondaryContainerGray
                        )
                        
                        ReturnStepperRow(
                            label = "Белый Б",
                            maxQty = loavesQty,
                            value = whiteReturnInput,
                            onValueChange = { input ->
                                whiteReturnInput = input
                                val newSum = (((loavesQty - ((input.toIntOrNull() ?: 0).coerceIn(0, loavesQty))) * prices.whiteBreadPrice) +
                                        ((greyQty - greyReturn) * prices.greyBreadPrice) +
                                        ((lavashQty - lavashReturn) * prices.lavashPrice))
                                amountInput = newSum.toInt().toString()
                            }
                        )

                        ReturnStepperRow(
                            label = "Серый С",
                            maxQty = greyQty,
                            value = greyReturnInput,
                            onValueChange = { input ->
                                greyReturnInput = input
                                val newSum = (((loavesQty - whiteReturn) * prices.whiteBreadPrice) +
                                        ((greyQty - ((input.toIntOrNull() ?: 0).coerceIn(0, greyQty))) * prices.greyBreadPrice) +
                                        ((lavashQty - lavashReturn) * prices.lavashPrice))
                                amountInput = newSum.toInt().toString()
                            }
                        )

                        ReturnStepperRow(
                            label = "Лаваш Л",
                            maxQty = lavashQty,
                            value = lavashReturnInput,
                            onValueChange = { input ->
                                lavashReturnInput = input
                                val newSum = (((loavesQty - whiteReturn) * prices.whiteBreadPrice) +
                                        ((greyQty - greyReturn) * prices.greyBreadPrice) +
                                        ((lavashQty - ((input.toIntOrNull() ?: 0).coerceIn(0, lavashQty))) * prices.lavashPrice))
                                amountInput = newSum.toInt().toString()
                            }
                        )

                        if (whiteReturn > 0 || greyReturn > 0 || lavashReturn > 0) {
                            Text(
                                text = "✓ Возвраты автоматически вычтены из чека",
                                fontSize = 11.sp,
                                color = PrimaryContainerGreen,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }
                }

                // Premium Segmented payment type controller
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerHigh)
                        .padding(3.dp)
                ) {
                    val isCash = paymentType == "CASH"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCash) Color.White else Color.Transparent)
                            .border(
                                width = if (isCash) 1.dp else 0.dp,
                                color = if (isCash) OutlineVariant else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { paymentType = "CASH" },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = if (isCash) PrimaryGreen else SecondaryGray,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "НАЛИЧНЫЕ",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = if (isCash) PrimaryGreen else SecondaryGray,
                                maxLines = 1
                            )
                        }
                    }
                    val isCard = paymentType == "CARD"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCard) Color.White else Color.Transparent)
                            .border(
                                width = if (isCard) 1.dp else 0.dp,
                                color = if (isCard) Color(0xFF90CAF9) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { paymentType = "CARD" },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = if (isCard) Color(0xFF1976D2) else SecondaryGray,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "БЕЗНАЛ",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = if (isCard) Color(0xFF1976D2) else SecondaryGray,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Received amount input
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Внесено водителем:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGray
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.all { it.isDigit() }) {
                                    amountInput = input
                                }
                            },
                            suffix = {
                                Text(
                                    text = "₽",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryGray
                                )
                            },
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryGreen
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                unfocusedBorderColor = OutlineGray
                            )
                        )

                        Button(
                            onClick = { amountInput = calculatedSum.toInt().toString() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SecondaryContainerGray,
                                contentColor = OnSecondaryContainerGray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text(
                                text = "Чек",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                    }

                    // Received cash dynamic preset shortcuts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val exactPreset = calculatedSum.toInt()
                        val next100Preset = ((exactPreset + 99) / 100) * 100
                        val next500Preset = ((exactPreset + 499) / 500) * 500

                        val presets = remember(calculatedSum) {
                            listOfNotNull(
                                exactPreset,
                                if (next100Preset != exactPreset) next100Preset else null,
                                if (next500Preset != exactPreset && next500Preset != next100Preset) next500Preset else null
                            ).distinct()
                        }

                        presets.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFF8F4))
                                    .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
                                    .clickable { amountInput = preset.toString() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${preset} ₽",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen
                                )
                            }
                        }
                    }
                }

                // Warning banner
                if (enteredAmount > 0.0 && enteredAmount.toInt() != calculatedSum.toInt()) {
                    Surface(
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠ Введённая сумма отличается от чека (${calculatedSum.asRubles()})",
                            color = OnSecondaryContainerGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (isCloseEnabled) onClosePoint(paymentType, enteredAmount, whiteReturn, greyReturn, lavashReturn) },
                enabled = isCloseEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryContainerGreen,
                    disabledContainerColor = SecondaryGray.copy(alpha = 0.3f),
                    contentColor = Color.White,
                    disabledContentColor = SecondaryGray
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Подтвердить",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "ПОДТВЕРДИТЬ И ЗАКРЫТЬ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onSaveReturn(whiteReturn, greyReturn, lavashReturn)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text("Отмена", color = SecondaryGray, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = SurfaceWhite
    )
}

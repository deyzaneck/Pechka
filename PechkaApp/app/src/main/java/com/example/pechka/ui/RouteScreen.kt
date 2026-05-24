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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
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
                ),
                modifier = Modifier.border(1.dp, OutlineVariant)
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
                    onClosePoint = { paymentType, amount, loavesReturn, greyReturn, lavashReturn ->
                        viewModel.updateOrderReturns(
                            detailPoint.point.id,
                            loavesReturn,
                            greyReturn,
                            lavashReturn
                        )
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (points.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет заказов.\nВнесите количество на вкладке «Заказы».",
                    color = SecondaryGray,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
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

            Spacer(modifier = Modifier.weight(1f))

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

    Column(modifier = modifier.fillMaxSize()) {
        // ===== Pinned summary above the route list =====
        Surface(
            color = SurfaceWhite,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, OutlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "ЗАГРУЖЕНО В МАШИНУ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
                val loadedText = buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)) {
                        append("Белый: ")
                    }
                    withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryGreen)) {
                        append("${formatTrays(whiteTrays)} л.")
                    }
                    withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)) {
                        append("  |  Серый: ")
                    }
                    withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryGreen)) {
                        append("${formatTrays(greyTrays)} л.")
                    }
                    withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)) {
                        append("  |  Лаваш: ")
                    }
                    withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryGreen)) {
                        append("$totalLavash шт")
                    }
                }
                Text(
                    text = loadedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Маршрут пуст.",
                            color = SecondaryGray,
                            fontSize = 16.sp
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

            // Text block — takes all remaining space via weight(1f) — HARD FIX
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
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

                // Pieces only — SINGLE Text with AnnotatedString
                // Guaranteed single line: maxLines=1, overflow=Ellipsis, softWrap=false
                val quantityText = buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)) {
                        append("Хлеб Б: ")
                    }
                    withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryGreen)) {
                        append("$loaves шт")
                    }
                    withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)) {
                        append(" | Серый: ")
                    }
                    withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryGreen)) {
                        append("$grey шт")
                    }
                    withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)) {
                        append(" | Лаваш: ")
                    }
                    withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryGreen)) {
                        append("$lavash шт")
                    }
                }
                Text(
                    text = quantityText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )

                // Status badge
                if (isCompleted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "СДАНО",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryContainerGreen
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
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .widthIn(min = 80.dp, max = 100.dp)
                ) {
                    Text(
                        text = "ОТКРЫТЬ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.widthIn(min = 60.dp)
                ) {
                    Text(
                        text = "${point.payment?.amountPaid?.toInt() ?: 0} ₽",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
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
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

// ============================================================
// DetailDialog — Close point dialog (with returns)
// ============================================================

@Composable
fun DetailDialog(
    point: PointWithOrderAndPayment,
    prices: com.example.pechka.data.PriceEntity,
    onDismiss: () -> Unit,
    onClosePoint: (String, Double, Int, Int, Int) -> Unit
) {
    val order = point.order
    val loavesQty = order?.loavesQtyPieces ?: 0
    val greyQty = order?.greyBreadQtyPieces ?: 0
    val lavashQty = order?.lavashQtyPieces ?: 0

    var loavesReturnInput by remember(order?.loavesReturnPieces) {
        mutableStateOf((order?.loavesReturnPieces ?: 0).toString())
    }
    var greyReturnInput by remember(order?.greyReturnPieces) {
        mutableStateOf((order?.greyReturnPieces ?: 0).toString())
    }
    var lavashReturnInput by remember(order?.lavashReturnPieces) {
        mutableStateOf((order?.lavashReturnPieces ?: 0).toString())
    }

    val loavesReturn = (loavesReturnInput.toIntOrNull() ?: 0).coerceIn(0, loavesQty)
    val greyReturn = (greyReturnInput.toIntOrNull() ?: 0).coerceIn(0, greyQty)
    val lavashReturn = (lavashReturnInput.toIntOrNull() ?: 0).coerceIn(0, lavashQty)

    val calculatedSum =
        ((loavesQty - loavesReturn) * prices.whiteBreadPrice) +
        ((greyQty - greyReturn) * prices.greyBreadPrice) +
        ((lavashQty - lavashReturn) * prices.lavashPrice)

    var paymentType by remember { mutableStateOf("CASH") }
    var amountInput by remember(calculatedSum) { mutableStateOf(calculatedSum.toInt().toString()) }
    val enteredAmount = amountInput.toDoubleOrNull() ?: 0.0
    val isCloseEnabled = enteredAmount > 0.0

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
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                    // Order summary
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "ПРОВЕРКА ЗАКАЗА",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryGray
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Хлеб Б:", fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("$loavesQty шт", fontSize = 17.sp, fontWeight = FontWeight.Black, color = PrimaryGreen, maxLines = 1)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Серый:", fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("$greyQty шт", fontSize = 17.sp, fontWeight = FontWeight.Black, color = PrimaryGreen, maxLines = 1)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Лаваш:", fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("$lavashQty шт", fontSize = 17.sp, fontWeight = FontWeight.Black, color = PrimaryGreen, maxLines = 1)
                            }
                        }
                    }

                    // ====== Returns block ======
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "ВОЗВРАТ (ШТ)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = loavesReturnInput,
                                    onValueChange = { v ->
                                        if (v.isEmpty() || v.all { it.isDigit() }) loavesReturnInput = v
                                    },
                                    label = { Text("Белый", fontSize = 11.sp, maxLines = 1) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ErrorRed),
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ErrorRed,
                                        unfocusedBorderColor = OutlineGray
                                    )
                                )
                                OutlinedTextField(
                                    value = greyReturnInput,
                                    onValueChange = { v ->
                                        if (v.isEmpty() || v.all { it.isDigit() }) greyReturnInput = v
                                    },
                                    label = { Text("Серый", fontSize = 11.sp, maxLines = 1) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ErrorRed),
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ErrorRed,
                                        unfocusedBorderColor = OutlineGray
                                    )
                                )
                                OutlinedTextField(
                                    value = lavashReturnInput,
                                    onValueChange = { v ->
                                        if (v.isEmpty() || v.all { it.isDigit() }) lavashReturnInput = v
                                    },
                                    label = { Text("Лаваш", fontSize = 11.sp, maxLines = 1) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ErrorRed),
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ErrorRed,
                                        unfocusedBorderColor = OutlineGray
                                    )
                                )
                            }
                            HorizontalDivider(color = OutlineVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "К оплате:",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${calculatedSum.toInt()} ₽",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryContainerGreen,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Cash/Card toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerHigh)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (paymentType == "CASH") Color.White else Color.Transparent)
                                .clickable { paymentType = "CASH" },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "НАЛИЧНЫЕ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (paymentType == "CASH") PrimaryGreen else SecondaryGray,
                                maxLines = 1
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (paymentType == "CARD") Color.White else Color.Transparent)
                                .clickable { paymentType = "CARD" },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "БЕЗНАЛ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (paymentType == "CARD") PrimaryGreen else SecondaryGray,
                                maxLines = 1
                            )
                        }
                    }

                    // Received amount input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Получено:",
                            fontSize = 14.sp,
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
                                    fontWeight = FontWeight.Bold,
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
                                    text = "Вся\nсумма",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    // Warning
                    if (enteredAmount > 0.0 && enteredAmount.toInt() != calculatedSum.toInt()) {
                        Text(
                            text = "⚠ Сумма отличается от расчётной (${calculatedSum.toInt()} ₽)",
                            color = WarningOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2
                        )
                    }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isCloseEnabled) {
                        onClosePoint(paymentType, enteredAmount, loavesReturn, greyReturn, lavashReturn)
                    }
                },
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
                        text = "ПОДТВЕРДИТЬ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text("Отмена", color = SecondaryGray, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = SurfaceWhite
    )
}

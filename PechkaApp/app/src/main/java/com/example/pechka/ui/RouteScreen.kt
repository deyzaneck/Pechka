package com.example.pechka.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

            // Text block — takes all remaining space via weight(1f)
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

                // Pieces-only display — SINGLE Text with AnnotatedString
                // This guarantees it stays on ONE LINE and truncates with ellipsis
                val quantityText = buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)) {
                        append("Хлеб Б: ")
                    }
                    withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryGreen)) {
                        append("$loaves шт")
                    }
                    withStyle(SpanStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)) {
                        append(" | Серый: ")
                    }
                    withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryGreen)) {
                        append("$grey шт")
                    }
                    withStyle(SpanStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)) {
                        append(" | Лаваш: ")
                    }
                    withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryGreen)) {
                        append("$lavash шт")
                    }
                }
                Text(
                    text = quantityText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )

                // Status badge
                if (isCompleted) {
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

// ============================================================
// DetailDialog — Close point dialog (payment selection)
// ============================================================

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
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Large Quantities
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        HorizontalDivider(color = OutlineVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("К оплате:", fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(calculatedSum.asRubles(), fontSize = 18.sp, fontWeight = FontWeight.Black, color = PrimaryContainerGreen, maxLines = 1)
                        }
                    }
                }

                // Returns section
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ВОЗВРАТ ТОВАРА",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarningOrange
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = whiteReturnInput,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.all { it.isDigit() }) {
                                        whiteReturnInput = input
                                        val newSum = (((loavesQty - ((input.toIntOrNull() ?: 0).coerceIn(0, loavesQty))) * prices.whiteBreadPrice) +
                                                ((greyQty - greyReturn) * prices.greyBreadPrice) +
                                                ((lavashQty - lavashReturn) * prices.lavashPrice))
                                        amountInput = newSum.toInt().toString()
                                    }
                                },
                                label = { Text("Возврат Б", fontSize = 12.sp, maxLines = 1, fontWeight = FontWeight.SemiBold) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WarningOrange),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WarningOrange,
                                    unfocusedBorderColor = OutlineGray
                                )
                            )
                            OutlinedTextField(
                                value = greyReturnInput,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.all { it.isDigit() }) {
                                        greyReturnInput = input
                                        val newSum = (((loavesQty - whiteReturn) * prices.whiteBreadPrice) +
                                                ((greyQty - ((input.toIntOrNull() ?: 0).coerceIn(0, greyQty))) * prices.greyBreadPrice) +
                                                ((lavashQty - lavashReturn) * prices.lavashPrice))
                                        amountInput = newSum.toInt().toString()
                                    }
                                },
                                label = { Text("Возврат С", fontSize = 12.sp, maxLines = 1, fontWeight = FontWeight.SemiBold) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WarningOrange),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WarningOrange,
                                    unfocusedBorderColor = OutlineGray
                                )
                            )
                            OutlinedTextField(
                                value = lavashReturnInput,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.all { it.isDigit() }) {
                                        lavashReturnInput = input
                                        val newSum = (((loavesQty - whiteReturn) * prices.whiteBreadPrice) +
                                                ((greyQty - greyReturn) * prices.greyBreadPrice) +
                                                ((lavashQty - ((input.toIntOrNull() ?: 0).coerceIn(0, lavashQty))) * prices.lavashPrice))
                                        amountInput = newSum.toInt().toString()
                                    }
                                },
                                label = { Text("Возврат Л", fontSize = 12.sp, maxLines = 1, fontWeight = FontWeight.SemiBold) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WarningOrange),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WarningOrange,
                                    unfocusedBorderColor = OutlineGray
                                )
                            )
                        }
                        if (whiteReturn > 0 || greyReturn > 0 || lavashReturn > 0) {
                            Text(
                                text = "Возврат учтён в сумме к оплате",
                                fontSize = 11.sp,
                                color = WarningOrange,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Cash/Card toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceContainerHigh)
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (paymentType == "CASH") Color.White else Color.Transparent)
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
                                tint = if (paymentType == "CASH") PrimaryGreen else SecondaryGray,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "НАЛИЧНЫЕ",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = if (paymentType == "CASH") PrimaryGreen else SecondaryGray,
                                maxLines = 1
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (paymentType == "CARD") Color.White else Color.Transparent)
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
                                tint = if (paymentType == "CARD") Color(0xFF1976D2) else SecondaryGray,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "БЕЗНАЛ",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = if (paymentType == "CARD") Color(0xFF1976D2) else SecondaryGray,
                                maxLines = 1
                            )
                        }
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
                        text = "⚠ Сумма отличается от расчётной (${calculatedSum.asRubles()})",
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

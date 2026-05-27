package com.example.pechka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import com.example.pechka.R
import com.example.pechka.data.PointWithOrderAndPayment
import com.example.pechka.data.PriceEntity
import com.example.pechka.data.ExpenseItemEntity
import com.example.pechka.data.effectiveWhitePrice
import com.example.pechka.data.effectiveGreyPrice
import com.example.pechka.data.effectiveLavashPrice
import com.example.pechka.theme.*
import com.example.pechka.ui.DeliveryViewModel

// ── Color tokens ──
private val ReportAccentBread = Color(0xFF7D562D)
private val ReportAccentLavash = Color(0xFF3D627C)
private val ReportExpenseRed = Color(0xFFBA1A1A)
private val ReportSummaryBreadBg = Color(0xFFF0E0D0)
private val ReportSummaryLavashBg = Color(0xFFDAEAF5)
private val ReportDivider = Color(0xFFE9D5C3)
private val ChipBorder = Color(0xFFD4C4B7)

private data class ReportExpenseLine(
    val storeName: String,
    val category: String,
    val amount: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: DeliveryViewModel,
    onBack: () -> Unit
) {
    val points by viewModel.pointsWithOrderAndPaymentFlow.collectAsState(initial = emptyList())
    val prices by viewModel.activePrices.collectAsState()
    val expenseItems by viewModel.expenseItems.collectAsState()

    val completed = remember(points) { points.filter { it.point.status == "COMPLETED" } }

    val whiteSoldPieces = completed.sumOf {
        ((it.order?.loavesQtyPieces ?: 0) - (it.returns?.whiteReturnQty ?: 0)).coerceAtLeast(0)
    }
    val greySoldPieces = completed.sumOf {
        ((it.order?.greyBreadQtyPieces ?: 0) - (it.returns?.greyReturnQty ?: 0)).coerceAtLeast(0)
    }
    val lavashSoldPieces = completed.sumOf {
        ((it.order?.lavashQtyPieces ?: 0) - (it.returns?.lavashReturnQty ?: 0)).coerceAtLeast(0)
    }

    val whiteRevenue = completed.sumOf {
        val sold = ((it.order?.loavesQtyPieces ?: 0) - (it.returns?.whiteReturnQty ?: 0)).coerceAtLeast(0)
        sold * it.effectiveWhitePrice(prices.whiteBreadPrice)
    }
    val greyRevenue = completed.sumOf {
        val sold = ((it.order?.greyBreadQtyPieces ?: 0) - (it.returns?.greyReturnQty ?: 0)).coerceAtLeast(0)
        sold * it.effectiveGreyPrice(prices.greyBreadPrice)
    }
    val lavashRevenue = completed.sumOf {
        val sold = ((it.order?.lavashQtyPieces ?: 0) - (it.returns?.lavashReturnQty ?: 0)).coerceAtLeast(0)
        sold * it.effectiveLavashPrice(prices.lavashPrice)
    }

    val expenseLines = remember(completed, expenseItems) {
        buildReportExpenseLines(completed, expenseItems)
    }

    val totalExpenses = expenseLines.sumOf { it.amount }
    val netBreadTotal = (whiteRevenue + greyRevenue) - totalExpenses
    val lavashTotal = lavashRevenue

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth

        // Estimate required height precisely to scale down elements perfectly if they exceed screen height.
        val requiredHeight = 260f + (expenseLines.size * 20f)
        val heightScale = screenHeight.value / requiredHeight
        val widthScale = screenWidth.value / 360f
        // Scale exactly to fit all elements proportionally without scrolling
        val scale = minOf(heightScale, widthScale).coerceAtMost(1.0f)

        val sSp = { size: Float -> (size * scale).sp }
        val sDp = { size: Float -> (size * scale).dp }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(modifier = Modifier.fillMaxWidth().padding(end = sDp(48f)), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Отчёт",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = sSp(24f),
                                color = Color(0xFF221A11)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад",
                                tint = Color(0xFF221A11)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFF8F4))
                )
            },
            containerColor = Color(0xFFFFF8F4)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = sDp(12f), vertical = sDp(4f)),
                verticalArrangement = Arrangement.spacedBy(sDp(4f))
            ) {
                // ── Top chips row (fits perfectly side-by-side, no scroll, weight 1f each) ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(sDp(8f))
                ) {
                    ProductChip(icon = R.drawable.ic_bread, iconTint = ReportAccentBread, label = "Белый", quantity = whiteSoldPieces, price = prices.whiteBreadPrice.toInt(), sSp = sSp, sDp = sDp, modifier = Modifier.weight(1f))
                    ProductChip(icon = R.drawable.ic_bread, iconTint = ReportAccentBread, label = "Серый", quantity = greySoldPieces, price = prices.greyBreadPrice.toInt(), sSp = sSp, sDp = sDp, modifier = Modifier.weight(1f))
                    ProductChip(icon = R.drawable.ic_lavash, iconTint = ReportAccentBread, label = "Лаваш", quantity = lavashSoldPieces, price = prices.lavashPrice.toInt(), sSp = sSp, sDp = sDp, modifier = Modifier.weight(1f))
                }

                // ── Expenses card (fills remaining height) ──
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(sDp(10f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(sDp(1f), ChipBorder, RoundedCornerShape(sDp(10f)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top: header + expense list
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFFF8F4))
                                    .padding(horizontal = sDp(8f), vertical = sDp(4f))
                            ) {
                                Text(
                                    text = "Расходы",
                                    fontSize = sSp(16f),
                                    fontWeight = FontWeight.Bold,
                                    color = ReportAccentBread
                                )
                            }
                            HorizontalDivider(color = ReportDivider, thickness = sDp(1f))

                            if (expenseLines.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Нет расходов за смену", fontSize = sSp(14f), color = SecondaryGray)
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    expenseLines.forEachIndexed { index, line ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = sDp(8f), vertical = sDp(2f)),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = line.storeName,
                                                fontSize = sSp(13f),
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF221A11),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(sDp(8f)))
                                            Text(
                                                text = "— ${line.amount.toInt().asRubles()}",
                                                fontSize = sSp(14f),
                                                fontWeight = FontWeight.Bold,
                                                color = ReportExpenseRed
                                            )
                                        }
                                        if (index < expenseLines.lastIndex) {
                                            HorizontalDivider(color = ReportDivider, thickness = sDp(1f))
                                        }
                                    }
                                    HorizontalDivider(color = ReportDivider, thickness = sDp(1.5f))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = sDp(8f), vertical = sDp(6f)),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Итого расходов",
                                            fontSize = sSp(13f),
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF82756A)
                                        )
                                        Text(
                                            text = "— ${totalExpenses.toInt().asRubles()}",
                                            fontSize = sSp(14f),
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ReportExpenseRed
                                        )
                                    }
                                }
                            }
                        }

                        // Bottom totals
                        Column {
                             HorizontalDivider(color = ReportDivider, thickness = sDp(1f))
                             Row(
                                 modifier = Modifier
                                     .fillMaxWidth()
                                     .background(Color(0xFFFFF8F4))
                                     .padding(horizontal = sDp(16f), vertical = sDp(8f)),
                                 horizontalArrangement = Arrangement.SpaceBetween,
                                 verticalAlignment = Alignment.CenterVertically
                             ) {
                                 // Bread total (Left side)
                                 Column(
                                     horizontalAlignment = Alignment.Start,
                                     modifier = Modifier.weight(1f)
                                 ) {
                                     Row(verticalAlignment = Alignment.CenterVertically) {
                                         // Warm beige dot indicator
                                         Box(
                                             modifier = Modifier
                                                 .size(sDp(6f))
                                                 .clip(RoundedCornerShape(50))
                                                 .background(Color(0xFFBA9675))
                                         )
                                         Spacer(modifier = Modifier.width(sDp(6f)))
                                         Text(
                                             text = "ИТОГ ХЛЕБ",
                                             fontSize = sSp(11f),
                                             fontWeight = FontWeight.Bold,
                                             color = Color(0xFF82756A),
                                             letterSpacing = 0.5.sp
                                         )
                                     }
                                     Spacer(modifier = Modifier.height(sDp(2f)))
                                     val netBreadText = if (netBreadTotal < 0) {
                                         "−${(-netBreadTotal).toInt().asRubles()}"
                                     } else {
                                         netBreadTotal.toInt().asRubles()
                                     }
                                     Text(
                                         text = netBreadText,
                                         fontSize = sSp(20f),
                                         fontWeight = FontWeight.ExtraBold,
                                         color = Color(0xFF221A11),
                                         maxLines = 1
                                     )
                                 }

                                 // Golden/beige vertical thin separator line
                                 Box(
                                     modifier = Modifier
                                         .height(sDp(32f))
                                         .width(sDp(1f))
                                         .background(Color(0xFFE9D5C3))
                                 )

                                 // Lavash total (Right side)
                                 Column(
                                     horizontalAlignment = Alignment.End,
                                     modifier = Modifier.weight(1f)
                                 ) {
                                     Row(verticalAlignment = Alignment.CenterVertically) {
                                         Text(
                                             text = "ИТОГ ЛАВАШ",
                                             fontSize = sSp(11f),
                                             fontWeight = FontWeight.Bold,
                                             color = Color(0xFF82756A),
                                             letterSpacing = 0.5.sp
                                         )
                                         Spacer(modifier = Modifier.width(sDp(6f)))
                                         // Soft blue dot indicator
                                         Box(
                                             modifier = Modifier
                                                 .size(sDp(6f))
                                                 .clip(RoundedCornerShape(50))
                                                 .background(ReportAccentLavash)
                                         )
                                     }
                                     Spacer(modifier = Modifier.height(sDp(2f)))
                                     Text(
                                         text = lavashTotal.toInt().asRubles(),
                                         fontSize = sSp(20f),
                                         fontWeight = FontWeight.ExtraBold,
                                         color = ReportAccentLavash,
                                         maxLines = 1
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

// ── Product chip (Column-based badge) ──
@Composable
private fun ProductChip(
    icon: Int,
    iconTint: Color,
    label: String,
    quantity: Int,
    price: Int,
    sSp: (Float) -> androidx.compose.ui.unit.TextUnit,
    sDp: (Float) -> androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(sDp(8f)))
            .border(sDp(1f), ChipBorder, RoundedCornerShape(sDp(8f)))
            .background(Color(0xFFFBEBDB))
            .padding(horizontal = sDp(4f), vertical = sDp(2f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(sDp(14f))
            )
            Spacer(modifier = Modifier.width(sDp(4f)))
            Text(
                text = label,
                fontSize = sSp(12f),
                fontWeight = FontWeight.SemiBold,
                color = iconTint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(sDp(2f)))
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$quantity",
                fontSize = sSp(16f),
                fontWeight = FontWeight.ExtraBold,
                color = iconTint,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(sDp(2f)))
            Text(
                text = "шт",
                fontSize = sSp(11f),
                fontWeight = FontWeight.Medium,
                color = iconTint,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(sDp(4f)))
            Text(
                text = "$price ₽/шт",
                fontSize = sSp(11f),
                fontWeight = FontWeight.Medium,
                color = Color(0xFF82756A),
                maxLines = 1
            )
        }
    }
}

// ════════════════════════════════════════
// Business Logic: Build expense lines
// ════════════════════════════════════════

private fun buildReportExpenseLines(
    completedPoints: List<PointWithOrderAndPayment>,
    expenseItems: List<ExpenseItemEntity>
): List<ReportExpenseLine> {
    val lines = mutableListOf<ReportExpenseLine>()

    // 1. Manual expense items
    expenseItems.forEach { item ->
        lines.add(
            ReportExpenseLine(
                storeName = item.name,
                category = "Расход",
                amount = item.amount
            )
        )
    }

    // 2. Cash payments (store paid cash → expense because register is cashless)
    completedPoints
        .filter { it.payment?.paymentType == "CASH" && (it.payment?.amountPaid ?: 0.0) > 0.0 }
        .forEach { point ->
            lines.add(
                ReportExpenseLine(
                    storeName = point.point.name,
                    category = "Оплата наличными",
                    amount = point.payment!!.amountPaid
                )
            )
        }

    // 3. Zero-payment orders (driver selected 0 in payment)
    completedPoints
        .filter { (it.payment?.amountPaid ?: 0.0) == 0.0 && it.payment != null }
        .forEach { point ->
            val orderValue = point.order?.calculatedSum ?: 0.0
            if (orderValue > 0.0) {
                lines.add(
                    ReportExpenseLine(
                        storeName = point.point.name,
                        category = "Не оплачено",
                        amount = orderValue
                    )
                )
            }
        }

    return lines
}

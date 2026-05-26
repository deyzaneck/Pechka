package com.example.pechka.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pechka.RoundingDirection
import com.example.pechka.theme.*

// Weight ratio constants for the adaptive rounding matrix layout.
// Store name takes ~30% of width, each product column takes ~23%.
private const val STORE_COL_WEIGHT = 1.3f
private const val PRODUCT_COL_WEIGHT = 1f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundingScreen(
    viewModel: DeliveryViewModel,
    modifier: Modifier = Modifier
) {
    val loavesTraysText by viewModel.loavesTraysText.collectAsState()
    val greyTraysText by viewModel.greyTraysText.collectAsState()
    val selectedLoavesDirection by viewModel.selectedLoavesDirection.collectAsState()
    val selectedGreyDirection by viewModel.selectedGreyDirection.collectAsState()
    val roundingPoints by viewModel.roundingPoints.collectAsState()
    val calc by viewModel.roundingCalculator.collectAsState()
    val localCalc = calc

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
                            contentDescription = "Транспорт",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Загрузка",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = PrimaryGreen
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.clearAllOrders() },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Очистить",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = PrimaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite
                )
            )
        },
        bottomBar = {
            val isBalanced = calc?.isBalanced == true
            val loavesRemaining = calc?.loavesRemainingBalance ?: 0
            val greyRemaining = calc?.greyRemainingBalance ?: 0

            Surface(
                color = SurfaceWhite,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = { if (isBalanced) viewModel.confirmRounding() },
                        enabled = isBalanced,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryContainerGreen,
                            disabledContainerColor = SecondaryGray.copy(alpha = 0.3f),
                            contentColor = Color.White,
                            disabledContentColor = SecondaryGray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ПОДТВЕРДИТЬ ЗАГРУЗКУ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1
                            )

                            if (!isBalanced) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (calc?.needsRoundingLoaves == true && loavesRemaining != 0) {
                                        val text = if (loavesRemaining > 0) "+$loavesRemaining Бух" else "$loavesRemaining Бух"
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(ErrorContainerRed)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(text = text, color = OnErrorContainerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (calc?.needsRoundingGrey == true && greyRemaining != 0) {
                                        val text = if (greyRemaining > 0) "+$greyRemaining Сер" else "$greyRemaining Сер"
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(ErrorContainerRed)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(text = text, color = OnErrorContainerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
        ) {
            // ── STATUS CARD ──
            if (localCalc != null) {
                item(key = "status-card") {
                    RoundingStatusCard(
                        calc = localCalc,
                        loavesTraysText = loavesTraysText,
                        greyTraysText = greyTraysText
                    )
                }
            }

            // ── LOAVES SECTION ──
            if (calc?.needsRoundingLoaves == true) {
                // Section header
                item(key = "loaves-header") {
                    Text(
                        text = "БУХАНКИ (БЕЛЫЙ ХЛЕБ)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryGreen,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                // Alert banner for loaves
                val loavesBalanced = calc?.loavesRemainingBalance == 0 && selectedLoavesDirection != null
                if (!loavesBalanced) {
                    item(key = "loaves-alert") {
                        RoundingAlertBanner(message = "Требуется округление Буханок. Нажмите [+] или [-] у магазинов.")
                    }
                }

                // Direction buttons for loaves
                item(key = "loaves-direction") {
                    RoundingDirectionButtons(
                        selectedDirection = selectedLoavesDirection,
                        onSelectDirection = { viewModel.selectLoavesRoundingDirection(it) }
                    )
                }

                // Matrix table for loaves
                if (selectedLoavesDirection != null) {
                    item(key = "loaves-matrix-header") {
                        RoundingMatrixHeader(productLabel = "Бух.")
                    }

                    items(roundingPoints, key = { "loaves-row-${it.pointId}" }) { point ->
                        val offset = point.currentLoavesQty - point.initialLoavesQty
                        RoundingMatrixRow(
                            storeName = point.pointName,
                            value = point.currentLoavesQty,
                            offset = offset,
                            onDecrement = { viewModel.decrementRoundingLoaves(point.pointId) },
                            onIncrement = { viewModel.incrementRoundingLoaves(point.pointId) },
                            enabled = true
                        )
                    }
                }
            }

            // ── GREY BREAD SECTION ──
            if (calc?.needsRoundingGrey == true) {
                // Divider
                if (calc?.needsRoundingLoaves == true) {
                    item(key = "grey-divider") {
                        HorizontalDivider(
                            color = OutlineVariant,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                // Section header
                item(key = "grey-header") {
                    Text(
                        text = "СЕРЫЙ ХЛЕБ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryGreen,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                // Alert banner for grey
                val greyBalanced = calc?.greyRemainingBalance == 0 && selectedGreyDirection != null
                if (!greyBalanced) {
                    item(key = "grey-alert") {
                        RoundingAlertBanner(message = "Требуется округление Серого хлеба. Нажмите [+] или [-] у магазинов.")
                    }
                }

                // Direction buttons for grey
                item(key = "grey-direction") {
                    RoundingDirectionButtons(
                        selectedDirection = selectedGreyDirection,
                        onSelectDirection = { viewModel.selectGreyRoundingDirection(it) }
                    )
                }

                // Matrix table for grey
                if (selectedGreyDirection != null) {
                    item(key = "grey-matrix-header") {
                        RoundingMatrixHeader(productLabel = "Сер.")
                    }

                    items(roundingPoints, key = { "grey-row-${it.pointId}" }) { point ->
                        val offset = point.currentGreyQty - point.initialGreyQty
                        RoundingMatrixRow(
                            storeName = point.pointName,
                            value = point.currentGreyQty,
                            offset = offset,
                            onDecrement = { viewModel.decrementRoundingGrey(point.pointId) },
                            onIncrement = { viewModel.incrementRoundingGrey(point.pointId) },
                            enabled = true
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Status Card — unified summary with balance badges
// ═══════════════════════════════════════════════════════════

@Composable
fun RoundingStatusCard(
    calc: com.example.pechka.CrateRoundingCalculator,
    loavesTraysText: String,
    greyTraysText: String
) {
    val loavesRemaining = calc.loavesRemainingBalance
    val greyRemaining = calc.greyRemainingBalance
    val isBalanced = calc.isBalanced

    Surface(
        color = SurfaceContainerLow,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineVariant, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title row with badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "ЗАГРУЗКА",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGray,
                        letterSpacing = 1.sp
                    )
                }

                // Balance badges
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (calc.needsRoundingLoaves) {
                        val text = when {
                            loavesRemaining > 0 -> "+$loavesRemaining Бух"
                            loavesRemaining < 0 -> "$loavesRemaining Бух"
                            else -> "0 Бух"
                        }
                        val bgColor = if (loavesRemaining == 0)
                            PrimaryGreen.copy(alpha = 0.15f)
                        else
                            ErrorContainerRed
                        val textColor = if (loavesRemaining == 0) PrimaryGreen else OnErrorContainerRed
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(bgColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = text,
                                color = textColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (calc.needsRoundingGrey) {
                        val text = when {
                            greyRemaining > 0 -> "+$greyRemaining Сер"
                            greyRemaining < 0 -> "$greyRemaining Сер"
                            else -> "0 Сер"
                        }
                        val bgColor = if (greyRemaining == 0)
                            PrimaryGreen.copy(alpha = 0.15f)
                        else
                            SurfaceContainerHigh
                        val textColor = if (greyRemaining == 0) PrimaryGreen else SecondaryGray
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(bgColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = text,
                                color = textColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Totals row: pieces left / trays right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Total pieces
                Row(verticalAlignment = Alignment.Bottom) {
                    val totalPieces = calc.totalCurrentLoaves + calc.totalCurrentGrey
                    Text(
                        text = "$totalPieces",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryGreen,
                        lineHeight = 36.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "шт",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = SecondaryGray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Total trays
                Row(verticalAlignment = Alignment.Bottom) {
                    val totalTrays = calc.totalCurrentLoaves / 16.0 + calc.totalCurrentGrey / 16.0
                    val traysFormatted = if (totalTrays % 1.0 == 0.0) {
                        totalTrays.toInt().toString()
                    } else {
                        String.format(java.util.Locale.US, "%.4f", totalTrays)
                    }
                    Text(
                        text = traysFormatted,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "лотков",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = SecondaryGray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            // Warning message
            if (!isBalanced) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = SecondaryContainerGray.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SecondaryContainerGray, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = OnSecondaryContainerGray,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (calc.needsRoundingLoaves && calc.needsRoundingGrey)
                                "Требуется округление Буханок и Серого хлеба."
                            else if (calc.needsRoundingLoaves)
                                "Требуется округление Буханок. Нажмите [+] или [-] у магазинов."
                            else
                                "Требуется округление Серого хлеба. Нажмите [+] или [-] у магазинов.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnSecondaryContainerGray,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Alert Banner
// ═══════════════════════════════════════════════════════════

@Composable
fun RoundingAlertBanner(message: String) {
    Surface(
        color = SecondaryContainerGray.copy(alpha = 0.4f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WarningOrange.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Внимание",
                tint = OnSecondaryContainerGray,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = message,
                color = OnSecondaryContainerGray,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                lineHeight = 16.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Direction Buttons (В БОЛЬШУЮ / В МЕНЬШУЮ)
// ═══════════════════════════════════════════════════════════

@Composable
fun RoundingDirectionButtons(
    selectedDirection: RoundingDirection?,
    onSelectDirection: (RoundingDirection) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val isUpSelected = selectedDirection == RoundingDirection.UP
        val isDownSelected = selectedDirection == RoundingDirection.DOWN

        // UP button
        Box(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isUpSelected) PrimaryContainerGreen else SurfaceWhite)
                .border(
                    width = if (isUpSelected) 2.dp else 1.dp,
                    color = if (isUpSelected) PrimaryContainerGreen else OutlineVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onSelectDirection(RoundingDirection.UP) },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Вверх",
                    tint = if (isUpSelected) Color.White else PrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "В БОЛЬШУЮ",
                    color = if (isUpSelected) Color.White else PrimaryGreen,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }
        }

        // DOWN button
        Box(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDownSelected) PrimaryContainerGreen else SurfaceWhite)
                .border(
                    width = if (isDownSelected) 2.dp else 1.dp,
                    color = if (isDownSelected) PrimaryContainerGreen else OutlineVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onSelectDirection(RoundingDirection.DOWN) },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Вниз",
                    tint = if (isDownSelected) Color.White else PrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "В МЕНЬШУЮ",
                    color = if (isDownSelected) Color.White else PrimaryGreen,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Matrix Table Header
// ═══════════════════════════════════════════════════════════

@Composable
fun RoundingMatrixHeader(productLabel: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            .background(SurfaceContainer)
            .border(0.5.dp, OutlineVariant, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
    ) {
        // Store column header
        Box(
            modifier = Modifier
                .weight(STORE_COL_WEIGHT)
                .border(0.5.dp, OutlineVariant)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Магазин",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SecondaryGray,
                maxLines = 1
            )
        }

        // Product column header
        Box(
            modifier = Modifier
                .weight(PRODUCT_COL_WEIGHT * 3) // take the remaining 3-column-equivalent space
                .border(0.5.dp, OutlineVariant)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = productLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SecondaryGray,
                maxLines = 1
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Matrix Table Row — inline stepper
// ═══════════════════════════════════════════════════════════

@Composable
fun RoundingMatrixRow(
    storeName: String,
    value: Int,
    offset: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite)
            .border(0.5.dp, OutlineVariant)
    ) {
        // Store name column
        Box(
            modifier = Modifier
                .weight(STORE_COL_WEIGHT)
                .heightIn(min = 52.dp)
                .border(0.5.dp, OutlineVariant)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = storeName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Show delta badge if edited
                if (offset != 0) {
                    val offsetText = if (offset > 0) "+$offset" else "$offset"
                    Text(
                        text = offsetText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (offset > 0) PrimaryGreen else ErrorRed
                    )
                }
            }
        }

        // Stepper cell (takes the remaining space)
        Box(
            modifier = Modifier
                .weight(PRODUCT_COL_WEIGHT * 3)
                .heightIn(min = 52.dp)
                .border(0.5.dp, OutlineVariant)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BackgroundGray)
                    .border(
                        1.dp,
                        if (offset != 0) SecondaryContainerGray else OutlineVariant,
                        RoundedCornerShape(8.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Minus button
                IconButton(
                    onClick = onDecrement,
                    enabled = enabled && value > 0,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceContainerHigh)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Уменьшить",
                        tint = if (value > 0) PrimaryGreen else SecondaryGray.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Value
                Text(
                    text = value.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (offset != 0) PrimaryGreen else SecondaryGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                // Plus button
                IconButton(
                    onClick = onIncrement,
                    enabled = enabled,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(PrimaryContainerGreen)
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
    }
}

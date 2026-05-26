package com.example.pechka.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.LocalShipping
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

    val needsLoaves = calc?.needsRoundingLoaves == true
    val needsGrey = calc?.needsRoundingGrey == true
    val needsBoth = needsLoaves && needsGrey

    // Determine if directions are selected (to show the table)
    val loavesDirReady = !needsLoaves || selectedLoavesDirection != null
    val greyDirReady = !needsGrey || selectedGreyDirection != null
    val showTable = (needsLoaves && selectedLoavesDirection != null) ||
            (needsGrey && selectedGreyDirection != null)

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
                        .padding(horizontal = 16.dp, vertical = 8.dp)
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
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ПОДТВЕРДИТЬ ЗАГРУЗКУ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )

                            if (!isBalanced) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (calc?.needsRoundingLoaves == true && loavesRemaining != 0) {
                                        val text = if (loavesRemaining > 0) "+$loavesRemaining Бух" else "$loavesRemaining Бух"
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(ErrorContainerRed)
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = text,
                                                color = OnErrorContainerRed,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                    if (calc?.needsRoundingGrey == true && greyRemaining != 0) {
                                        val text = if (greyRemaining > 0) "+$greyRemaining Сер" else "$greyRemaining Сер"
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(ErrorContainerRed)
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = text,
                                                color = OnErrorContainerRed,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                softWrap = false
                                            )
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

            // ── DIRECTION BUTTONS ──
            // Loaves direction (if needs rounding)
            if (needsLoaves) {
                item(key = "loaves-direction-header") {
                    Text(
                        text = if (needsBoth) "БУХАНКИ (БЕЛЫЙ ХЛЕБ)" else "ОКРУГЛЕНИЕ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryGreen,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                item(key = "loaves-direction") {
                    val loavesInitial = localCalc?.totalInitialLoaves ?: 0
                    val loavesRem = loavesInitial % 16
                    val loavesUpHint = if (loavesRem > 0) {
                        val upVal = 16 - loavesRem
                        val downVal = loavesRem
                        if (upVal < downVal) "выгоднее: +$upVal шт." else null
                    } else null
                    val loavesDownHint = if (loavesRem > 0) {
                        val upVal = 16 - loavesRem
                        val downVal = loavesRem
                        if (downVal < upVal) "выгоднее: -$downVal шт." else null
                    } else null

                    RoundingDirectionButtons(
                        selectedDirection = selectedLoavesDirection,
                        onSelectDirection = { viewModel.selectLoavesRoundingDirection(it) },
                        upHint = loavesUpHint,
                        downHint = loavesDownHint
                    )
                }
            }

            // Grey direction (if needs rounding)
            if (needsGrey) {
                item(key = "grey-direction-header") {
                    Text(
                        text = if (needsBoth) "СЕРЫЙ ХЛЕБ" else "ОКРУГЛЕНИЕ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryGreen,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                item(key = "grey-direction") {
                    val greyInitial = localCalc?.totalInitialGrey ?: 0
                    val greyRem = greyInitial % 16
                    val greyUpHint = if (greyRem > 0) {
                        val upVal = 16 - greyRem
                        val downVal = greyRem
                        if (upVal < downVal) "выгоднее: +$upVal шт." else null
                    } else null
                    val greyDownHint = if (greyRem > 0) {
                        val upVal = 16 - greyRem
                        val downVal = greyRem
                        if (downVal < upVal) "выгоднее: -$downVal шт." else null
                    } else null

                    RoundingDirectionButtons(
                        selectedDirection = selectedGreyDirection,
                        onSelectDirection = { viewModel.selectGreyRoundingDirection(it) },
                        upHint = greyUpHint,
                        downHint = greyDownHint
                    )
                }
            }

            // ── UNIFIED MATRIX TABLE ──
            if (showTable) {
                item(key = "matrix-header") {
                    UnifiedRoundingMatrixHeader(
                        showLoaves = needsLoaves && selectedLoavesDirection != null,
                        showGrey = needsGrey && selectedGreyDirection != null
                    )
                }

                items(roundingPoints, key = { "row-${it.pointId}" }) { point ->
                    val loavesOffset = point.currentLoavesQty - point.initialLoavesQty
                    val greyOffset = point.currentGreyQty - point.initialGreyQty

                    UnifiedRoundingMatrixRow(
                        storeName = point.pointName,
                        showLoaves = needsLoaves && selectedLoavesDirection != null,
                        loavesValue = point.currentLoavesQty,
                        loavesOffset = loavesOffset,
                        onLoavesDecrement = { viewModel.decrementRoundingLoaves(point.pointId) },
                        onLoavesIncrement = { viewModel.incrementRoundingLoaves(point.pointId) },
                        showGrey = needsGrey && selectedGreyDirection != null,
                        greyValue = point.currentGreyQty,
                        greyOffset = greyOffset,
                        onGreyDecrement = { viewModel.decrementRoundingGrey(point.pointId) },
                        onGreyIncrement = { viewModel.incrementRoundingGrey(point.pointId) }
                    )
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
// Direction Buttons (В БОЛЬШУЮ / В МЕНЬШУЮ)
// ═══════════════════════════════════════════════════════════

@Composable
fun RoundingDirectionButtons(
    selectedDirection: RoundingDirection?,
    onSelectDirection: (RoundingDirection) -> Unit,
    upHint: String? = null,
    downHint: String? = null
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
                .height(52.dp)
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Вверх",
                        tint = if (isUpSelected) Color.White else PrimaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "В БОЛЬШУЮ",
                        color = if (isUpSelected) Color.White else PrimaryGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                }
                if (upHint != null) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = upHint,
                        color = if (isUpSelected) Color.White.copy(alpha = 0.9f) else PrimaryGreen.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp
                    )
                }
            }
        }

        // DOWN button
        Box(
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Вниз",
                        tint = if (isDownSelected) Color.White else PrimaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "В МЕНЬШУЮ",
                        color = if (isDownSelected) Color.White else PrimaryGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                }
                if (downHint != null) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = downHint,
                        color = if (isDownSelected) Color.White.copy(alpha = 0.9f) else PrimaryGreen.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Unified Matrix Table Header — adapts columns to products
// ═══════════════════════════════════════════════════════════

@Composable
fun UnifiedRoundingMatrixHeader(
    showLoaves: Boolean,
    showGrey: Boolean
) {
    val productCount = (if (showLoaves) 1 else 0) + (if (showGrey) 1 else 0)

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

        // Product column headers — each gets proportional space
        if (showLoaves) {
            Box(
                modifier = Modifier
                    .weight(if (productCount == 1) PRODUCT_COL_WEIGHT * 3 else PRODUCT_COL_WEIGHT * 1.5f)
                    .border(0.5.dp, OutlineVariant)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Бух.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = SecondaryGray,
                    maxLines = 1
                )
            }
        }

        if (showGrey) {
            Box(
                modifier = Modifier
                    .weight(if (productCount == 1) PRODUCT_COL_WEIGHT * 3 else PRODUCT_COL_WEIGHT * 1.5f)
                    .border(0.5.dp, OutlineVariant)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Сер.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = SecondaryGray,
                    maxLines = 1
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Unified Matrix Table Row — adaptive stepper columns
// ═══════════════════════════════════════════════════════════

@Composable
fun UnifiedRoundingMatrixRow(
    storeName: String,
    showLoaves: Boolean,
    loavesValue: Int,
    loavesOffset: Int,
    onLoavesDecrement: () -> Unit,
    onLoavesIncrement: () -> Unit,
    showGrey: Boolean,
    greyValue: Int,
    greyOffset: Int,
    onGreyDecrement: () -> Unit,
    onGreyIncrement: () -> Unit
) {
    val productCount = (if (showLoaves) 1 else 0) + (if (showGrey) 1 else 0)
    val stepperWeight = if (productCount == 1) PRODUCT_COL_WEIGHT * 3 else PRODUCT_COL_WEIGHT * 1.5f

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
                    fontSize = if (productCount > 1) 11.sp else 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Show combined delta badges
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (showLoaves && loavesOffset != 0) {
                        val offsetText = if (loavesOffset > 0) "+$loavesOffset" else "$loavesOffset"
                        Text(
                            text = offsetText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (loavesOffset > 0) PrimaryGreen else ErrorRed
                        )
                    }
                    if (showGrey && greyOffset != 0) {
                        val offsetText = if (greyOffset > 0) "+$greyOffset" else "$greyOffset"
                        Text(
                            text = if (showLoaves && loavesOffset != 0) "| $offsetText" else offsetText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (greyOffset > 0) PrimaryGreen else ErrorRed
                        )
                    }
                }
            }
        }

        // Loaves stepper cell
        if (showLoaves) {
            CompactStepperCell(
                value = loavesValue,
                offset = loavesOffset,
                onDecrement = onLoavesDecrement,
                onIncrement = onLoavesIncrement,
                isCompact = productCount > 1,
                modifier = Modifier.weight(stepperWeight)
            )
        }

        // Grey stepper cell
        if (showGrey) {
            CompactStepperCell(
                value = greyValue,
                offset = greyOffset,
                onDecrement = onGreyDecrement,
                onIncrement = onGreyIncrement,
                isCompact = productCount > 1,
                modifier = Modifier.weight(stepperWeight)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Compact Stepper Cell — adapts layout based on column count
// Compact (2 cols): vertical stack [+] value [-] for readability
// Normal (1 col):  horizontal [-] value [+]
// ═══════════════════════════════════════════════════════════

@Composable
fun CompactStepperCell(
    value: Int,
    offset: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    val highlightColor = if (offset != 0) PrimaryContainerGreen.copy(alpha = 0.12f) else BackgroundGray
    val borderColor = if (offset != 0) PrimaryGreen.copy(alpha = 0.35f) else OutlineVariant

    Box(
        modifier = modifier
            .heightIn(min = 56.dp)
            .border(0.5.dp, OutlineVariant),
        contentAlignment = Alignment.Center
    ) {
        if (isCompact) {
            // ── VERTICAL LAYOUT for compact (2-column) mode ──
            // [+] on top, big value in center, [-] on bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 3.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(highlightColor)
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Plus button (top)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryContainerGreen)
                        .clickable { onIncrement() }
                        .padding(vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 15.sp
                    )
                }

                // Value (center) — big and readable
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value.toString(),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (offset != 0) PrimaryGreen else SecondaryGray,
                        textAlign = TextAlign.Center
                    )
                }

                // Minus button (bottom)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (value > 0) SurfaceContainerHigh
                            else SurfaceContainerHigh.copy(alpha = 0.4f)
                        )
                        .clickable(enabled = value > 0) { onDecrement() }
                        .padding(vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "−",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (value > 0) PrimaryGreen else SecondaryGray.copy(alpha = 0.35f),
                        lineHeight = 15.sp
                    )
                }
            }
        } else {
            // ── HORIZONTAL LAYOUT for normal (1-column) mode ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(highlightColor)
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Minus button
                Box(
                    modifier = Modifier
                        .width(38.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                        .background(SurfaceContainerHigh)
                        .clickable(enabled = value > 0) { onDecrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "−",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (value > 0) PrimaryGreen else SecondaryGray.copy(alpha = 0.35f),
                        lineHeight = 18.sp
                    )
                }

                // Value
                Text(
                    text = value.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (offset != 0) PrimaryGreen else SecondaryGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                // Plus button
                Box(
                    modifier = Modifier
                        .width(38.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                        .background(PrimaryContainerGreen)
                        .clickable { onIncrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

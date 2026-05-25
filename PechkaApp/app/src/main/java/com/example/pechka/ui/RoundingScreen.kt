package com.example.pechka.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pechka.RoundingDirection
import com.example.pechka.theme.*

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

    var expandedLoavesPointId by remember { mutableStateOf<Long?>(null) }
    var expandedGreyPointId by remember { mutableStateOf<Long?>(null) }

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
                            fontSize = 16.sp
                        )
                        
                        if (!isBalanced) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (calc?.needsRoundingLoaves == true && loavesRemaining != 0) {
                                    val text = if (loavesRemaining > 0) "+$loavesRemaining Бух" else "$loavesRemaining Бух"
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
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
                                            .clip(CircleShape)
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            // ==================== LOAVES (БУХАНКИ) SECTION ====================
            if (calc?.needsRoundingLoaves == true) {
                item {
                    Text(
                        text = "БУХАНКИ (БЕЛЫЙ ХЛЕБ)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryGreen,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Summary Block Loaves
                item {
                    SummaryCard(title = "БУХАНКИ", text = loavesTraysText)
                }

                // Alert Banner Loaves
                val loavesBalanced = calc?.loavesRemainingBalance == 0 && selectedLoavesDirection != null
                if (!loavesBalanced) {
                    item { AlertBanner(message = "Требуется округление Буханок") }
                }

                // Direction Buttons Loaves
                item {
                    DirectionButtons(
                        selectedDirection = selectedLoavesDirection,
                        onSelectDirection = { viewModel.selectLoavesRoundingDirection(it) }
                    )
                }

                // Steppers Loaves
                if (selectedLoavesDirection != null) {
                    items(roundingPoints, key = { "loaves-${it.pointId}" }) { point ->
                        val firstUnedited = remember(roundingPoints) { roundingPoints.find { it.currentLoavesQty == it.initialLoavesQty } }
                        val activeExpandedId = expandedLoavesPointId ?: firstUnedited?.pointId
                        val isExpanded = activeExpandedId == point.pointId
                        val isEdited = point.currentLoavesQty != point.initialLoavesQty

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .border(1.dp, if (isExpanded) PrimaryGreen else OutlineVariant, RoundedCornerShape(8.dp))
                        ) {
                            if (isExpanded) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = point.pointName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = PrimaryGreen,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            IconButton(
                                                onClick = { viewModel.decrementRoundingLoaves(point.pointId) },
                                                enabled = point.currentLoavesQty > 0,
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .background(SurfaceContainer, RoundedCornerShape(8.dp))
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Remove,
                                                    contentDescription = "Уменьшить",
                                                    tint = if (point.currentLoavesQty > 0) PrimaryGreen else SecondaryGray.copy(alpha = 0.5f)
                                                )
                                            }

                                            Box(
                                                modifier = Modifier.width(40.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = point.currentLoavesQty.toString(),
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryGreen
                                                )
                                            }

                                            IconButton(
                                                onClick = { viewModel.incrementRoundingLoaves(point.pointId) },
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .background(PrimaryContainerGreen, RoundedCornerShape(8.dp))
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Увеличить",
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            val currentIndex = roundingPoints.indexOfFirst { it.pointId == point.pointId }
                                            var nextIndex = currentIndex + 1
                                            var foundNextId: Long? = null
                                            while (nextIndex < roundingPoints.size) {
                                                val p = roundingPoints[nextIndex]
                                                if (p.currentLoavesQty == p.initialLoavesQty) {
                                                    foundNextId = p.pointId
                                                    break
                                                }
                                                nextIndex++
                                            }
                                            if (foundNextId == null) {
                                                val wrapPoint = roundingPoints.find { it.currentLoavesQty == it.initialLoavesQty && it.pointId != point.pointId }
                                                foundNextId = wrapPoint?.pointId
                                            }
                                            expandedLoavesPointId = foundNextId ?: -1L
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainerGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("ГОТОВО", fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedLoavesPointId = point.pointId }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = point.pointName,
                                        fontSize = 15.sp,
                                        color = PrimaryGreen,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Show ABSOLUTE qty (same as stepper shows)
                                        Text(
                                            text = "${point.currentLoavesQty} шт",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryGreen
                                        )

                                        // Show delta badge only if edited
                                        val offset = point.currentLoavesQty - point.initialLoavesQty
                                        if (offset != 0) {
                                            val offsetText = if (offset > 0) "+$offset" else "$offset"
                                            val offsetColor = if (offset > 0) PrimaryContainerGreen else ErrorRed
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(offsetColor.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = offsetText,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = offsetColor
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

            // ==================== GREY BREAD (СЕРЫЙ ХЛЕБ) SECTION ====================
            if (calc?.needsRoundingGrey == true) {
                item {
                    Divider(color = OutlineVariant, thickness = 1.dp)
                }

                item {
                    Text(
                        text = "СЕРЫЙ ХЛЕБ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryGreen,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Summary Block Grey
                item {
                    SummaryCard(title = "СЕРЫЙ ХЛЕБ", text = greyTraysText)
                }

                // Alert Banner Grey
                val greyBalanced = calc?.greyRemainingBalance == 0 && selectedGreyDirection != null
                if (!greyBalanced) {
                    item { AlertBanner(message = "Требуется округление Серого хлеба") }
                }

                // Direction Buttons Grey
                item {
                    DirectionButtons(
                        selectedDirection = selectedGreyDirection,
                        onSelectDirection = { viewModel.selectGreyRoundingDirection(it) }
                    )
                }

                // Steppers Grey
                if (selectedGreyDirection != null) {
                    items(roundingPoints, key = { "grey-${it.pointId}" }) { point ->
                        val firstUnedited = remember(roundingPoints) { roundingPoints.find { it.currentGreyQty == it.initialGreyQty } }
                        val activeExpandedId = expandedGreyPointId ?: firstUnedited?.pointId
                        val isExpanded = activeExpandedId == point.pointId
                        val isEdited = point.currentGreyQty != point.initialGreyQty

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .border(1.dp, if (isExpanded) PrimaryGreen else OutlineVariant, RoundedCornerShape(8.dp))
                        ) {
                            if (isExpanded) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = point.pointName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = PrimaryGreen,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            IconButton(
                                                onClick = { viewModel.decrementRoundingGrey(point.pointId) },
                                                enabled = point.currentGreyQty > 0,
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .background(SurfaceContainer, RoundedCornerShape(8.dp))
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Remove,
                                                    contentDescription = "Уменьшить",
                                                    tint = if (point.currentGreyQty > 0) PrimaryGreen else SecondaryGray.copy(alpha = 0.5f)
                                                )
                                            }

                                            Box(
                                                modifier = Modifier.width(40.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = point.currentGreyQty.toString(),
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryGreen
                                                )
                                            }

                                            IconButton(
                                                onClick = { viewModel.incrementRoundingGrey(point.pointId) },
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .background(PrimaryContainerGreen, RoundedCornerShape(8.dp))
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Увеличить",
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            val currentIndex = roundingPoints.indexOfFirst { it.pointId == point.pointId }
                                            var nextIndex = currentIndex + 1
                                            var foundNextId: Long? = null
                                            while (nextIndex < roundingPoints.size) {
                                                val p = roundingPoints[nextIndex]
                                                if (p.currentGreyQty == p.initialGreyQty) {
                                                    foundNextId = p.pointId
                                                    break
                                                }
                                                nextIndex++
                                            }
                                            if (foundNextId == null) {
                                                val wrapPoint = roundingPoints.find { it.currentGreyQty == it.initialGreyQty && it.pointId != point.pointId }
                                                foundNextId = wrapPoint?.pointId
                                            }
                                            expandedGreyPointId = foundNextId ?: -1L
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainerGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("ГОТОВО", fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedGreyPointId = point.pointId }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = point.pointName,
                                        fontSize = 15.sp,
                                        color = PrimaryGreen,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Show ABSOLUTE qty (same as stepper shows)
                                        Text(
                                            text = "${point.currentGreyQty} шт",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryGreen
                                        )

                                        // Show delta badge only if edited
                                        val offset = point.currentGreyQty - point.initialGreyQty
                                        if (offset != 0) {
                                            val offsetText = if (offset > 0) "+$offset" else "$offset"
                                            val offsetColor = if (offset > 0) PrimaryContainerGreen else ErrorRed
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(offsetColor.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = offsetText,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = offsetColor
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
        }
    }
}

@Composable
fun SummaryCard(title: String, text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineVariant, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SecondaryGray
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val parts = text.split(" / ")
                val pcs = parts.getOrNull(0)?.replace(" шт", "") ?: "0"
                val trays = parts.getOrNull(1)?.replace(" лотков", "") ?: "0.0"
                
                Text(
                    text = pcs,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryGreen,
                    lineHeight = 44.sp
                )
                Text(
                    text = "шт",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryGray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "/",
                    fontSize = 24.sp,
                    color = OutlineGray,
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 4.dp)
                )
                Text(
                    text = trays,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "лотков",
                    fontSize = 14.sp,
                    color = SecondaryGray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun AlertBanner(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SecondaryContainerGray),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WarningOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Внимание",
                tint = OnSecondaryContainerGray,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = message,
                color = OnSecondaryContainerGray,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun DirectionButtons(
    selectedDirection: RoundingDirection?,
    onSelectDirection: (RoundingDirection) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val isUpSelected = selectedDirection == RoundingDirection.UP
        val isDownSelected = selectedDirection == RoundingDirection.DOWN

        Box(
            modifier = Modifier
                .weight(1f)
                .height(88.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isUpSelected) PrimaryContainerGreen else Color.Transparent)
                .border(2.dp, if (isUpSelected) PrimaryContainerGreen else OutlineGray, RoundedCornerShape(12.dp))
                .clickable { onSelectDirection(RoundingDirection.UP) }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Вверх",
                    tint = if (isUpSelected) Color.White else PrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "В БОЛЬШУЮ",
                    color = if (isUpSelected) Color.White else PrimaryGreen,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
                Text(
                    text = "СТОРОНУ",
                    color = if (isUpSelected) Color.White else PrimaryGreen,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(88.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDownSelected) PrimaryContainerGreen else Color.Transparent)
                .border(2.dp, if (isDownSelected) PrimaryContainerGreen else OutlineGray, RoundedCornerShape(12.dp))
                .clickable { onSelectDirection(RoundingDirection.DOWN) }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Вниз",
                    tint = if (isDownSelected) Color.White else PrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "В МЕНЬШУЮ",
                    color = if (isDownSelected) Color.White else PrimaryGreen,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
                Text(
                    text = "СТОРОНУ",
                    color = if (isDownSelected) Color.White else PrimaryGreen,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun RoundingStepperRow(
    name: String,
    qty: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(SurfaceWhite, RoundedCornerShape(8.dp))
            .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = PrimaryGreen,
            modifier = Modifier.weight(1f)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onDecrement,
                enabled = qty > 0,
                modifier = Modifier
                    .size(56.dp)
                    .background(SurfaceContainer, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Уменьшить",
                    tint = if (qty > 0) PrimaryGreen else SecondaryGray.copy(alpha = 0.5f)
                )
            }
            
            Box(
                modifier = Modifier.width(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = qty.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }
            
            IconButton(
                onClick = onIncrement,
                modifier = Modifier
                    .size(56.dp)
                    .background(PrimaryContainerGreen, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Увеличить",
                    tint = Color.White
                )
            }
        }
    }
}

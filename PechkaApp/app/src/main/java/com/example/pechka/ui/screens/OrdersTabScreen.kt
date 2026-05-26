package com.example.pechka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flatware
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pechka.ui.DeliveryViewModel
import com.example.pechka.theme.*

@Composable
fun OrdersTabScreen(viewModel: DeliveryViewModel) {
    val points by viewModel.pointsWithOrderAndPaymentFlow.collectAsState(initial = emptyList())

    // Compute fill progress: count non-zero cells out of total cells (points × 3 products)
    val totalCells = points.size * 3
    val filledCells = points.sumOf { item ->
        val loaves = item.order?.loavesQtyPieces ?: 0
        val grey = item.order?.greyBreadQtyPieces ?: 0
        val lavash = item.order?.lavashQtyPieces ?: 0
        (if (loaves > 0) 1 else 0) + (if (grey > 0) 1 else 0) + (if (lavash > 0) 1 else 0)
    }
    val totalPieces = points.sumOf { item ->
        (item.order?.loavesQtyPieces ?: 0) +
        (item.order?.greyBreadQtyPieces ?: 0) +
        (item.order?.lavashQtyPieces ?: 0)
    }
    val progressFraction = if (totalCells > 0) filledCells.toFloat() / totalCells else 0f

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top App Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(BackgroundGray)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left spacer (balance with right button)
                Spacer(modifier = Modifier.width(72.dp))

                Text(
                    text = "Заказы",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryGray,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                TextButton(
                    onClick = { viewModel.clearAllOrders() },
                    modifier = Modifier.width(72.dp)
                ) {
                    Text(
                        text = "Очистить",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = PrimaryGreen
                    )
                }
            }

            // ── Progress Indicator ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundGray)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Заполнение",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SecondaryGray
                    )
                    Text(
                        text = "$totalPieces шт.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SecondaryContainerGray,
                    trackColor = SurfaceContainerHighest,
                )
            }

            // ── Matrix Grid ──
            val horizontalScrollState = rememberScrollState()
            val storeColumnWidth = 120.dp
            val cellWidth = 90.dp
            val cellHeight = 48.dp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 72.dp) // space for floating button
            ) {
                // ── Table Header Row ──
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Sticky corner: "Магазин"
                    Box(
                        modifier = Modifier
                            .width(storeColumnWidth)
                            .background(SurfaceContainer)
                            .border(
                                width = 0.5.dp,
                                color = OutlineVariant,
                                shape = RoundedCornerShape(0.dp)
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(
                            text = "Магазин",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = SecondaryGray
                        )
                    }
                    // Scrollable product headers
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        // Белый хлеб header
                        Box(
                            modifier = Modifier
                                .width(cellWidth)
                                .background(SurfaceContainer)
                                .border(
                                    width = 0.5.dp,
                                    color = OutlineVariant,
                                    shape = RoundedCornerShape(0.dp)
                                )
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.BakeryDining,
                                    contentDescription = "Белый хлеб",
                                    tint = SecondaryGray.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Белый",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SecondaryGray
                                )
                            }
                        }
                        // Серый хлеб header
                        Box(
                            modifier = Modifier
                                .width(cellWidth)
                                .background(SurfaceContainer)
                                .border(
                                    width = 0.5.dp,
                                    color = OutlineVariant,
                                    shape = RoundedCornerShape(0.dp)
                                )
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.BreakfastDining,
                                    contentDescription = "Серый хлеб",
                                    tint = SecondaryGray.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Серый",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SecondaryGray
                                )
                            }
                        }
                        // Лаваш header
                        Box(
                            modifier = Modifier
                                .width(cellWidth)
                                .background(SurfaceContainer)
                                .border(
                                    width = 0.5.dp,
                                    color = OutlineVariant,
                                    shape = RoundedCornerShape(0.dp)
                                )
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Flatware,
                                    contentDescription = "Лаваш",
                                    tint = SecondaryGray.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Лаваш",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SecondaryGray
                                )
                            }
                        }
                    }
                }

                // ── Data Rows ──
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(points, key = { it.point.id }) { item ->
                        val loaves = item.order?.loavesQtyPieces ?: 0
                        val grey = item.order?.greyBreadQtyPieces ?: 0
                        val lavash = item.order?.lavashQtyPieces ?: 0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 0.5.dp,
                                    color = SurfaceContainerHighest,
                                    shape = RoundedCornerShape(0.dp)
                                )
                        ) {
                            // Sticky store name column
                            Box(
                                modifier = Modifier
                                    .width(storeColumnWidth)
                                    .heightIn(min = cellHeight)
                                    .background(BackgroundGray)
                                    .border(
                                        width = 0.5.dp,
                                        color = OutlineVariant,
                                        shape = RoundedCornerShape(0.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = item.point.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SecondaryGray,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Scrollable input cells — same scroll state as header
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(horizontalScrollState)
                            ) {
                                // White bread cell
                                OrderMatrixCell(
                                    value = loaves,
                                    width = cellWidth,
                                    height = cellHeight,
                                    onValueChange = { newVal ->
                                        viewModel.updateOrderQuantities(item.point.id, newVal, grey, lavash)
                                    }
                                )
                                // Grey bread cell
                                OrderMatrixCell(
                                    value = grey,
                                    width = cellWidth,
                                    height = cellHeight,
                                    onValueChange = { newVal ->
                                        viewModel.updateOrderQuantities(item.point.id, loaves, newVal, lavash)
                                    }
                                )
                                // Lavash cell
                                OrderMatrixCell(
                                    value = lavash,
                                    width = cellWidth,
                                    height = cellHeight,
                                    onValueChange = { newVal ->
                                        viewModel.updateOrderQuantities(item.point.id, loaves, grey, newVal)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Floating Save Button ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Button(
                onClick = { /* Auto-saved; visual feedback only */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryContainerGray,
                    contentColor = OnSecondaryContainerGray
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Сохранить заказ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * Single editable cell inside the orders matrix table.
 * Shows a compact numeric input with bakery-themed styling.
 */
@Composable
fun OrderMatrixCell(
    value: Int,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    onValueChange: (Int) -> Unit
) {
    var textValue by remember(value) { mutableStateOf(if (value == 0) "" else value.toString()) }
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = textValue,
            onValueChange = { input ->
                if (input.isEmpty() || input.all { it.isDigit() }) {
                    textValue = input
                    onValueChange(input.toIntOrNull() ?: 0)
                }
            },
            textStyle = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryGray,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isFocused) SurfaceWhite else BackgroundGray
                )
                .border(
                    width = if (isFocused) 1.5.dp else 1.dp,
                    color = if (isFocused) PrimaryGreen else OutlineVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 4.dp, vertical = 8.dp),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (textValue.isEmpty()) {
                        Text(
                            text = "0",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SecondaryGray.copy(alpha = 0.35f),
                            textAlign = TextAlign.Center
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

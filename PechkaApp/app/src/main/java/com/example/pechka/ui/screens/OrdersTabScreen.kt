package com.example.pechka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pechka.ui.DeliveryViewModel
import com.example.pechka.theme.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction

// Weight ratio constants for the adaptive table layout.
// Store name takes ~41% of width, each product column takes ~19.5%.
private const val STORE_COLUMN_WEIGHT = 1.7f
private const val PRODUCT_COLUMN_WEIGHT = 0.8f

@Composable
fun OrdersTabScreen(viewModel: DeliveryViewModel) {
    val points by viewModel.pointsWithOrderAndPaymentFlow.collectAsState(initial = emptyList())

    // Compute fill progress: count stores where at least one product is ordered
    val totalStores = points.size
    val filledStores = points.count { item ->
        val loaves = item.order?.loavesQtyPieces ?: 0
        val grey = item.order?.greyBreadQtyPieces ?: 0
        val lavash = item.order?.lavashQtyPieces ?: 0
        loaves > 0 || grey > 0 || lavash > 0
    }
    val progressFraction = if (totalStores > 0) filledStores.toFloat() / totalStores else 0f

    val cellHeight = 48.dp

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top App Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(BackgroundGray)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(80.dp))

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
                    modifier = Modifier.width(80.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Очистить",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = PrimaryGreen,
                        maxLines = 1
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
                        text = "$filledStores из $totalStores",
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

            // ── Matrix Grid (fully adaptive, no horizontal scroll) ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 52.dp) // space for floating button
            ) {
                // ── Table Header Row ──
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Store name header
                    Box(
                        modifier = Modifier
                            .weight(STORE_COLUMN_WEIGHT)
                            .background(SurfaceContainer)
                            .border(0.5.dp, OutlineVariant)
                            .padding(8.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(
                            text = "Магазин",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = SecondaryGray,
                            maxLines = 1
                        )
                    }
                    // Белый хлеб header
                    Box(
                        modifier = Modifier
                            .weight(PRODUCT_COLUMN_WEIGHT)
                            .background(SurfaceContainer)
                            .border(0.5.dp, OutlineVariant)
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.BakeryDining,
                                contentDescription = "Белый хлеб",
                                tint = SecondaryGray.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Белый",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = SecondaryGray,
                                maxLines = 1
                            )
                        }
                    }
                    // Серый хлеб header
                    Box(
                        modifier = Modifier
                            .weight(PRODUCT_COLUMN_WEIGHT)
                            .background(SurfaceContainer)
                            .border(0.5.dp, OutlineVariant)
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.BreakfastDining,
                                contentDescription = "Серый хлеб",
                                tint = SecondaryGray.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Серый",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = SecondaryGray,
                                maxLines = 1
                            )
                        }
                    }
                    // Лаваш header
                    Box(
                        modifier = Modifier
                            .weight(PRODUCT_COLUMN_WEIGHT)
                            .background(SurfaceContainer)
                            .border(0.5.dp, OutlineVariant)
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Flatware,
                                contentDescription = "Лаваш",
                                tint = SecondaryGray.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Лаваш",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = SecondaryGray,
                                maxLines = 1
                            )
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
                                .height(IntrinsicSize.Min)
                                .border(0.5.dp, SurfaceContainerHighest)
                        ) {
                            // Store name column
                            Box(
                                modifier = Modifier
                                    .weight(STORE_COLUMN_WEIGHT)
                                    .fillMaxHeight()
                                    .heightIn(min = cellHeight)
                                    .background(BackgroundGray)
                                    .border(0.5.dp, OutlineVariant)
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = item.point.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SecondaryGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // White bread cell
                            OrderMatrixCell(
                                value = loaves,
                                height = cellHeight,
                                modifier = Modifier
                                    .weight(PRODUCT_COLUMN_WEIGHT)
                                    .fillMaxHeight(),
                                onValueChange = { newVal ->
                                    viewModel.updateOrderQuantities(item.point.id, newVal, grey, lavash)
                                }
                            )
                            // Grey bread cell
                            OrderMatrixCell(
                                value = grey,
                                height = cellHeight,
                                modifier = Modifier
                                    .weight(PRODUCT_COLUMN_WEIGHT)
                                    .fillMaxHeight(),
                                onValueChange = { newVal ->
                                    viewModel.updateOrderQuantities(item.point.id, loaves, newVal, lavash)
                                }
                            )
                            // Lavash cell
                            OrderMatrixCell(
                                value = lavash,
                                height = cellHeight,
                                modifier = Modifier
                                    .weight(PRODUCT_COLUMN_WEIGHT)
                                    .fillMaxHeight(),
                                onValueChange = { newVal ->
                                    viewModel.updateOrderQuantities(item.point.id, loaves, grey, newVal)
                                }
                            )
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { /* Auto-saved; visual feedback only */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryContainerGray,
                    contentColor = OnSecondaryContainerGray
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .wrapContentWidth()
                    .height(40.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Сохранить заказ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * Single editable cell inside the orders matrix table.
 * Uses weight-based sizing to adapt to any screen width.
 */
@Composable
fun OrderMatrixCell(
    value: Int,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    onValueChange: (Int) -> Unit
) {
    var textValue by remember(value) { mutableStateOf(if (value == 0) "" else value.toString()) }
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
 
    Box(
        modifier = modifier
            .fillMaxHeight()
            .heightIn(min = height)
            .padding(3.dp),
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                }
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxSize()
                .onFocusChanged { isFocused = it.isFocused }
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isFocused) SurfaceWhite else BackgroundGray
                )
                .border(
                    width = if (isFocused) 1.5.dp else 1.dp,
                    color = if (isFocused) PrimaryGreen else OutlineVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 2.dp, vertical = 8.dp),
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

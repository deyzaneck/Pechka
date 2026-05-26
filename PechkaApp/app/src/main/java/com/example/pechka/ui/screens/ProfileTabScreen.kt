package com.example.pechka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
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
import com.example.pechka.data.effectiveWhitePrice
import com.example.pechka.data.effectiveGreyPrice
import com.example.pechka.data.effectiveLavashPrice
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileTabScreen(
    viewModel: DeliveryViewModel,
    firstName: String,
    lastName: String
) {
    val points by viewModel.pointsWithOrderAndPaymentFlow.collectAsState(initial = emptyList())
    val prices by viewModel.activePrices.collectAsState()
    val expenseItems by viewModel.expenseItems.collectAsState()
    val expenseTotal by viewModel.expenseTotal.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showAddPointDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showShipmentCheckDialog by remember { mutableStateOf(false) }
    var editingPoint by remember { mutableStateOf<com.example.pechka.data.PointWithOrderAndPayment?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var deletingPoint by remember { mutableStateOf<com.example.pechka.data.PointWithOrderAndPayment?>(null) }
    var pricingPoint by remember { mutableStateOf<com.example.pechka.data.PointWithOrderAndPayment?>(null) }

    // Prices editing state
    var whitePriceInput by remember(prices) { mutableStateOf(prices.whiteBreadPrice.toInt().toString()) }
    var greyPriceInput by remember(prices) { mutableStateOf(prices.greyBreadPrice.toInt().toString()) }
    var lavashPriceInput by remember(prices) { mutableStateOf(prices.lavashPrice.toInt().toString()) }

    // Cash register calculations
    val completed = remember(points) { points.filter { it.point.status == "COMPLETED" } }
    val cashSum = completed.filter { it.payment?.paymentType == "CASH" }.sumOf { it.payment?.amountPaid ?: 0.0 }
    val cardSum = completed.filter { it.payment?.paymentType == "CARD" }.sumOf { it.payment?.amountPaid ?: 0.0 }
    val totalSum = cashSum + cardSum

    // Per-product revenue: (ordered - returned) * effective price, only for COMPLETED points
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

    val netCash = cashSum - expenseTotal

    fun formatTrays(pieces: Int): String {
        return Math.round(pieces / 16.0).toString()
    }

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
    if (showShipmentCheckDialog) {
        AlertDialog(
            onDismissRequest = { showShipmentCheckDialog = false },
            title = {
                Text(
                    text = "Отгрузка по точкам",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    fontSize = 18.sp
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (points.isEmpty()) {
                        item {
                            Text("Магазинов нет.", color = SecondaryGray)
                        }
                    } else {
                        items(points, key = { it.point.id }) { item ->
                            val w = item.order?.loavesQtyPieces ?: 0
                            val g = item.order?.greyBreadQtyPieces ?: 0
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
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = PrimaryGreen,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    softWrap = false
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Б:$w | С:$g | Л:$l",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryContainerGreen,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showShipmentCheckDialog = false }) {
                    Text("Готово", fontWeight = FontWeight.Bold, color = PrimaryGreen)
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

    // Add expense dialog
    if (showAddExpenseDialog) {
        var expenseNameInput by remember { mutableStateOf("") }
        var expenseAmountInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = {
                Text("Добавить расход", fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 18.sp)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = expenseNameInput,
                        onValueChange = { expenseNameInput = it },
                        label = { Text("Название (бензин, обед...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = OutlineGray
                        )
                    )
                    OutlinedTextField(
                        value = expenseAmountInput,
                        onValueChange = { v ->
                            if (v.isEmpty() || v.all { it.isDigit() }) expenseAmountInput = v
                        },
                        label = { Text("Сумма") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        suffix = { Text("₽", fontWeight = FontWeight.Bold, color = SecondaryGray) },
                        textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ErrorRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = OutlineGray
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = expenseAmountInput.toDoubleOrNull() ?: 0.0
                        if (expenseNameInput.isNotBlank() && amount > 0.0) {
                            viewModel.addExpenseItem(expenseNameInput.trim(), amount)
                            showAddExpenseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainerGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Добавить", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) {
                    Text("Отмена", color = SecondaryGray)
                }
            },
            containerColor = SurfaceWhite
        )
    }

    // Per-store special pricing dialog
    if (pricingPoint != null) {
        val p = pricingPoint!!
        val existing = p.storePrice
        var enabled by remember(p.point.id) { mutableStateOf(existing?.enabled ?: false) }
        var whiteSpecial by remember(p.point.id) {
            mutableStateOf((existing?.whiteBreadPrice ?: prices.whiteBreadPrice).toInt().toString())
        }
        var greySpecial by remember(p.point.id) {
            mutableStateOf((existing?.greyBreadPrice ?: prices.greyBreadPrice).toInt().toString())
        }
        var lavashSpecial by remember(p.point.id) {
            mutableStateOf((existing?.lavashPrice ?: prices.lavashPrice).toInt().toString())
        }

        AlertDialog(
            onDismissRequest = { pricingPoint = null },
            title = {
                Column {
                    Text(
                        text = "Особая цена",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = p.point.name,
                        fontSize = 13.sp,
                        color = SecondaryGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceContainerLow, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Использовать особую цену",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Только для этого магазина",
                                fontSize = 11.sp,
                                color = SecondaryGray,
                                maxLines = 1
                            )
                        }
                        Switch(
                            checked = enabled,
                            onCheckedChange = { enabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryContainerGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = SecondaryGray.copy(alpha = 0.4f)
                            )
                        )
                    }

                    Text(
                        text = "Цены для этого магазина (₽/шт)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGray
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = whiteSpecial,
                            onValueChange = { v -> if (v.isEmpty() || v.all { it.isDigit() }) whiteSpecial = v },
                            label = { Text("Буханка", fontSize = 11.sp) },
                            enabled = enabled,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = greySpecial,
                            onValueChange = { v -> if (v.isEmpty() || v.all { it.isDigit() }) greySpecial = v },
                            label = { Text("Серый", fontSize = 11.sp) },
                            enabled = enabled,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = lavashSpecial,
                            onValueChange = { v -> if (v.isEmpty() || v.all { it.isDigit() }) lavashSpecial = v },
                            label = { Text("Лаваш", fontSize = 11.sp) },
                            enabled = enabled,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    if (!enabled) {
                        Text(
                            text = "Сейчас используются общие цены: ${prices.whiteBreadPrice.toInt()} / ${prices.greyBreadPrice.toInt()} / ${prices.lavashPrice.toInt()} ₽",
                            fontSize = 11.sp,
                            color = SecondaryGray,
                            maxLines = 2
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = whiteSpecial.toDoubleOrNull() ?: prices.whiteBreadPrice
                        val g = greySpecial.toDoubleOrNull() ?: prices.greyBreadPrice
                        val l = lavashSpecial.toDoubleOrNull() ?: prices.lavashPrice
                        viewModel.setStoreSpecialPrice(p.point.id, enabled, w, g, l)
                        pricingPoint = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainerGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Сохранить", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pricingPoint = null }) {
                    Text("Отмена", color = SecondaryGray)
                }
            },
            containerColor = SurfaceWhite
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
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineVariant, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainerGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Водитель",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$lastName $firstName",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Водитель-экспедитор",
                            fontSize = 12.sp,
                            color = SecondaryGray,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = SimpleDateFormat("EEEE, d MMMM", Locale.forLanguageTag("ru")).format(Date()).replaceFirstChar { it.uppercase() },
                            fontSize = 11.sp,
                            color = SecondaryGray,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Block 1: Revenue by products
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
                        color = SecondaryGray
                    )
                    Text(
                        text = "• • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • •",
                        color = OutlineVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Буханки (белый/серый)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrimaryGreen, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${whiteSoldPieces + greySoldPieces} шт продано", fontSize = 11.sp, color = SecondaryGray, maxLines = 1)
                        }
                        Text((whiteRevenue + greyRevenue).asRubles(), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = PrimaryGreen, maxLines = 1)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Лаваш", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrimaryGreen, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("$lavashSoldPieces шт продано", fontSize = 11.sp, color = SecondaryGray, maxLines = 1)
                        }
                        Text(lavashRevenue.asRubles(), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = PrimaryGreen, maxLines = 1)
                    }
                    Text(
                        text = "• • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • •",
                        color = OutlineVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Всего:", fontWeight = FontWeight.Bold, color = SecondaryGray)
                        Text(totalSum.asRubles(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryContainerGreen)
                    }
                }
            }
        }

        // Block 2: Касса (cash/card split)
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
                        text = "КАССА",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGray
                    )
                    Text(
                        text = "• • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • •",
                        color = OutlineVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Наличные", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(cashSum.asRubles(), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = PrimaryContainerGreen)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Безнал (Карты)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(cardSum.asRubles(), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1976D2))
                    }
                }
            }
        }

        // Block 3: Expenses + Net cash
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "РАСХОДЫ И ЧИСТАЯ КАССА",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryGray
                        )
                        IconButton(onClick = { showAddExpenseDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Добавить расход",
                                tint = PrimaryContainerGreen
                            )
                        }
                    }
                    Text(
                        text = "• • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • •",
                        color = OutlineVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )

                    if (expenseItems.isEmpty()) {
                        Text(
                            text = "Нет расходов за смену",
                            fontSize = 14.sp,
                            color = SecondaryGray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        expenseItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceContainerLow, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "−${item.amount.asRubles()}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = ErrorRed
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { viewModel.deleteExpenseItem(item.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Удалить расход",
                                            tint = ErrorRed.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = "• • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • •",
                        color = OutlineVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Всего расходов:",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = SecondaryGray
                        )
                        Text(
                            text = expenseTotal.asRubles(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ErrorRed
                        )
                    }

                    HorizontalDivider(color = OutlineVariant)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Чистая наличка в кармане",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PrimaryGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Наличные − Расходы",
                                fontSize = 11.sp,
                                color = SecondaryGray,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = netCash.asRubles(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = if (netCash >= 0.0) PrimaryContainerGreen else ErrorRed,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Shipment check button
        item {
            Button(
                onClick = { showShipmentCheckDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "ПРОВЕРИТЬ ОТГРУЗКУ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White,
                    maxLines = 1
                )
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
                                    onClick = { pricingPoint = item },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = "Особая цена",
                                        tint = if (item.storePrice?.enabled == true) PrimaryGreen else SecondaryGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
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

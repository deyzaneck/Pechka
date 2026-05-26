package com.example.pechka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pechka.ui.DeliveryViewModel
import com.example.pechka.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryPointRow(
    item: com.example.pechka.data.PointWithOrderAndPayment,
    formatter: SimpleDateFormat
) {
    val timestamp = item.payment?.timestamp ?: 0L
    val timeStr = if (timestamp > 0) formatter.format(Date(timestamp)) else "--:--"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite, RoundedCornerShape(8.dp))
            .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.point.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PrimaryGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(SurfaceContainer, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = timeStr,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGray
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            val order = item.order
            if (order != null) {
                Text(
                    text = "Буханки: ${order.loavesQtyPieces} шт. • Серый: ${order.greyBreadQtyPieces} шт. • Лаваш: ${order.lavashQtyPieces} шт.",
                    fontSize = 12.sp,
                    color = SecondaryGray
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            val typeText = if (item.payment?.paymentType == "CASH") "Наличные" else "Безнал"
            Text(
                text = "Оплата: $typeText",
                fontSize = 12.sp,
                color = if (item.payment?.paymentType == "CASH") PrimaryContainerGreen else Color(0xFF1976D2)
            )
        }
        Text(
            text = (item.payment?.amountPaid?.toInt() ?: 0).asRubles(),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = PrimaryContainerGreen
        )
    }
}

@Composable
fun HistoryTabScreen(
    viewModel: DeliveryViewModel
) {
    val points by viewModel.pointsWithOrderAndPaymentFlow.collectAsState(initial = emptyList())
    val pastHistory by viewModel.pastHistoryFlow.collectAsState(initial = emptyList())
    val formatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val completedPoints = remember(points) {
        points.filter { item ->
            item.point.status == "COMPLETED" && (
                (item.order?.loavesQtyPieces ?: 0) > 0 ||
                (item.order?.greyBreadQtyPieces ?: 0) > 0 ||
                (item.order?.lavashQtyPieces ?: 0) > 0
            )
        }
    }
    val pastRoutes = remember(pastHistory) {
        pastHistory
            .groupBy { it.routeId }
            .entries
            .sortedByDescending { it.key }
            .take(3)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "История рейса",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ТЕКУЩИЙ СТАТУС РЕЙСА",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Выполнено точек: ${completedPoints.size} из ${points.size}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            // Current Route Header
            item {
                Text(
                    text = "Текущий рейс",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryGray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (completedPoints.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceWhite, RoundedCornerShape(12.dp))
                            .border(1.dp, OutlineVariant, RoundedCornerShape(12.dp))
                            .padding(vertical = 28.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = SecondaryGray.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Точки ещё не закрыты",
                            color = SecondaryGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Здесь появятся завершённые сегодня доставки",
                            color = SecondaryGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            } else {
                items(completedPoints) { item ->
                    HistoryPointRow(item, formatter)
                }
            }

            if (pastRoutes.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Предыдущие рейсы (до 3-х дней)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                pastRoutes.forEach { (pastRouteId, pastPoints) ->
                    val totalSum = pastPoints.sumOf { it.amountPaid }
                    val dateStr = if (pastPoints.isNotEmpty()) {
                        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(pastPoints.first().timestamp))
                    } else {
                        "Дата неизвестна"
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Рейс №$pastRouteId ($dateStr)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PrimaryGreen
                                )
                                Text(
                                    text = totalSum.asRubles(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PrimaryContainerGreen
                                )
                            }
                        }
                    }

                    items(pastPoints) { pastItem ->
                        Box(modifier = Modifier.padding(start = 12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceWhite, RoundedCornerShape(8.dp))
                                    .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = pastItem.pointName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = PrimaryGreen
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(SurfaceContainer, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = formatter.format(Date(pastItem.timestamp)),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SecondaryGray
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Буханки: ${pastItem.loavesQty} шт. • Серый: ${pastItem.greyBreadQty} шт. • Лаваш: ${pastItem.lavashQty} шт.",
                                        fontSize = 12.sp,
                                        color = SecondaryGray
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val typeText = if (pastItem.paymentType == "CASH") "Наличные" else "Безнал"
                                    Text(
                                        text = "Оплата: $typeText",
                                        fontSize = 12.sp,
                                        color = if (pastItem.paymentType == "CASH") PrimaryContainerGreen else Color(0xFF1976D2)
                                    )
                                }
                                Text(
                                    text = pastItem.amountPaid.asRubles(),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = PrimaryContainerGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

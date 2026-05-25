package com.example.pechka.ui.route

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pechka.data.PointWithOrderAndPayment
import com.example.pechka.theme.PrimaryContainerGreen
import com.example.pechka.theme.SecondaryGray
import com.example.pechka.theme.WarningOrange
import com.example.pechka.ui.route.components.ProductSummaryCard

/**
 * Pre-route loading screen: shows aggregated warehouse summary and the
 * "СФОРМИРОВАТЬ ЗАГРУЗКУ" CTA.
 *
 * UX notes:
 * - The CTA is anchored to the bottom of the screen (thumb zone).
 * - The "needs rounding" warning sits *between* the data and the CTA so
 *   the user reads it right before tapping the button.
 * - Totals are computed via remember(points) so they don't recalculate
 *   on every recomposition triggered by sibling state.
 */
@Composable
internal fun LoadingSummaryPhase(
    points: List<PointWithOrderAndPayment>,
    onStartRoute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totals = remember(points) { RouteTotals.from(points) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(RouteDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(RouteDimens.SectionGap),
    ) {
        if (points.isEmpty()) {
            EmptyOrders(modifier = Modifier.fillMaxWidth().weight(1f))
            return@Column
        }

        Text(
            text = "СВОДНЫЙ ОТЧЁТ ДЛЯ СКЛАДА",
            fontSize = RouteDimens.LabelSmall,
            fontWeight = FontWeight.Bold,
            color = SecondaryGray,
        )

        ProductSummaryCard(
            title = "Буханки (Белый)",
            subtitle = "${RouteFormatters.formatTrays(totals.whiteTrays)} л. × ${RouteDimens.PIECES_PER_TRAY} шт.",
            valueText = "${totals.whitePieces} шт",
        )
        ProductSummaryCard(
            title = "Серый хлеб",
            subtitle = "${RouteFormatters.formatTrays(totals.greyTrays)} л. × ${RouteDimens.PIECES_PER_TRAY} шт.",
            valueText = "${totals.greyPieces} шт",
        )
        ProductSummaryCard(
            title = "Лаваш",
            subtitle = "Поштучно",
            valueText = "${totals.lavashPieces} шт",
        )

        if (totals.needsRounding) RoundingWarningBanner()

        Spacer(modifier = Modifier.weight(1f))

        StartRouteButton(enabled = totals.hasAny, onClick = onStartRoute)
    }
}

@Composable
private fun EmptyOrders(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = "Нет заказов.\nВнесите количество на вкладке «Заказы».",
            color = SecondaryGray,
            fontSize = RouteDimens.TitleSmall,
            lineHeight = 22.sp,
        )
    }
}

@Composable
private fun RoundingWarningBanner() {
    Card(
        colors = CardDefaults.cardColors(containerColor = WarningOrange),
        shape = RoundedCornerShape(RouteDimens.ButtonRadius),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "⚠ Лотки дробные — потребуется округление",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = RouteDimens.BodyMedium,
            modifier = Modifier.padding(12.dp),
            maxLines = 2,
        )
    }
}

@Composable
private fun StartRouteButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryContainerGreen,
            disabledContainerColor = SecondaryGray.copy(alpha = 0.3f),
            contentColor = Color.White,
            disabledContentColor = SecondaryGray,
        ),
        shape = RoundedCornerShape(RouteDimens.PrimaryButtonRadius),
        modifier = Modifier
            .fillMaxWidth()
            .height(RouteDimens.PrimaryCtaHeight),
    ) {
        Text(
            text = "СФОРМИРОВАТЬ ЗАГРУЗКУ",
            fontWeight = FontWeight.Bold,
            fontSize = RouteDimens.TitleSmall,
            maxLines = 1,
        )
    }
}


package com.example.pechka.ui.route.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pechka.theme.OutlineVariant
import com.example.pechka.theme.PrimaryGreen
import com.example.pechka.theme.SecondaryGray
import com.example.pechka.theme.SurfaceWhite
import com.example.pechka.ui.route.RouteDimens

/**
 * Reusable product summary card used by the loading phase ("СВОДНЫЙ ОТЧЁТ").
 * Replaces three near-identical Card blocks in the legacy LoadingSummaryPhase
 * and gives every product row the same vertical rhythm + overflow behaviour.
 */
@Composable
internal fun ProductSummaryCard(
    title: String,
    subtitle: String,
    valueText: String,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, OutlineVariant, RoundedCornerShape(RouteDimens.ButtonRadius)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RouteDimens.CardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = RouteDimens.BodyLarge,
                    color = PrimaryGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    fontSize = RouteDimens.LabelMedium,
                    color = SecondaryGray,
                    maxLines = 1,
                )
            }
            Spacer(modifier = Modifier.width(RouteDimens.CardGap))
            Text(
                text = valueText,
                fontWeight = FontWeight.ExtraBold,
                fontSize = RouteDimens.DisplayMedium,
                color = PrimaryGreen,
            )
        }
    }
}

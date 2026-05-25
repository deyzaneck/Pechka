package com.example.pechka.ui.route.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pechka.theme.PrimaryGreen
import com.example.pechka.ui.route.RouteDimens

/**
 * Displays "Хлеб Б: 12 шт • Серый: 8 шт • Лаваш: 4 шт" with bold values.
 *
 * UX rationale: legacy code rendered this as a single-line ellipsised
 * AnnotatedString — on compact widths (iPhone SE / Android 320dp) the
 * lavash count was silently truncated. We now allow up to two lines so
 * the data is never hidden, only the layout rhythm degrades.
 */
@Composable
internal fun PiecesLine(
    whitePieces: Int,
    greyPieces: Int,
    lavashPieces: Int,
    labelSize: TextUnit = RouteDimens.LabelMedium,
    valueSize: TextUnit = RouteDimens.NumericLarge,
    labelColor: Color = Color.Black,
    valueColor: Color = PrimaryGreen,
    modifier: Modifier = Modifier,
) {
    val text = buildAnnotatedString {
        appendChip("Хлеб Б: ", "$whitePieces шт", labelSize, valueSize, labelColor, valueColor)
        appendSeparator(labelSize, labelColor)
        appendChip("Серый: ", "$greyPieces шт", labelSize, valueSize, labelColor, valueColor)
        appendSeparator(labelSize, labelColor)
        appendChip("Лаваш: ", "$lavashPieces шт", labelSize, valueSize, labelColor, valueColor)
    }
    Text(text = text, maxLines = 2, modifier = modifier)
}

private fun AnnotatedString.Builder.appendChip(
    label: String,
    value: String,
    labelSize: TextUnit,
    valueSize: TextUnit,
    labelColor: Color,
    valueColor: Color,
) {
    withStyle(SpanStyle(fontSize = labelSize, fontWeight = FontWeight.Bold, color = labelColor)) {
        append(label)
    }
    withStyle(SpanStyle(fontSize = valueSize, fontWeight = FontWeight.ExtraBold, color = valueColor)) {
        append(value)
    }
}

private fun AnnotatedString.Builder.appendSeparator(labelSize: TextUnit, labelColor: Color) {
    withStyle(SpanStyle(fontSize = labelSize, fontWeight = FontWeight.Bold, color = labelColor)) {
        append("  •  ")
    }
}

/** Small rounded badge used for status indicators (e.g. "СДАНО"). */
@Composable
internal fun StatusBadge(
    text: String,
    background: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(RouteDimens.ChipRadius))
            .background(background)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
        )
    }
}

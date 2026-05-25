package com.example.pechka.ui.route

import com.example.pechka.data.PointWithOrderAndPayment
import java.util.Locale

/**
 * Formatters and totals helpers reused by every phase of the Route flow.
 * Pulling these out of the composables removes duplicated lambdas and
 * keeps the UI layer free of String.format / Locale boilerplate.
 */
internal object RouteFormatters {

    /** "5" if integer, "5.5" otherwise. Always Locale.US to keep the dot. */
    fun formatTrays(trays: Double): String =
        if (trays % 1.0 == 0.0) trays.toInt().toString()
        else String.format(Locale.US, "%.1f", trays)
}

/**
 * Aggregated totals across all points. Computed once per [points] update
 * via remember(points) on the call site to avoid recomputing during
 * unrelated recompositions (sticky bar, dialogs, etc.).
 */
internal data class RouteTotals(
    val whitePieces: Int,
    val greyPieces: Int,
    val lavashPieces: Int,
) {
    val whiteTrays: Double get() = whitePieces / RouteDimens.PIECES_PER_TRAY.toDouble()
    val greyTrays: Double get() = greyPieces / RouteDimens.PIECES_PER_TRAY.toDouble()

    val hasAny: Boolean get() = whitePieces > 0 || greyPieces > 0 || lavashPieces > 0

    val needsRounding: Boolean
        get() = whitePieces % RouteDimens.PIECES_PER_TRAY != 0 ||
            greyPieces % RouteDimens.PIECES_PER_TRAY != 0

    companion object {
        fun from(points: List<PointWithOrderAndPayment>): RouteTotals = RouteTotals(
            whitePieces = points.sumOf { it.order?.loavesQtyPieces ?: 0 },
            greyPieces = points.sumOf { it.order?.greyBreadQtyPieces ?: 0 },
            lavashPieces = points.sumOf { it.order?.lavashQtyPieces ?: 0 },
        )
    }
}

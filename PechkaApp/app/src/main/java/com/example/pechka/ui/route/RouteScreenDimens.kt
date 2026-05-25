package com.example.pechka.ui.route

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Centralised sizing/typography for the Route flow.
 *
 * Why: replaces magic numbers scattered through the previous monolithic
 * RouteScreen.kt so the visual rhythm is consistent and easy to tune
 * for different device classes (SE 320dp → Pro Max 430dp).
 */
internal object RouteDimens {
    // Pieces of bread per tray ("лоток"). Domain constant.
    const val PIECES_PER_TRAY = 16

    // Screen-edge & inter-element spacing
    val ScreenPadding = 16.dp
    val ListSidePadding = 12.dp
    val SectionGap = 16.dp
    val CardGap = 8.dp
    val CardPadding = 16.dp
    val CompactCardPadding = 12.dp
    val DialogSpacing = 14.dp

    // Touch targets — Material accessibility minimum is 48dp.
    val MinTouchTarget = 48.dp
    val PrimaryCtaHeight = 56.dp
    val ConfirmCtaHeight = 52.dp
    val InputHeight = 56.dp
    val SecondaryCtaHeight = 44.dp
    val ActionButtonHeight = 48.dp
    val ActionButtonMinWidth = 84.dp
    val ActionButtonMaxWidth = 108.dp
    val SegmentedControlHeight = 46.dp

    // Corner radii
    val CardRadius = 10.dp
    val ChipRadius = 4.dp
    val ButtonRadius = 8.dp
    val PrimaryButtonRadius = 12.dp
    val InputRadius = 8.dp

    // Iconography
    val TopBarIconSize = 28.dp
    val StatusIconSize = 22.dp
    val InlineIconSize = 20.dp

    // Typography
    val LabelTiny = 11.sp
    val LabelSmall = 12.sp
    val LabelMedium = 13.sp
    val BodyMedium = 14.sp
    val BodyLarge = 15.sp
    val TitleSmall = 16.sp
    val TitleMedium = 17.sp
    val TitleLarge = 20.sp
    val TopBarTitle = 22.sp
    val DisplayMedium = 22.sp
    val NumericLarge = 18.sp
}

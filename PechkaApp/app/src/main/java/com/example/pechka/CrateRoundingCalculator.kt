package com.example.pechka

data class PointOrder(
    val pointId: Long,
    val pointName: String,
    val initialLoavesQty: Int,
    val initialGreyQty: Int,
    val initialLavashQty: Int,
    var currentLoavesQty: Int = initialLoavesQty,
    var currentGreyQty: Int = initialGreyQty,
    val whiteBreadPrice: Double = 47.0,
    val greyBreadPrice: Double = 47.0,
    val lavashPrice: Double = 52.0
) {
    val calculatedSum: Double
        get() = (currentLoavesQty * whiteBreadPrice) + (currentGreyQty * greyBreadPrice) + (initialLavashQty * lavashPrice)
}

enum class RoundingDirection { UP, DOWN }

class CrateRoundingCalculator(val points: List<PointOrder>) {
    val totalInitialLoaves: Int = points.sumOf { it.initialLoavesQty }
    val totalInitialGrey: Int = points.sumOf { it.initialGreyQty }

    val totalInitialLoavesTrays: Double = totalInitialLoaves / 16.0
    val totalInitialGreyTrays: Double = totalInitialGrey / 16.0

    val needsRoundingLoaves: Boolean = totalInitialLoaves % 16 != 0
    val needsRoundingGrey: Boolean = totalInitialGrey % 16 != 0
    val needsRounding: Boolean = needsRoundingLoaves || needsRoundingGrey

    var loavesDirection: RoundingDirection? = null
        private set
    var greyDirection: RoundingDirection? = null
        private set

    val targetTotalLoaves: Int
        get() {
            if (!needsRoundingLoaves) return totalInitialLoaves
            val dir = loavesDirection ?: return totalInitialLoaves
            return when (dir) {
                RoundingDirection.UP -> kotlin.math.ceil(totalInitialLoavesTrays).toInt() * 16
                RoundingDirection.DOWN -> kotlin.math.floor(totalInitialLoavesTrays).toInt() * 16
            }
        }

    val targetTotalGrey: Int
        get() {
            if (!needsRoundingGrey) return totalInitialGrey
            val dir = greyDirection ?: return totalInitialGrey
            return when (dir) {
                RoundingDirection.UP -> kotlin.math.ceil(totalInitialGreyTrays).toInt() * 16
                RoundingDirection.DOWN -> kotlin.math.floor(totalInitialGreyTrays).toInt() * 16
            }
        }

    val totalCurrentLoaves: Int
        get() = points.sumOf { it.currentLoavesQty }

    val totalCurrentGrey: Int
        get() = points.sumOf { it.currentGreyQty }

    val loavesRemainingBalance: Int
        get() = targetTotalLoaves - totalCurrentLoaves

    val greyRemainingBalance: Int
        get() = targetTotalGrey - totalCurrentGrey

    fun setLoavesRoundingDirection(dir: RoundingDirection) {
        loavesDirection = dir
        points.forEach { it.currentLoavesQty = it.initialLoavesQty }
    }

    fun setGreyRoundingDirection(dir: RoundingDirection) {
        greyDirection = dir
        points.forEach { it.currentGreyQty = it.initialGreyQty }
    }

    fun incrementLoaves(pointId: Long): Boolean {
        val point = points.find { it.pointId == pointId } ?: return false
        point.currentLoavesQty += 1
        return true
    }

    fun decrementLoaves(pointId: Long): Boolean {
        val point = points.find { it.pointId == pointId } ?: return false
        if (point.currentLoavesQty <= 0) return false
        point.currentLoavesQty -= 1
        return true
    }

    fun incrementGrey(pointId: Long): Boolean {
        val point = points.find { it.pointId == pointId } ?: return false
        point.currentGreyQty += 1
        return true
    }

    fun decrementGrey(pointId: Long): Boolean {
        val point = points.find { it.pointId == pointId } ?: return false
        if (point.currentGreyQty <= 0) return false
        point.currentGreyQty -= 1
        return true
    }

    val isBalanced: Boolean
        get() {
            val loavesOk = if (needsRoundingLoaves) (loavesRemainingBalance == 0 && loavesDirection != null) else true
            val greyOk = if (needsRoundingGrey) (greyRemainingBalance == 0 && greyDirection != null) else true
            return loavesOk && greyOk
        }
}

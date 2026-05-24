package com.example.pechka

import org.junit.Assert.*
import org.junit.Test

class CrateRoundingCalculatorTest {

    @Test
    fun testTraysConversionAndRoundingFlag() {
        // Case 1: Loaves 264 pieces / 16 = 16.5 trays (needs rounding), Grey 160 pieces / 16 = 10.0 trays (balanced)
        val points1 = listOf(
            PointOrder(pointId = 1L, pointName = "Полтаво", initialLoavesQty = 80, initialGreyQty = 60, initialLavashQty = 10),
            PointOrder(pointId = 2L, pointName = "Пятерка", initialLoavesQty = 100, initialGreyQty = 50, initialLavashQty = 20),
            PointOrder(pointId = 3L, pointName = "Роза", initialLoavesQty = 84, initialGreyQty = 50, initialLavashQty = 15)
        )
        val calculator1 = CrateRoundingCalculator(points1)
        assertEquals(264, calculator1.totalInitialLoaves)
        assertEquals(160, calculator1.totalInitialGrey)
        assertEquals(16.5, calculator1.totalInitialLoavesTrays, 0.001)
        assertEquals(10.0, calculator1.totalInitialGreyTrays, 0.001)
        
        assertTrue(calculator1.needsRoundingLoaves)
        assertFalse(calculator1.needsRoundingGrey)
        assertTrue(calculator1.needsRounding)
    }

    @Test
    fun testRoundingTargetAndDiscrepancy() {
        val points = listOf(
            PointOrder(pointId = 1L, pointName = "Полтаво", initialLoavesQty = 80, initialGreyQty = 84, initialLavashQty = 10), // Loaves: 80, Grey: 84
            PointOrder(pointId = 2L, pointName = "Пятерка", initialLoavesQty = 100, initialGreyQty = 50, initialLavashQty = 20), // Loaves: 100, Grey: 50
            PointOrder(pointId = 3L, pointName = "Роза", initialLoavesQty = 84, initialGreyQty = 50, initialLavashQty = 15)   // Loaves: 84, Grey: 50
        ) // Loaves: 264 (needs rounding), Grey: 184 (needs rounding)

        val calculator = CrateRoundingCalculator(points)
        assertTrue(calculator.needsRoundingLoaves)
        assertTrue(calculator.needsRoundingGrey)

        // Round Loaves UP (target 272, remaining balance 8)
        calculator.setLoavesRoundingDirection(RoundingDirection.UP)
        assertEquals(272, calculator.targetTotalLoaves)
        assertEquals(8, calculator.loavesRemainingBalance)

        // Round Grey DOWN (target 176, remaining balance -8)
        calculator.setGreyRoundingDirection(RoundingDirection.DOWN)
        assertEquals(176, calculator.targetTotalGrey)
        assertEquals(-8, calculator.greyRemainingBalance)
    }

    @Test
    fun testIncrementDecrementAndCostRecalculation() {
        val points = listOf(
            PointOrder(pointId = 1L, pointName = "Полтаво", initialLoavesQty = 80, initialGreyQty = 50, initialLavashQty = 10)
            // Initial sum: (80 + 50) * 47 + 10 * 52 = 130 * 47 + 520 = 6110 + 520 = 6630.0
        )

        val calculator = CrateRoundingCalculator(points)
        calculator.setLoavesRoundingDirection(RoundingDirection.UP)

        // Increment Loaves
        val incSuccess = calculator.incrementLoaves(1L)
        assertTrue(incSuccess)
        assertEquals(81, points[0].currentLoavesQty)
        assertEquals(6677.0, points[0].calculatedSum, 0.001) // (81 + 50) * 47 + 520 = 6677.0

        // Decrement Loaves
        val decSuccess = calculator.decrementLoaves(1L)
        assertTrue(decSuccess)
        assertEquals(80, points[0].currentLoavesQty)
        assertEquals(6630.0, points[0].calculatedSum, 0.001)
    }

    @Test
    fun testUnderflowProtection() {
        val points = listOf(
            PointOrder(pointId = 1L, pointName = "Полтаво", initialLoavesQty = 0, initialGreyQty = 0, initialLavashQty = 10)
        )
        val calculator = CrateRoundingCalculator(points)
        calculator.setLoavesRoundingDirection(RoundingDirection.DOWN)
        calculator.setGreyRoundingDirection(RoundingDirection.DOWN)

        assertFalse(calculator.decrementLoaves(1L))
        assertFalse(calculator.decrementGrey(1L))
        assertEquals(0, points[0].currentLoavesQty)
        assertEquals(0, points[0].currentGreyQty)
    }

    @Test
    fun testIsBalancedFlag() {
        val points = listOf(
            PointOrder(pointId = 1L, pointName = "Полтаво", initialLoavesQty = 80, initialGreyQty = 80, initialLavashQty = 10),
            PointOrder(pointId = 2L, pointName = "Пятерка", initialLoavesQty = 100, initialGreyQty = 100, initialLavashQty = 20)
        ) // Total Loaves: 180 (needs rounding UP to 192), Total Grey: 180 (needs rounding DOWN to 176)
        
        val calculator = CrateRoundingCalculator(points)
        calculator.setLoavesRoundingDirection(RoundingDirection.UP)
        calculator.setGreyRoundingDirection(RoundingDirection.DOWN)

        assertFalse(calculator.isBalanced)

        // Balance loaves (+12)
        for (i in 1..12) {
            calculator.incrementLoaves(1L)
        }
        // Balance grey (-4)
        for (i in 1..4) {
            calculator.decrementGrey(1L)
        }

        assertEquals(0, calculator.loavesRemainingBalance)
        assertEquals(0, calculator.greyRemainingBalance)
        assertTrue(calculator.isBalanced)
    }
}

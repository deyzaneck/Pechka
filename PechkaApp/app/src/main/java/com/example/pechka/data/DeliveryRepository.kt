package com.example.pechka.data

import kotlinx.coroutines.flow.Flow

class DeliveryRepository(private val deliveryDao: DeliveryDao) {

    val pointsWithOrderAndPaymentFlow: Flow<List<PointWithOrderAndPayment>> =
        deliveryDao.getPointsWithOrderAndPaymentFlow()

    val pricesFlow: Flow<PriceEntity?> =
        deliveryDao.getPricesFlow()

    val pastHistoryFlow: Flow<List<PastPointHistoryEntity>> =
        deliveryDao.getPastPointsHistoryFlow()

    val expenseItemsFlow: Flow<List<ExpenseItemEntity>> =
        deliveryDao.getExpenseItemsFlow()

    suspend fun getPointsWithOrderAndPayment(): List<PointWithOrderAndPayment> {
        return deliveryDao.getPointsWithOrderAndPayment()
    }

    suspend fun getPrices(): PriceEntity {
        return deliveryDao.getPrices() ?: PriceEntity(
            whiteBreadPrice = 47.0,
            greyBreadPrice = 47.0,
            lavashPrice = 52.0
        )
    }

    suspend fun updatePrices(whitePrice: Double, greyPrice: Double, lavashPrice: Double) {
        deliveryDao.updatePrices(PriceEntity(id = 1, whiteBreadPrice = whitePrice, greyBreadPrice = greyPrice, lavashPrice = lavashPrice))
        recalculateActiveRouteSums(whitePrice, greyPrice, lavashPrice)
    }

    private suspend fun recalculateActiveRouteSums(whitePrice: Double, greyPrice: Double, lavashPrice: Double) {
        val points = deliveryDao.getPointsWithOrderAndPayment()
        points.forEach { item ->
            if (item.point.status == "PENDING") {
                val order = item.order
                if (order != null) {
                    val w = item.effectiveWhitePrice(whitePrice)
                    val g = item.effectiveGreyPrice(greyPrice)
                    val l = item.effectiveLavashPrice(lavashPrice)
                    val calculatedSum = (order.loavesQtyPieces * w) + (order.greyBreadQtyPieces * g) + (order.lavashQtyPieces * l)
                    deliveryDao.updateOrderQuantities(item.point.id, order.loavesQtyPieces, order.greyBreadQtyPieces, order.lavashQtyPieces, calculatedSum)
                }
            }
        }
    }

    fun createCalculator(
        dbPoints: List<PointWithOrderAndPayment>,
        whitePrice: Double,
        greyPrice: Double,
        lavashPrice: Double
    ): com.example.pechka.CrateRoundingCalculator {
        val pointOrders = dbPoints.map { item ->
            val order = item.order ?: OrderEntity(
                pointId = item.point.id,
                loavesQtyPieces = 0,
                greyBreadQtyPieces = 0,
                lavashQtyPieces = 0,
                calculatedSum = 0.0
            )
            com.example.pechka.PointOrder(
                pointId = item.point.id,
                pointName = item.point.name,
                initialLoavesQty = order.loavesQtyPieces,
                initialGreyQty = order.greyBreadQtyPieces,
                initialLavashQty = order.lavashQtyPieces,
                whiteBreadPrice = item.effectiveWhitePrice(whitePrice),
                greyBreadPrice = item.effectiveGreyPrice(greyPrice),
                lavashPrice = item.effectiveLavashPrice(lavashPrice)
            )
        }
        return com.example.pechka.CrateRoundingCalculator(pointOrders)
    }

    suspend fun saveRoundedOrders(calculator: com.example.pechka.CrateRoundingCalculator) {
        if (!calculator.isBalanced) {
            throw IllegalStateException("Cannot save orders: CrateRoundingCalculator is not balanced!")
        }
        calculator.points.forEach { pointOrder ->
            deliveryDao.updateOrderQuantities(
                pointId = pointOrder.pointId,
                loaves = pointOrder.currentLoavesQty,
                grey = pointOrder.currentGreyQty,
                lavash = pointOrder.initialLavashQty,
                calculatedSum = pointOrder.calculatedSum
            )
        }
    }

    suspend fun updateOrderQuantities(pointId: Long, loaves: Int, grey: Int, lavash: Int) {
        val prices = getPrices()
        val storePrice = deliveryDao.getStorePrice(pointId)
        val w = if (storePrice?.enabled == true) storePrice.whiteBreadPrice else prices.whiteBreadPrice
        val g = if (storePrice?.enabled == true) storePrice.greyBreadPrice else prices.greyBreadPrice
        val l = if (storePrice?.enabled == true) storePrice.lavashPrice else prices.lavashPrice
        val calculatedSum = (loaves * w) + (grey * g) + (lavash * l)
        deliveryDao.updateOrderQuantities(pointId, loaves, grey, lavash, calculatedSum)
    }

    suspend fun closePoint(pointId: Long, paymentType: String, amountPaid: Double) {
        deliveryDao.closePoint(pointId, paymentType, amountPaid)
    }

    suspend fun reopenPoint(pointId: Long) {
        deliveryDao.reopenPoint(pointId)
    }

    /**
     * Auto-complete all points that have no bread (all quantities are 0).
     * Called when route starts.
     */
    suspend fun autoCompleteEmptyPoints() {
        val points = deliveryDao.getPointsWithOrderAndPayment()
        points.forEach { item ->
            if (item.point.status == "PENDING") {
                val order = item.order
                val hasBread = (order?.loavesQtyPieces ?: 0) > 0 ||
                        (order?.greyBreadQtyPieces ?: 0) > 0 ||
                        (order?.lavashQtyPieces ?: 0) > 0
                if (!hasBread) {
                    deliveryDao.closePoint(item.point.id, "CASH", 0.0)
                }
            }
        }
    }

    /**
     * If the point is currently COMPLETED, reopen it (set to PENDING).
     * Called when bread is added to a point that was auto-completed.
     */
    suspend fun reopenPointIfCompleted(pointId: Long) {
        val points = deliveryDao.getPointsWithOrderAndPayment()
        val item = points.find { it.point.id == pointId }
        if (item != null && item.point.status == "COMPLETED") {
            deliveryDao.reopenPoint(pointId)
        }
    }

    /**
     * If the point is PENDING and has no bread, auto-complete it.
     * Called when bread is removed from a point during active route.
     */
    suspend fun autoCompletePointIfEmpty(pointId: Long) {
        val points = deliveryDao.getPointsWithOrderAndPayment()
        val item = points.find { it.point.id == pointId }
        if (item != null && item.point.status == "PENDING") {
            val order = item.order
            val hasBread = (order?.loavesQtyPieces ?: 0) > 0 ||
                    (order?.greyBreadQtyPieces ?: 0) > 0 ||
                    (order?.lavashQtyPieces ?: 0) > 0
            if (!hasBread) {
                deliveryDao.closePoint(pointId, "CASH", 0.0)
            }
        }
    }

    suspend fun resetRoute() {
        deliveryDao.resetRoute()
    }

    suspend fun deletePoint(pointId: Long) {
        deliveryDao.deletePoint(pointId)
    }

    suspend fun addPointWithOrder(name: String, sequenceOrder: Int) {
        val pointId = deliveryDao.insertPoint(
            PointEntity(name = name, sequenceOrder = sequenceOrder, status = "PENDING")
        )
        deliveryDao.insertOrder(
            OrderEntity(
                pointId = pointId,
                loavesQtyPieces = 0,
                greyBreadQtyPieces = 0,
                lavashQtyPieces = 0,
                calculatedSum = 0.0
            )
        )
    }

    suspend fun updatePointName(pointId: Long, name: String) {
        deliveryDao.updatePointName(pointId, name)
    }

    suspend fun saveReturn(pointId: Long, whiteReturn: Int, greyReturn: Int, lavashReturn: Int) {
        if (whiteReturn == 0 && greyReturn == 0 && lavashReturn == 0) {
            deliveryDao.deleteReturnForPoint(pointId)
        } else {
            deliveryDao.upsertReturn(
                ReturnEntity(
                    pointId = pointId,
                    whiteReturnQty = whiteReturn,
                    greyReturnQty = greyReturn,
                    lavashReturnQty = lavashReturn
                )
            )
        }
    }

    // --- Expense items ---
    suspend fun addExpenseItem(name: String, amount: Double) {
        if (name.isBlank() || amount <= 0.0) return
        deliveryDao.insertExpenseItem(
            ExpenseItemEntity(name = name.trim(), amount = amount)
        )
    }

    suspend fun deleteExpenseItem(id: Long) {
        deliveryDao.deleteExpenseItem(id)
    }

    // --- Store-specific prices ---
    suspend fun setStoreSpecialPrice(
        pointId: Long,
        enabled: Boolean,
        whitePrice: Double,
        greyPrice: Double,
        lavashPrice: Double
    ) {
        val existing = deliveryDao.getStorePrice(pointId)
        deliveryDao.upsertStorePrice(
            StorePriceEntity(
                id = existing?.id ?: 0L,
                pointId = pointId,
                enabled = enabled,
                whiteBreadPrice = whitePrice,
                greyBreadPrice = greyPrice,
                lavashPrice = lavashPrice
            )
        )
        // Recalculate this point's sum if it's still pending
        val global = getPrices()
        val point = deliveryDao.getPointsWithOrderAndPayment().find { it.point.id == pointId }
        if (point != null && point.point.status == "PENDING") {
            val order = point.order
            if (order != null) {
                val w = if (enabled) whitePrice else global.whiteBreadPrice
                val g = if (enabled) greyPrice else global.greyBreadPrice
                val l = if (enabled) lavashPrice else global.lavashPrice
                val sum = (order.loavesQtyPieces * w) + (order.greyBreadQtyPieces * g) + (order.lavashQtyPieces * l)
                deliveryDao.updateOrderQuantities(pointId, order.loavesQtyPieces, order.greyBreadQtyPieces, order.lavashQtyPieces, sum)
            }
        }
    }
}

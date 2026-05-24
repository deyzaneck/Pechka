package com.example.pechka.data

import kotlinx.coroutines.flow.Flow

class DeliveryRepository(private val deliveryDao: DeliveryDao) {

    val pointsWithOrderAndPaymentFlow: Flow<List<PointWithOrderAndPayment>> =
        deliveryDao.getPointsWithOrderAndPaymentFlow()

    val pricesFlow: Flow<PriceEntity?> =
        deliveryDao.getPricesFlow()

    val pastHistoryFlow: Flow<List<PastPointHistoryEntity>> =
        deliveryDao.getPastPointsHistoryFlow()

    val expenseFlow: Flow<ExpenseEntity?> =
        deliveryDao.getExpenseFlow()

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
                    val calculatedSum = (order.loavesQtyPieces * whitePrice) + (order.greyBreadQtyPieces * greyPrice) + (order.lavashQtyPieces * lavashPrice)
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
                whiteBreadPrice = whitePrice,
                greyBreadPrice = greyPrice,
                lavashPrice = lavashPrice
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
        val calculatedSum = (loaves * prices.whiteBreadPrice) + (grey * prices.greyBreadPrice) + (lavash * prices.lavashPrice)
        deliveryDao.updateOrderQuantities(pointId, loaves, grey, lavash, calculatedSum)
    }

    suspend fun closePoint(pointId: Long, paymentType: String, amountPaid: Double) {
        deliveryDao.closePoint(pointId, paymentType, amountPaid)
    }

    suspend fun reopenPoint(pointId: Long) {
        deliveryDao.reopenPoint(pointId)
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

    suspend fun updateExpense(amount: Double) {
        deliveryDao.upsertExpense(ExpenseEntity(id = 1, amount = amount))
    }
}

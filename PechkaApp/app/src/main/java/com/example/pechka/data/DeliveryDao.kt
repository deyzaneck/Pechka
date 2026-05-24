package com.example.pechka.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryDao {
    @Transaction
    @Query("SELECT * FROM points ORDER BY sequenceOrder ASC")
    fun getPointsWithOrderAndPaymentFlow(): Flow<List<PointWithOrderAndPayment>>

    @Transaction
    @Query("SELECT * FROM points ORDER BY sequenceOrder ASC")
    suspend fun getPointsWithOrderAndPayment(): List<PointWithOrderAndPayment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoint(point: PointEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Query("UPDATE orders SET loaves_qty_pieces = :loaves, grey_bread_qty_pieces = :grey, lavash_qty_pieces = :lavash, calculated_sum = :calculatedSum WHERE point_id = :pointId")
    suspend fun updateOrderQuantities(pointId: Long, loaves: Int, grey: Int, lavash: Int, calculatedSum: Double)

    @Query("UPDATE points SET status = :status WHERE id = :pointId")
    suspend fun updatePointStatus(pointId: Long, status: String)

    @Transaction
    suspend fun closePoint(pointId: Long, paymentType: String, amountPaid: Double) {
        val payment = PaymentEntity(
            pointId = pointId,
            paymentType = paymentType,
            amountPaid = amountPaid,
            timestamp = System.currentTimeMillis()
        )
        insertPayment(payment)
        updatePointStatus(pointId, "COMPLETED")
    }

    @Transaction
    suspend fun reopenPoint(pointId: Long) {
        deletePaymentForPoint(pointId)
        updatePointStatus(pointId, "PENDING")
    }

    @Query("DELETE FROM payments WHERE point_id = :pointId")
    suspend fun deletePaymentForPoint(pointId: Long)

    @Query("SELECT * FROM orders WHERE point_id = :pointId")
    suspend fun getOrderForPoint(pointId: Long): OrderEntity?

    @Query("DELETE FROM points WHERE id = :pointId")
    suspend fun deletePoint(pointId: Long)

    @Query("SELECT * FROM prices WHERE id = 1")
    suspend fun getPrices(): PriceEntity?

    @Query("SELECT * FROM prices WHERE id = 1")
    fun getPricesFlow(): Flow<PriceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePrices(price: PriceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPastPointHistory(pastPoint: PastPointHistoryEntity): Long

    @Query("SELECT * FROM past_points_history ORDER BY timestamp DESC")
    fun getPastPointsHistoryFlow(): Flow<List<PastPointHistoryEntity>>

    @Query("SELECT * FROM past_points_history ORDER BY timestamp DESC")
    suspend fun getPastPointsHistory(): List<PastPointHistoryEntity>

    @Query("DELETE FROM past_points_history WHERE route_id < :routeId")
    suspend fun deleteHistoryOlderThan(routeId: Int)

    @Query("SELECT COALESCE(MAX(route_id), 0) FROM past_points_history")
    suspend fun getMaxArchivedRouteId(): Int

    @Transaction
    suspend fun resetRoute() {
        val currentPoints = getPointsWithOrderAndPayment()
        val completedPoints = currentPoints.filter { it.point.status == "COMPLETED" }

        val maxArchivedRoute = getMaxArchivedRouteId()
        val newArchiveRouteId = maxArchivedRoute + 1

        completedPoints.forEach { item ->
            val order = item.order
            val payment = item.payment
            if (order != null && payment != null) {
                insertPastPointHistory(PastPointHistoryEntity(
                    routeId = newArchiveRouteId,
                    pointName = item.point.name,
                    loavesQty = order.loavesQtyPieces,
                    greyBreadQty = order.greyBreadQtyPieces,
                    lavashQty = order.lavashQtyPieces,
                    amountPaid = payment.amountPaid,
                    paymentType = payment.paymentType,
                    timestamp = payment.timestamp
                ))
            }
        }

        clearAllPayments()
        clearAllReturns()
        clearAllExpenses()
        resetAllPointStatuses("PENDING")

        val oldestRouteToKeep = newArchiveRouteId - 2
        if (oldestRouteToKeep > 0) {
            deleteHistoryOlderThan(oldestRouteToKeep)
        }
    }

    @Query("DELETE FROM payments")
    suspend fun clearAllPayments()

    @Query("UPDATE points SET status = :status")
    suspend fun resetAllPointStatuses(status: String)

    @Query("DELETE FROM points")
    suspend fun clearAllPoints()

    @Query("UPDATE points SET name = :name WHERE id = :pointId")
    suspend fun updatePointName(pointId: Long, name: String)

    // --- Returns ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReturn(returnEntity: ReturnEntity): Long

    @Query("SELECT * FROM returns WHERE point_id = :pointId")
    suspend fun getReturnForPoint(pointId: Long): ReturnEntity?

    @Query("DELETE FROM returns WHERE point_id = :pointId")
    suspend fun deleteReturnForPoint(pointId: Long)

    @Query("DELETE FROM returns")
    suspend fun clearAllReturns()

    // --- Expenses (single-row, current shift) ---
    @Query("SELECT * FROM expenses WHERE id = 1")
    suspend fun getExpense(): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE id = 1")
    fun getExpenseFlow(): Flow<ExpenseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses")
    suspend fun clearAllExpenses()
}

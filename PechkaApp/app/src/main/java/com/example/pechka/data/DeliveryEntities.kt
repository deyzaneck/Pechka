package com.example.pechka.data

import androidx.room.*

@Entity(tableName = "points")
data class PointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sequenceOrder: Int,
    val status: String // "PENDING", "COMPLETED"
)

@Entity(
    tableName = "orders",
    foreignKeys = [ForeignKey(
        entity = PointEntity::class,
        parentColumns = ["id"],
        childColumns = ["point_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["point_id"], unique = true)]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "point_id") val pointId: Long,
    @ColumnInfo(name = "loaves_qty_pieces") val loavesQtyPieces: Int,
    @ColumnInfo(name = "grey_bread_qty_pieces") val greyBreadQtyPieces: Int,
    @ColumnInfo(name = "lavash_qty_pieces") val lavashQtyPieces: Int,
    @ColumnInfo(name = "calculated_sum") val calculatedSum: Double,
    @ColumnInfo(name = "loaves_return_pieces", defaultValue = "0") val loavesReturnPieces: Int = 0,
    @ColumnInfo(name = "grey_return_pieces", defaultValue = "0") val greyReturnPieces: Int = 0,
    @ColumnInfo(name = "lavash_return_pieces", defaultValue = "0") val lavashReturnPieces: Int = 0
)

@Entity(
    tableName = "payments",
    foreignKeys = [ForeignKey(
        entity = PointEntity::class,
        parentColumns = ["id"],
        childColumns = ["point_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["point_id"], unique = true)]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "point_id") val pointId: Long,
    @ColumnInfo(name = "payment_type") val paymentType: String, // "CASH", "CARD"
    @ColumnInfo(name = "amount_paid") val amountPaid: Double,
    val timestamp: Long
)

data class PointWithOrderAndPayment(
    @Embedded val point: PointEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "point_id"
    )
    val order: OrderEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "point_id"
    )
    val payment: PaymentEntity?
)

@Entity(tableName = "prices")
data class PriceEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "white_bread_price") val whiteBreadPrice: Double,
    @ColumnInfo(name = "grey_bread_price") val greyBreadPrice: Double,
    @ColumnInfo(name = "lavash_price") val lavashPrice: Double
)

@Entity(tableName = "past_points_history")
data class PastPointHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "route_id") val routeId: Int,
    @ColumnInfo(name = "point_name") val pointName: String,
    @ColumnInfo(name = "loaves_qty") val loavesQty: Int,
    @ColumnInfo(name = "grey_bread_qty") val greyBreadQty: Int,
    @ColumnInfo(name = "lavash_qty") val lavashQty: Int,
    @ColumnInfo(name = "amount_paid") val amountPaid: Double,
    @ColumnInfo(name = "payment_type") val paymentType: String,
    val timestamp: Long
)

@Entity(tableName = "shift_state")
data class ShiftStateEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "expenses", defaultValue = "0") val expenses: Double = 0.0,
    @ColumnInfo(name = "crates_collected_white", defaultValue = "0") val cratesCollectedWhite: Int = 0,
    @ColumnInfo(name = "crates_returned_white", defaultValue = "0") val cratesReturnedWhite: Int = 0,
    @ColumnInfo(name = "crates_collected_grey", defaultValue = "0") val cratesCollectedGrey: Int = 0,
    @ColumnInfo(name = "crates_returned_grey", defaultValue = "0") val cratesReturnedGrey: Int = 0
)

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
    @ColumnInfo(name = "calculated_sum") val calculatedSum: Double
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

@Entity(
    tableName = "returns",
    foreignKeys = [ForeignKey(
        entity = PointEntity::class,
        parentColumns = ["id"],
        childColumns = ["point_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["point_id"], unique = true)]
)
data class ReturnEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "point_id") val pointId: Long,
    @ColumnInfo(name = "white_return_qty") val whiteReturnQty: Int,
    @ColumnInfo(name = "grey_return_qty") val greyReturnQty: Int,
    @ColumnInfo(name = "lavash_return_qty") val lavashReturnQty: Int
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
    val payment: PaymentEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "point_id"
    )
    val returns: ReturnEntity? = null
)

@Entity(tableName = "prices")
data class PriceEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "white_bread_price") val whiteBreadPrice: Double,
    @ColumnInfo(name = "grey_bread_price") val greyBreadPrice: Double,
    @ColumnInfo(name = "lavash_price") val lavashPrice: Double
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "amount") val amount: Double
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

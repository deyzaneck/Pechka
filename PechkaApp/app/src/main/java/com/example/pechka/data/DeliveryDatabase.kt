package com.example.pechka.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PointEntity::class,
        OrderEntity::class,
        PaymentEntity::class,
        PriceEntity::class,
        PastPointHistoryEntity::class,
        ReturnEntity::class,
        ExpenseEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class DeliveryDatabase : RoomDatabase() {
    abstract fun deliveryDao(): DeliveryDao
}

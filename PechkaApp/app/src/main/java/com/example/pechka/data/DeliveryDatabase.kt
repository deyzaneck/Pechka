package com.example.pechka.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PointEntity::class, OrderEntity::class, PaymentEntity::class, PriceEntity::class, PastPointHistoryEntity::class],
    version = 5,
    exportSchema = false
)
abstract class DeliveryDatabase : RoomDatabase() {
    abstract fun deliveryDao(): DeliveryDao
}

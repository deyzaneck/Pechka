package com.example.pechka

import android.app.Application
import androidx.room.Room
import com.example.pechka.data.DeliveryDatabase
import com.example.pechka.data.DeliveryRepository
import com.example.pechka.data.PointEntity
import com.example.pechka.data.OrderEntity
import com.example.pechka.data.PriceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PechkaApplication : Application() {
    lateinit var database: DeliveryDatabase
        private set
    lateinit var repository: DeliveryRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            DeliveryDatabase::class.java,
            "pechka_delivery.db"
        ).fallbackToDestructiveMigration().build()
        
        repository = DeliveryRepository(database.deliveryDao())
        instance = this

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val dao = database.deliveryDao()
            
            // Populate default prices if empty
            val currentPrices = dao.getPrices()
            if (currentPrices == null) {
                dao.updatePrices(PriceEntity(id = 1, whiteBreadPrice = 47.0, greyBreadPrice = 47.0, lavashPrice = 52.0))
            }

            // Populate initial stores if empty
            val existing = dao.getPointsWithOrderAndPayment()
            if (existing.isEmpty()) {
                val initialStores = listOf(
                    "Полтево", "Пятерка", "Роза", "База", "Зара",
                    "Новый", "Белорус", "Полушкино", "Дикси", "Дина",
                    "Анна", "Хрипань", "Вкус М", "Ил Рынок", "Ил Пять",
                    "Саша", "ИП Левина", "МАГ - ДА", "Фабр Рынок", "УЗБ Пекарня",
                    "Горки", "Первомайская", "Люба", "Лианна", "Спартак",
                    "Чулково", "Лева 2"
                )
                initialStores.forEachIndexed { index, name ->
                    val pointId = dao.insertPoint(PointEntity(
                        name = name,
                        sequenceOrder = index + 1,
                        status = "PENDING"
                    ))
                    dao.insertOrder(OrderEntity(
                        pointId = pointId,
                        loavesQtyPieces = 0,
                        greyBreadQtyPieces = 0,
                        lavashQtyPieces = 0,
                        calculatedSum = 0.0
                    ))
                }
            }
        }
    }

    companion object {
        lateinit var instance: PechkaApplication
            private set
    }
}

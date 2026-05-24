package com.example.pechka.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.pechka.CrateRoundingCalculator
import com.example.pechka.RoundingDirection
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class DeliveryDatabaseTest {

    private lateinit var db: DeliveryDatabase
    private lateinit var dao: DeliveryDao
    private lateinit var repository: DeliveryRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, DeliveryDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.deliveryDao()
        repository = DeliveryRepository(dao)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertAndRetrieveRoute() = runBlocking {
        // Добавление магазинов с заказами
        repository.addPointWithOrder("Полтаво", 1)
        repository.addPointWithOrder("Пятерка", 2)

        val list = repository.getPointsWithOrderAndPayment()
        assertEquals(2, list.size)
        
        // Установка количеств для проверки
        repository.updateOrderQuantities(list[0].point.id, 80, 40, 10)
        repository.updateOrderQuantities(list[1].point.id, 100, 50, 20)

        val updatedList = repository.getPointsWithOrderAndPayment()
        assertEquals("Полтаво", updatedList[0].point.name)
        assertEquals(1, updatedList[0].point.sequenceOrder)
        assertEquals("PENDING", updatedList[0].point.status)

        assertEquals(80, updatedList[0].order?.loavesQtyPieces)
        assertEquals(40, updatedList[0].order?.greyBreadQtyPieces)
        assertEquals(10, updatedList[0].order?.lavashQtyPieces)
        
        assertEquals(100, updatedList[1].order?.loavesQtyPieces)
        assertEquals(50, updatedList[1].order?.greyBreadQtyPieces)
        assertEquals(20, updatedList[1].order?.lavashQtyPieces)
    }

    @Test
    fun testUpdateBreadQuantityAndSaveRounded() = runBlocking {
        repository.addPointWithOrder("Полтаво", 1)
        repository.addPointWithOrder("Пятерка", 2)

        val initialList = repository.getPointsWithOrderAndPayment()
        repository.updateOrderQuantities(initialList[0].point.id, 80, 80, 10)
        repository.updateOrderQuantities(initialList[1].point.id, 100, 100, 20)

        val list = repository.getPointsWithOrderAndPayment()
        val calculator = repository.createCalculator(list, 47.0, 47.0, 52.0)

        // Инициализируем округление буханок в большую сторону (+12 шт)
        calculator.setLoavesRoundingDirection(RoundingDirection.UP)
        for (i in 1..12) {
            calculator.incrementLoaves(list[0].point.id)
        }
        
        // Округлим серый хлеб в меньшую сторону (-4 шт)
        calculator.setGreyRoundingDirection(RoundingDirection.DOWN)
        for (i in 1..4) {
            calculator.decrementGrey(list[0].point.id)
        }
        
        assertTrue(calculator.isBalanced)

        // Сохраняем измененные заказы обратно в БД
        repository.saveRoundedOrders(calculator)

        // Читаем из базы заново и сверяем количества
        val updatedList = repository.getPointsWithOrderAndPayment()
        assertEquals(92, updatedList[0].order?.loavesQtyPieces) // 80 + 12 = 92
        assertEquals(76, updatedList[0].order?.greyBreadQtyPieces)
        
        assertEquals(100, updatedList[1].order?.loavesQtyPieces)
        assertEquals(100, updatedList[1].order?.greyBreadQtyPieces)
        
        // Общая стоимость точки 1 должна обновиться: (92 + 76) * 47 + 10 * 52 = 168 * 47 + 520 = 7896 + 520 = 8416.0
        assertEquals(8416.0, updatedList[0].order?.calculatedSum ?: 0.0, 0.001)
    }

    @Test
    fun testCloseAndReopenPoint() = runBlocking {
        repository.addPointWithOrder("Полтаво", 1)
        val list = repository.getPointsWithOrderAndPayment()
        val pointId = list[0].point.id
        
        repository.updateOrderQuantities(pointId, 80, 40, 10)

        // Закрываем точку с частичной оплатой
        repository.closePoint(pointId, "CASH", 1500.0)

        var updatedList = repository.getPointsWithOrderAndPayment()
        assertEquals("COMPLETED", updatedList[0].point.status)
        assertNotNull(updatedList[0].payment)
        assertEquals(1500.0, updatedList[0].payment?.amountPaid ?: 0.0, 0.001)
        assertEquals("CASH", updatedList[0].payment?.paymentType)

        // Переоткрываем точку
        repository.reopenPoint(pointId)

        updatedList = repository.getPointsWithOrderAndPayment()
        assertEquals("PENDING", updatedList[0].point.status)
        assertNull(updatedList[0].payment) // Оплата удалена!
    }

    @Test
    fun testResetRouteClearsPaymentsAndResetsStatuses() = runBlocking {
        repository.addPointWithOrder("Полтаво", 1)
        repository.addPointWithOrder("Пятерка", 2)

        val list = repository.getPointsWithOrderAndPayment()
        val point1Id = list[0].point.id
        val point2Id = list[1].point.id
        
        repository.updateOrderQuantities(point1Id, 80, 40, 10)
        repository.updateOrderQuantities(point2Id, 100, 50, 20)

        repository.closePoint(point1Id, "CASH", 1500.0)
        repository.closePoint(point2Id, "CARD", 2312.0)

        // Проверяем что они закрыты
        var checkedList = repository.getPointsWithOrderAndPayment()
        assertEquals("COMPLETED", checkedList[0].point.status)
        assertEquals("COMPLETED", checkedList[1].point.status)
        assertNotNull(checkedList[0].payment)
        assertNotNull(checkedList[1].payment)

        // Сбрасываем день
        repository.resetRoute()

        // Проверяем, что в базе активные точки сброшены in-place
        checkedList = repository.getPointsWithOrderAndPayment()
        assertEquals(2, checkedList.size)

        // Активные точки переведены в PENDING и оплаты сброшены
        assertEquals("PENDING", checkedList[0].point.status)
        assertEquals("PENDING", checkedList[1].point.status)
        assertNull(checkedList[0].payment)
        assertNull(checkedList[1].payment)

        // Заказы (шаблоны) сохранены
        assertEquals(80, checkedList[0].order?.loavesQtyPieces)
        assertEquals(40, checkedList[0].order?.greyBreadQtyPieces)
        assertEquals(10, checkedList[0].order?.lavashQtyPieces)

        assertEquals(100, checkedList[1].order?.loavesQtyPieces)
        assertEquals(50, checkedList[1].order?.greyBreadQtyPieces)
        assertEquals(20, checkedList[1].order?.lavashQtyPieces)

        // Проверяем архивную историю
        val historyList = dao.getPastPointsHistory()
        assertEquals(2, historyList.size)
        val names = historyList.map { it.pointName }
        assertTrue(names.contains("Полтаво"))
        assertTrue(names.contains("Пятерка"))
        assertEquals(1, historyList[0].routeId)
        assertEquals(1, historyList[1].routeId)
    }
}

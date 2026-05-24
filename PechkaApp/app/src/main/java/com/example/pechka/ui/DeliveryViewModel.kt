package com.example.pechka.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pechka.CrateRoundingCalculator
import com.example.pechka.PechkaApplication
import com.example.pechka.PointOrder
import com.example.pechka.RoundingDirection
import com.example.pechka.data.DeliveryRepository
import com.example.pechka.data.PointWithOrderAndPayment
import com.example.pechka.data.PriceEntity
import com.example.pechka.data.PastPointHistoryEntity
import com.example.pechka.data.ExpenseEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DeliveryViewModel(private val repository: DeliveryRepository) : ViewModel() {

    private val sharedPrefs = PechkaApplication.instance.getSharedPreferences("pechka_prefs", Context.MODE_PRIVATE)
    
    private val _isRouteStarted = MutableStateFlow(sharedPrefs.getBoolean("is_route_started", false))
    val isRouteStarted: StateFlow<Boolean> = _isRouteStarted.asStateFlow()

    val pointsWithOrderAndPaymentFlow: StateFlow<List<PointWithOrderAndPayment>> =
        repository.pointsWithOrderAndPaymentFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())



    val pricesFlow: StateFlow<PriceEntity?> =
        repository.pricesFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activePrices: StateFlow<PriceEntity> = pricesFlow.map {
        it ?: PriceEntity(whiteBreadPrice = 47.0, greyBreadPrice = 47.0, lavashPrice = 52.0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PriceEntity(whiteBreadPrice = 47.0, greyBreadPrice = 47.0, lavashPrice = 52.0))

    val pastHistoryFlow: StateFlow<List<PastPointHistoryEntity>> =
        repository.pastHistoryFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseAmount: StateFlow<Double> = repository.expenseFlow
        .map { it?.amount ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    private val _selectedLoavesDirection = MutableStateFlow<RoundingDirection?>(null)
    val selectedLoavesDirection: StateFlow<RoundingDirection?> = _selectedLoavesDirection.asStateFlow()

    private val _selectedGreyDirection = MutableStateFlow<RoundingDirection?>(null)
    val selectedGreyDirection: StateFlow<RoundingDirection?> = _selectedGreyDirection.asStateFlow()

    private val _roundingPoints = MutableStateFlow<List<PointOrder>>(emptyList())
    val roundingPoints: StateFlow<List<PointOrder>> = _roundingPoints.asStateFlow()

    val isRoundingActive = combine(pointsWithOrderAndPaymentFlow, isRouteStarted) { list, started ->
        if (!started) return@combine false
        if (list.isEmpty()) return@combine false
        val totalLoaves = list.sumOf { it.order?.loavesQtyPieces ?: 0 }
        val totalGrey = list.sumOf { it.order?.greyBreadQtyPieces ?: 0 }
        (totalLoaves % 16 != 0) || (totalGrey % 16 != 0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val loavesTraysText = pointsWithOrderAndPaymentFlow.map { list ->
        val totalLoaves = list.sumOf { it.order?.loavesQtyPieces ?: 0 }
        val trays = totalLoaves / 16.0
        "$totalLoaves шт / ${trays} лотков"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0 шт / 0.0 лотков")

    val greyTraysText = pointsWithOrderAndPaymentFlow.map { list ->
        val totalGrey = list.sumOf { it.order?.greyBreadQtyPieces ?: 0 }
        val trays = totalGrey / 16.0
        "$totalGrey шт / ${trays} лотков"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0 шт / 0.0 лотков")

    val roundingCalculator = combine(
        pointsWithOrderAndPaymentFlow,
        _selectedLoavesDirection,
        _selectedGreyDirection,
        _roundingPoints,
        activePrices
    ) { dbList, loavesDir, greyDir, roundPoints, prices ->
        if (dbList.isEmpty()) return@combine null
        
        val calc = repository.createCalculator(dbList, prices.whiteBreadPrice, prices.greyBreadPrice, prices.lavashPrice)
        loavesDir?.let { calc.setLoavesRoundingDirection(it) }
        greyDir?.let { calc.setGreyRoundingDirection(it) }
        
        roundPoints.forEach { temp ->
            val calcPoint = calc.points.find { it.pointId == temp.pointId }
            if (calcPoint != null) {
                calcPoint.currentLoavesQty = temp.currentLoavesQty
                calcPoint.currentGreyQty = temp.currentGreyQty
            }
        }
        calc
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun startRoute() {
        sharedPrefs.edit().putBoolean("is_route_started", true).apply()
        _isRouteStarted.value = true
    }

    fun selectLoavesRoundingDirection(dir: RoundingDirection) {
        _selectedLoavesDirection.value = dir
        initializeRoundingPointsIfEmpty()
    }

    fun selectGreyRoundingDirection(dir: RoundingDirection) {
        _selectedGreyDirection.value = dir
        initializeRoundingPointsIfEmpty()
    }

    private fun initializeRoundingPointsIfEmpty() {
        if (_roundingPoints.value.isEmpty()) {
            val dbList = pointsWithOrderAndPaymentFlow.value
            val prices = activePrices.value
            val calc = repository.createCalculator(dbList, prices.whiteBreadPrice, prices.greyBreadPrice, prices.lavashPrice)
            _selectedLoavesDirection.value?.let { calc.setLoavesRoundingDirection(it) }
            _selectedGreyDirection.value?.let { calc.setGreyRoundingDirection(it) }
            _roundingPoints.value = calc.points.map { it.copy() }
        }
    }

    fun incrementRoundingLoaves(pointId: Long) {
        val currentList = _roundingPoints.value.map { it.copy() }
        val point = currentList.find { it.pointId == pointId }
        if (point != null) {
            point.currentLoavesQty += 1
            _roundingPoints.value = currentList
        }
    }

    fun decrementRoundingLoaves(pointId: Long) {
        val currentList = _roundingPoints.value.map { it.copy() }
        val point = currentList.find { it.pointId == pointId }
        if (point != null && point.currentLoavesQty > 0) {
            point.currentLoavesQty -= 1
            _roundingPoints.value = currentList
        }
    }

    fun incrementRoundingGrey(pointId: Long) {
        val currentList = _roundingPoints.value.map { it.copy() }
        val point = currentList.find { it.pointId == pointId }
        if (point != null) {
            point.currentGreyQty += 1
            _roundingPoints.value = currentList
        }
    }

    fun decrementRoundingGrey(pointId: Long) {
        val currentList = _roundingPoints.value.map { it.copy() }
        val point = currentList.find { it.pointId == pointId }
        if (point != null && point.currentGreyQty > 0) {
            point.currentGreyQty -= 1
            _roundingPoints.value = currentList
        }
    }

    fun confirmRounding() {
        val calc = roundingCalculator.value
        if (calc != null && calc.isBalanced) {
            viewModelScope.launch {
                repository.saveRoundedOrders(calc)
                _selectedLoavesDirection.value = null
                _selectedGreyDirection.value = null
                _roundingPoints.value = emptyList()
            }
        }
    }

    fun updateOrderQuantities(pointId: Long, loaves: Int, grey: Int, lavash: Int) {
        viewModelScope.launch {
            repository.updateOrderQuantities(pointId, loaves, grey, lavash)
        }
    }

    fun closePoint(pointId: Long, paymentType: String, amount: Double) {
        viewModelScope.launch {
            repository.closePoint(pointId, paymentType, amount)
        }
    }

    fun reopenPoint(pointId: Long) {
        viewModelScope.launch {
            repository.reopenPoint(pointId)
        }
    }

    fun resetDay() {
        viewModelScope.launch {
            repository.resetRoute()
            _selectedLoavesDirection.value = null
            _selectedGreyDirection.value = null
            _roundingPoints.value = emptyList()
            sharedPrefs.edit().putBoolean("is_route_started", false).apply()
            _isRouteStarted.value = false
        }
    }

    // Backward-compat alias
    fun resetRoute() = resetDay()

    fun addPoint(name: String, sequenceOrder: Int) {
        viewModelScope.launch {
            repository.addPointWithOrder(name, sequenceOrder)
        }
    }

    fun deletePoint(pointId: Long) {
        viewModelScope.launch {
            repository.deletePoint(pointId)
        }
    }

    fun updatePrices(whitePrice: Double, greyPrice: Double, lavashPrice: Double) {
        viewModelScope.launch {
            repository.updatePrices(whitePrice, greyPrice, lavashPrice)
        }
    }

    fun updatePointName(pointId: Long, name: String) {
        viewModelScope.launch {
            repository.updatePointName(pointId, name)
        }
    }

    fun saveReturn(pointId: Long, whiteReturn: Int, greyReturn: Int, lavashReturn: Int) {
        viewModelScope.launch {
            repository.saveReturn(pointId, whiteReturn, greyReturn, lavashReturn)
        }
    }

    fun updateExpense(amount: Double) {
        viewModelScope.launch {
            repository.updateExpense(amount)
        }
    }
}

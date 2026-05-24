# Product Requirements Document (PRD) — Локальное Android-приложение "Хлебный Путь"

## Problem Statement

Водители-экспедиторы хлебобулочных изделий ежедневно сталкиваются с необходимостью развозить хлеб и лаваш по торговым точкам (магазинам). В процессе доставки возникают следующие проблемы:
1. **Нецелые лотки при погрузке:** Суммарный заказ по точкам редко кратен вместимости стандартного лотка (16 буханок). Поставщик грузит хлеб целыми лотками. Возникает расхождение между заказанным объемом и погруженным (излишек или дефицит). Водителям приходится на лету решать, кому додать хлеб, а у кого урезать, вручную пересчитывая суммы в бумажных накладных.
2. **Экстремальные условия работы:** Небольшие экраны старых Android-смартфонов водителей (до 5 дюймов), тряска, работа одной рукой за рулем или при разгрузке. Обычные интерфейсы с мелкими элементами ввода приводят к частым ошибкам.
3. **Отсутствие связи:** В отдаленных точках или полуподвальных помещениях магазинов отсутствует мобильная связь. Приложение должно работать абсолютно автономно без синхронизации во время рейса.

## Solution

Мобильное Android-приложение с локальной базой данных (SQLite/Room), оптимизированное для работы одной рукой на экранах до 5 дюймов:
1. **Экран утреннего умного округления:** При запуске рейса приложение выявляет остаток хлеба (если суммарный заказ не кратен 16 шт.) и предлагает водителю выбрать направление округления (в большую сторону — излишек, или в меньшую — дефицит). После этого водитель распределяет разницу по магазинам с помощью крупных кнопок `[+]` и `[-]` на одном экране до обнуления баланса.
2. **Интерфейс высокой плотности и доступности:** Использование плотных списков-аккордеонов (Accordion) вместо переходов по экранам. Большие touch-таргеты (высота кнопок не менее 48dp) и цифровая клавиатура для быстрого ввода сумм оплаты.
3. **Автономность (Full Offline):** База данных Room сохраняет все данные локально. Пересчет сумм происходит мгновенно на устройстве.
4. **Управление рейсами и магазинами:** Возможность сбросить текущий рейс, создать новый рейс, а также отдельный экран для управления списком магазинов (добавление новых точек).

---

## User Stories

1. **Запуск рейса и выявление расхождений:**
   As a driver, I want the app to check if the total bread order is a multiple of 16, so that I immediately know if there is a discrepancy with the loaded trays.
2. **Округление партии хлеба в большую сторону:**
   As a driver, I want to round up the total bread quantity to the nearest whole tray, so that I can take the full tray from the warehouse and distribute the surplus pieces to selected shops.
3. **Округление партии хлеба в меньшую сторону:**
   As a driver, I want to round down the total bread quantity to the nearest whole tray, so that I don't carry half-empty trays and can subtract the deficit pieces from selected shops down to 0 pieces on a point if necessary.
4. **Интерактивное распределение баланса:**
   As a driver, I want to see a dedicated screen showing all shops with `[+]` and `[-]` buttons next to their bread quantities, so that I can easily add/subtract bread pieces until the route balance is zero.
5. **Мгновенный перерасчет стоимости при округлении:**
   As a driver, I want the system to instantly update the calculated total sum of a shop's order as I adjust its bread quantity, so that I see the correct final price for the invoice.
6. **Сохранение измененных заказов:**
   As a driver, I want to save the adjusted orders and proceed to the main delivery screen only when the route bread balance is exactly zero, so that I don't leave the warehouse with unaccounted bread.
7. **Просмотр маршрута рейса:**
   As a driver, I want to see the sequence of shops (points) on the main screen in their delivery order, so that I know where to go next.
8. **Быстрый просмотр заказа через аккордеон:**
   As a driver, I want to tap on a shop in the list to expand its details (Accordion style) showing the quantity of bread, trays, lavash, and the total cost, so that I don't have to navigate to a new screen.
9. **Ввод оплаты с предупреждением о расхождении:**
   As a driver, I want to enter the payment amount manually; if it differs from the calculated sum, the app should save the entered amount, show a warning, display the calculated sum in parentheses, and allow closing the point as long as the payment is greater than 0 (no closing in debt).
10. **Контроль статуса точек и отмена закрытия:**
    As a driver, I want to see visual indicators (Pending/Completed) for each shop, and be able to reopen a completed shop if I made a mistake, reverting its status back to Pending.
11. **Управление рейсами (Создать / Сбросить):**
    As a driver, I want to be able to create a new route or reset the current route to clean up states and start a new delivery day.
12. **Добавление магазинов:**
    As a driver, I want to have a separate menu to add new shops to the system, so that I can handle new clients on the route.

---

## Implementation Decisions

### 1. Стек и Архитектура приложения
- **Платформа:** Android (Kotlin, Jetpack Compose, Room DB, Clean Architecture / MVVM).
- **Плотность UI:** Минимальная высота тач-зон кнопок — 48dp. Вместо переходов между экранами используется разворачивающийся список-аккордеон для каждой точки маршрута.
- **Константы:**
  - Цена хлеба (Белый/Серый): `47` руб./шт.
  - Вместимость лотка хлеба: `16` шт.
  - Цена лаваша: `52` руб./шт. (поставляется только поштучно).

### 2. Схема Локальной БД (Room DB)

```kotlin
// Сущность Точки (Магазина)
@Entity(tableName = "points")
data class Point(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sequenceOrder: Int,
    val status: String // "PENDING", "COMPLETED"
)

// Сущность Заказа (связана с Точкой 1-к-1)
@Entity(
    tableName = "orders",
    foreignKeys = [ForeignKey(
        entity = Point::class,
        parentColumns = ["id"],
        childColumns = ["point_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "point_id") val pointId: Long,
    @ColumnInfo(name = "bread_qty_pieces") val breadQtyPieces: Int,
    @ColumnInfo(name = "lavash_qty_pieces") val lavashQtyPieces: Int,
    @ColumnInfo(name = "calculated_sum") val calculatedSum: Double
)

// Сущность Оплаты (связана с Точкой 1-к-Многие для поддержки частичных оплат или разных типов платежей)
@Entity(
    tableName = "payments",
    foreignKeys = [ForeignKey(
        entity = Point::class,
        parentColumns = ["id"],
        childColumns = ["point_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "point_id") val pointId: Long,
    @ColumnInfo(name = "payment_type") val paymentType: String, // "CASH", "CARD"
    @ColumnInfo(name = "amount_paid") val amountPaid: Double,
    val timestamp: Long
)
```

### 3. Бизнес-логика оплат и статусов
- **Округление:** Водитель может уменьшать количество хлеба на точке до `0` шт. Ограничений на минимальный заказ нет.
- **Оплата:**
  - Сумма оплаты вносится вручную.
  - Точка не может быть закрыта с оплатой `0` рублей (закрытие в долг запрещено). Оплата должна быть `> 0`.
  - Если сумма оплаты меньше или больше расчетной суммы заказа (`amountPaid != calculatedSum`), выводится предупреждающее диалоговое окно ("Введенная сумма отличается от расчетной. Закрыть точку?"). При согласии точка закрывается.
  - В UI для закрытых точек отображается введенная водителем сумма оплаты, а расчетная стоимость заказа выводится рядом в скобках, например: **Оплачено: 1 500 руб. (Чек: 1 800 руб.)**.
- **Отмена закрытия:** Водитель может повторно открыть выполненную точку. При этом статус возвращается в `PENDING`, а соответствующая запись оплаты удаляется из таблицы `payments` (или помечается как отмененная).

---

## Testing Decisions
- **Модульное тестирование (Unit Tests):**
  - Тестирование класса `BreadRoundingCalculator` (округление лотков, распределение излишков/дефицита до нуля).
  - Тестирование валидатора оплаты (проверка запрета на 0 руб., выдача предупреждения при несовпадении сумм, сохранение фактически внесенной суммы).
- **Интеграционное тестирование базы данных (Room DB Tests):**
  - Тестирование добавления новых магазинов, сброса рейса, отмены закрытия точки (удаление оплаты, возврат статуса PENDING).

---

## Out of Scope
- Облачная синхронизация в реальном времени.
- Интеграция с NFC, QR-ридерами или GPS-трекером.
- Учет возвратов старого (черствого) хлеба.

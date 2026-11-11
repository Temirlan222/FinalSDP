# Стартовый контракт GiftBox

Контракт реализован в общем проекте со всеми шестью паттернами. Исходные интерфейсы сохранены. Изменения общих методов согласовывайте и проверяйте вместе.

## Общие интерфейсы

Файл `src/main/java/giftbox/api/Gift.java`:

```java
package giftbox.api;

public interface Gift {
    String getDescription();
    int getPrice();
}
```

Файл `src/main/java/giftbox/api/DiscountStrategy.java`:

```java
package giftbox.api;

public interface DiscountStrategy {
    int applyDiscount(int subtotal);
}
```

Цена — целое число тенге, не меньше нуля. `Gift#getPrice()` возвращает текущую цену подарка вместе с уже добавленным оформлением. Описание декоратора дополняет исходное описание: `", gift wrap"` для упаковки и `", ribbon"` для ленты.

## Strategy и расчёт цены

Готовые стратегии находятся в `giftbox.strategy`:

| Класс | Результат `applyDiscount(subtotal)` |
| --- | --- |
| `NoDiscount` | `subtotal` |
| `StudentDiscount` | `floor(subtotal * 90 / 100)` |
| `HolidayDiscount` | `floor(subtotal * 80 / 100)` |
| `ThresholdDiscount` | `subtotal < 5000 ? subtotal : subtotal - 500` |

`giftbox.pricing.PriceCalculator` предоставляет:

```java
public PriceCalculator(DiscountStrategy discountStrategy);
public void setDiscountStrategy(DiscountStrategy discountStrategy);
public int calculateSubtotal(List<? extends Gift> gifts);
public int calculateTotal(List<? extends Gift> gifts);
```

`List` — `java.util.List`. `calculateSubtotal` суммирует уже оформленные подарки. `calculateTotal` применяет выбранную стратегию ровно один раз к полученной сумме. Пустой список даёт 0. Калькулятор не изменяет список или подарки; одновременно несколько стратегий не складываются.

## Decorator

Классы находятся в `giftbox.decorator`. Базовый `GiftDecorator` реализует `Gift` и хранит обёрнутый `Gift`. `GiftWrap` добавляет 200 ₸, `Ribbon` — 100 ₸. Оба принимают `Gift` в конструкторе и сами остаются `Gift`, поэтому допустимо:

```java
Gift decorated = new Ribbon(new GiftWrap(originalGift));
```

Декораторы проверяют цену обёрнутого подарка. Сложение цен использует `Math.addExact`, чтобы переполнение не превращалось в неверную сумму.

## Ошибки и ограничения

- `null` вместо обязательного объекта, списка или элемента списка — `NullPointerException`.
- Отрицательная цена или отрицательная входная сумма — `IllegalArgumentException`.
- Результат пользовательской стратегии меньше 0 или больше исходной суммы — `IllegalArgumentException` при расчёте общей цены.
- Сумма, не помещающаяся в `int`, — `ArithmeticException`.

Все участники должны сохранять эти правила. При необходимости изменить `giftbox.api` сначала согласуйте изменение с командой и обновите все зависимые части в одном согласованном наборе изменений.

## Границы остальных частей

**Участник 2: Factory Method + Builder.** Создаваемые подарки реализуют `Gift`. В Factory Method конкретные подклассы создателя переопределяют метод создания продукта. Один `switch`, выбирающий класс подарка, сам по себе является Simple Factory и не доказывает реализацию Factory Method. Builder собирает целый набор; Decorator добавляет оформление отдельному подарку.

`giftbox.builder.GiftBox#getGifts(): List<Gift>` возвращает неизменяемый список. GiftBox.Builder: forRecipient(String), withMessage(String), addGift(Gift), build(). Имя: 1–60 символов после trim; сообщение: до 200; набор: 1–12 подарков. Копируется список, не сами Gift: встроенные продукты/декораторы неизменяемы, внешние реализации Gift должны сохранять стабильную цену/описание. Цены считает PriceCalculator.

**Observer:** OrderObserver.onStatusChanged(String orderId, OrderStatus status). Order.subscribe/unsubscribe управляют подпиской; advanceTo допускает CREATED → PACKING → READY. При создании события ещё нет. Повторная подписка того же объекта не дублируется. Подписчики сохраняют сообщения в памяти. Callback внешнего подписчика должен завершаться без исключения: изоляция сбоев подписчиков не реализована.

**Facade:** GiftBoxFacade.quote(recipient, message, selections, strategy) возвращает Quote без заказа. placeOrder с теми же аргументами возвращает OrderReceipt с Order и двумя подписчиками. GiftSelection(product, wrapped, ribbon) выбирает book/candle/tea и оформление. Quote содержит box/subtotal/total/saving. OrderReceipt.advanceTo(status) передаёт изменение издателю событий.

**HTTP:** POST /api/quote, /api/orders, /api/status, формат application/x-www-form-urlencoded. Для первых двух: recipient, message, discount=none/student/holiday/threshold, повторяющийся item=book:1:0 (тип:упаковка:лента). Для статуса: id и status=PACKING/READY. Ответы JSON; 400 ввод, 404 неизвестный заказ, 405 метод, 413 размер, 415 формат. Клиент отправляет выбор, а сервер заново считает цены. Запросы обслуживаются последовательно, только 127.0.0.1.

**Запуск:** giftbox.web.WebServer соединяет приложение и браузер. giftbox.demo.Demo оставлен как изолированная демонстрация двух паттернов с ExampleGift; общий web-сценарий использует фабрики.

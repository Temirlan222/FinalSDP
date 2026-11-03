# Стартовый контракт GiftBox

Это **предложенный стартовый контракт для командного согласования**. Он уже используется частью Strategy + Decorator. Перед реализацией остальных частей команда должна просмотреть его и принять либо согласованно изменить. Наличие этого файла не означает, что интерфейсы товарищей или все шесть паттернов уже готовы.

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

Для передачи набора калькулятору предлагается `GiftBox#getGifts(): List<Gift>`. Это предложение для обсуждения с владельцем Builder, а не уже реализованный интерфейс. Методы `GiftBox#getSubtotal()` и `GiftBox#getTotal()` не обязательны: расчёт цены уже принадлежит `PriceCalculator`.

**Участник 3: Observer + Facade.** События, статусы, методы подписки и внешний интерфейс Facade проектирует владелец этой части с последующим согласованием. Этот документ намеренно не фиксирует неподтверждённые методы и статусы.

**Вместе:** общий `Main`, соединение шести паттернов и итоговая демонстрация. `giftbox.demo.Demo` и его внутренний `ExampleGift` показывают только Strategy + Decorator и не являются готовой частью товарищей.

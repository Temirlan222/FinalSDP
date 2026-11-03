# FinalSDP — GiftBox

Командный консольный проект на Java: сборка подарочного набора, оформление подарков и расчёт скидки. Нужен **JDK 11 или новее**, сторонних библиотек нет.

В этой версии реализована часть первого участника: **Strategy и Decorator — 2 из 6 паттернов**. Factory Method, Builder, Observer и Facade остаются задачами команды; общий сценарий приложения будет собран после их реализации. Текущий `Demo` показывает только готовую часть.

## Запуск

Из папки проекта в PowerShell:

```powershell
# Компиляция
powershell -ExecutionPolicy Bypass -File .\build.ps1

# Компиляция и проверки
powershell -ExecutionPolicy Bypass -File .\build.ps1 -Test

# Компиляция и демонстрация
powershell -ExecutionPolicy Bypass -File .\build.ps1 -Run
```

Скрипт поддерживает выбор JDK через переменные `JDK_HOME` или `JAVA_HOME`. В IntelliJ IDEA установите Project SDK 11+; исходники находятся в `src/main/java`, проверки — в `test`. Сборка использует `--release 11`.

## Что реализовано

- **Decorator:** `GiftWrap` добавляет упаковку за 200 ₸, `Ribbon` — ленту за 100 ₸. Декораторы можно вкладывать друг в друга.
- **Strategy:** `NoDiscount`, `StudentDiscount` (10%) и `HolidayDiscount` (20%). Стратегия меняется во время работы через `PriceCalculator`.
- **Расчёт:** сначала суммируются подарки с оформлением, затем скидка применяется один раз ко всей сумме. Цена хранится целым числом тенге, результат скидки округляется вниз.

Пример: книга 1500 ₸ + упаковка 200 ₸ + лента 100 ₸ + свеча 800 ₸ = **2600 ₸**. Со студенческой скидкой — **2340 ₸**, с праздничной — **2080 ₸**. Простой `ExampleGift` внутри демонстрации служит только примером, работа товарища над Factory Method ещё не заменена готовой реализацией.

## Структура

```text
src/main/java/giftbox/
  api/          Gift, DiscountStrategy
  decorator/    GiftDecorator, GiftWrap, Ribbon
  strategy/     NoDiscount, StudentDiscount, HolidayDiscount
  pricing/      PriceCalculator
  demo/         Demo
test/giftbox/   PartTests
build.ps1
CONTRACT.md
TEAM.md
```

[CONTRACT.md](CONTRACT.md) описывает стартовый контракт и границы интеграции. [TEAM.md](TEAM.md) — распределение работы и порядок совместных изменений. [MY_PART.md](MY_PART.md) — разбор твоих классов и сценарий защиты.

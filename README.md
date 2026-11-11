# GiftBox Studio — FinalSDP

Java-приложение с браузерным интерфейсом: выбор подарков, упаковка и лента, сборка бокса, скидки, оформление заказа и уведомления. Все **6 паттернов** работают в одном проекте. Группа **SE-2532**: Saparali Shalkar, Nurbayev Murager, Mukhametov Temirlan.

## Запуск

Нужен **JDK 11+**; npm, Maven, база данных и сторонние Java-библиотеки не нужны.

Windows: открой `start.cmd` или выполни из папки проекта:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ./build.ps1 -Web
```

Открой **http://127.0.0.1:8080**. Сервер работает только на этом компьютере. Для остановки нажми Ctrl+C в его терминале. Если JDK не найден, установи `JDK_HOME` или `JAVA_HOME` в папку JDK 11+. Порт можно поменять: `build.ps1 -Web -Port 8081`.

Linux/macOS: `sh build.sh web`. IntelliJ: JDK 11+, каталог `src/main/java` — Sources Root; главный класс `giftbox.web.WebServer`, рабочая папка — корень проекта.

## Проверка

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ./build.ps1 -Test -Run
# При запущенном сервере; нужен Python 3:
python test/http_smoke.py
```

24 Java-проверки: 12 твоей части и 12 общей интеграции. Ещё 20 HTTP-проверок. На Linux/macOS: `sh build.sh test`.

## Демонстрация за 3 минуты

1. **Try a sample box**: книга с упаковкой и лентой + свеча, subtotal **2600 ₸**, Student **2340 ₸**.
2. **No discount / Holiday**: **2600 / 2080 ₸**. Сними/добавь ленту: сумма пересчитывается Java-кодом.
3. **Big box**, добавь два Tea: subtotal **5000 ₸**, total **4500 ₸**. Убери один Tea: **3800 → 3800 ₸**, порог не достигнут.
4. **Create my gift box → Start packing → Mark ready**: два подписчика получают PACKING и READY.
5. **Create another box** разблокирует редактирование. Пустой бокс или пустое имя оформить нельзя.

## Паттерны

| Паттерн | Реализация в src/main/java/giftbox/ | Что показать |
|---|---|---|
| Factory Method | factory/GiftCreator, BookCreator, CandleCreator, TeaCreator | Переопределения createProduct() |
| Builder | builder/GiftBox.Builder | Пошаговая сборка, проверка, копия списка |
| Decorator | decorator/GiftDecorator, GiftWrap, Ribbon | Обёртки вокруг любого Gift |
| Strategy | api/DiscountStrategy, strategy/*, pricing/PriceCalculator | Процентный и пороговый алгоритмы |
| Observer | observer/Order, OrderObserver, CustomerNotifications, PackingDesk | Подписка/отписка и изменение статуса |
| Facade | facade/GiftBoxFacade | Один вызов связывает создание, оформление, сборку и расчёт |

Интерфейс `web/` обращается к Java по HTTP; JavaScript не рассчитывает конечную цену. `web/WebServer` — транспортный слой, не седьмой заявленный паттерн.

## Что сдавать

- `FinalSDP-project.zip`: исходники и запуск.
- `docs/FinalSDP_Report.pdf`: английский отчёт с GitHub, кодом, UML и настоящими скриншотами.
- `docs/FinalSDP_UML.pdf` и `docs/uml/`: отдельные диаграммы и редактируемые PlantUML-источники.
- `docs/FinalSDP_Checklist.pdf`: самопроверка требований и доказательства.

[Контракт](CONTRACT.md) · [Команда](TEAM.md) · [Твоя часть и защита](MY_PART.md)

GitHub: https://github.com/Temirlan222/FinalSDP. Репозиторий приватный: преподавателю и товарищам нужен доступ. Сама ссылка не выдаёт доступ.

## Ограничения

Локальная учебная демонстрация без оплаты, аккаунтов и доставки. Заказы и уведомления хранятся в памяти, сбрасываются при остановке; максимум 1000 заказов за запуск. Настоящие письма/SMS не отправляются. HTTP-запросы обрабатываются последовательно. Для публичного размещения потребуются хранение, доступ и эксплуатационная защита.

Сборка проверена JDK 25 с `--release 11`. Отдельный запуск на JDK 11 и Linux/macOS не проверялся. API совместимы с Java 11.

Готовый код не подтверждает индивидуальную реализацию двух паттернов каждым студентом. Команда должна подтвердить личный вклад своими изменениями и пониманием на защите. Не выдавайте автоматическую подготовку за историю самостоятельной работы.

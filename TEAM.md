# Команда SE-2532

## Зоны ответственности

| Участник | Паттерны | Пакеты |
|---|---|---|
| Mukhametov Temirlan | Strategy + Decorator | strategy, decorator, pricing, api |
| Saparali Shalkar | Factory Method + Builder | factory, builder |
| Nurbayev Murager ([@muryashin](https://github.com/muryashin)) | Backend, Orders & Integration: Observer + Facade, Java HTTP Server | observer, facade, giftbox.web |

Браузерный интерфейс в корневом каталоге `web/`, отчёт и UML — общие задачи команды. Интеграция согласуется всей командой; серверную часть интеграции сопровождает @muryashin в пределах обязанностей ниже. Ответственность остальных участников за их паттерны и общие интерфейсы сохраняется.

## Backend, Orders & Integration — @muryashin

По согласованному распределению обязанностей Nurbayev Murager отвечает за:

- **Observer и заказы:** `src/main/java/giftbox/observer/`, включая `Order`, `OrderStatus`, подписку/отписку, `CustomerNotifications` и `PackingDesk`, переходы `CREATED → PACKING → READY`.
- **Facade и серверную интеграцию:** `src/main/java/giftbox/facade/`, включая `GiftBoxFacade`, `GiftSelection`, `Quote` и `OrderReceipt`. Фасад соединяет подсистемы команды; ответственность за Factory Method, Builder, Strategy и Decorator остаётся у указанных выше участников.
- **Java HTTP Server и API:** `src/main/java/giftbox/web/WebServer.java`, `Json.java`, маршруты `/api/quote`, `/api/orders`, `/api/status`, обработку запросов, JSON-ответов и ошибок. Это HTTP API; общие интерфейсы `giftbox.api` не передаются в эту зону ответственности. Корневой каталог `web/` содержит общий браузерный интерфейс и также не передаётся @muryashin единолично.
- **Соответствующие тесты:** `test/http_smoke.py` и серверные проверки в `test/giftbox/ProjectTests.java`: `twoObservers`, `unsubscribe`, `statusValidation`, `integration`, `independentOrders`, `badRequests`, `quoteOnly` и их вспомогательный `sample`. Остальные проверки Factory/Builder/Strategy и общий тестовый код сохраняют командную ответственность; `test/giftbox/PartTests.java` не относится к этой серверной зоне.

В `.github/CODEOWNERS` указаны три серверных пакета и HTTP-тесты для назначения ответственного за проверку изменений. Общий `ProjectTests.java` не назначен одному владельцу: CODEOWNERS работает с файлами, а не отдельными методами. CODEOWNERS не меняет авторство старых строк, историю Blame или список Contributors.

Первоначальная общая версия проекта подготовлена с помощью ИИ. [PR #1](https://github.com/Temirlan222/FinalSDP/pull/1) содержит явно обозначенную реконструкцию истории согласно распределению ответственности; её даты отражают первоначальную загрузку, а не время самостоятельного написания каждой строки. Эта таблица описывает согласованные обязанности и не является доказательством авторства всех строк.

## Начало работы

После получения доступа к репозиторию:

```text
git clone https://github.com/Temirlan222/FinalSDP.git
cd FinalSDP
git switch -c feature/short-description
```

Прочитайте [README](README.md) и [контракт](CONTRACT.md). Запустите Java-проверки и приложение по инструкции в README.

## Внесение изменений

1. Перед новой задачей обновите `main` и создайте отдельную ветку.
2. Согласуйте изменения общих интерфейсов и HTTP API с остальными участниками.
3. Измените код и соответствующие тесты; при изменении структуры обновите UML и отчёт.
4. Запустите Java-проверки. При изменении HTTP API или интерфейса проверьте также HTTP-сценарии и работу в браузере.
5. Отправьте ветку и откройте pull request с описанием результата и проверок. Перед объединением попросите другого участника проверить изменения.

## Перед сдачей

- Проверить имена и группу в отчёте.
- Убедиться, что преподаватель имеет доступ к GitHub.
- Проверить запуск проекта из чистой копии.
- Подготовить ZIP проекта, PDF-отчёт и UML.
- Каждому участнику подготовить демонстрацию своих двух паттернов и их взаимодействия с остальной системой.

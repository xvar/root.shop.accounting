# store-service

Сервис для небольших магазинов (остатки, резервирование, QR-коды). Демо-стадия.

Полное описание идеи, сущностей, дорожной карты и открытых вопросов — в
[`docs/design.md`](docs/design.md). Инструкция по хостингу демо — в
[`docs/hosting-oracle-free-tier.md`](docs/hosting-oracle-free-tier.md).

## Структура

- `server/` — backend, Kotlin + Ktor.
- `shared/` — общая Kotlin Multiplatform логика/модели для сервера и клиентов.
- `composeApp/` — общий Compose Multiplatform UI (Android/iOS/Desktop/Web).
- `androidApp/` — точка входа Android-приложения (обязательно отдельный модуль
  начиная с AGP 9, см. `docs/design.md` §5).

## Быстрый старт

```bash
# backend локально
./gradlew :server:run

# desktop-клиент (Compose) для быстрой проверки UI без Android Studio
./gradlew :composeApp:desktopRun -DmainClass=root.shop.accounting.app.MainKt --quiet

# тесты (JVM-таргеты)
./gradlew :shared:jvmTest :server:test :composeApp:desktopTest

# всё в докере (сервер + Postgres)
docker compose up --build
```

# MCP‑доступ к бюджетам: план реализации

Статус: план согласован с автором, код не менялся. Дата: 2026-09-05.

Документ написан как задание на реализацию. Все решения уже приняты, вопросов к автору не осталось; там, где исполнителю нужно принять решение самостоятельно, это сказано явно.

## 0. Принятые решения (кратко)

| Тема | Решение |
|---|---|
| Уровень сервера | Один MCP‑сервер на всё приложение, живёт в `ManageModel`. Все инструменты принимают `budget_id`. |
| Ресурсы vs инструменты | Только инструменты (tools). Ресурсы `echo://` и тестовый tool `echo` удалить. |
| Транзакции | Наружу не отдаются. Все данные плоские: **запись** (record). Перевод превращается в две записи без категории. |
| Комментарии | Комментарий транзакции наружу не отдаётся вообще. У записи есть только её собственный комментарий; у записей перевода комментария нет (`null`). |
| Фильтр `direction` | Три значения: `credit`, `debit`, `transfer`. Обычно нужно одно из них. |
| Пагинация | `offset` + `limit`, в ответе `total` и `has_more`. |
| Аналитика | Возвращается **таблица**: строки — календарные периоды внутри внешних границ `date_min..date_max`; столбцы — группы (категории / счета / нет). У каждой строки и у всей таблицы есть `sum_debit`, `sum_credit`, `sum`. Агрегация `sum` (с флагом `incremental`) или `average` (с подпериодом). LLM ничего не должна досчитывать. |
| Аутентификация | Оставить как есть (без неё). |
| Переиспользование | Слой запросов не знает про MCP и спрятан за интерфейсами `BudgetsQuery` (список бюджетов, доступ по `budgetId`) и `BudgetQuery` (данные одного бюджета). MCP‑адаптер зависит только от них. Далее те же интерфейсы будут использоваться для Android AppFunctions и для аналитики самого приложения; эти два пункта **в скоуп не входят**. |

## 1. Что есть сейчас

### 1.1. Заготовка сервера

[BudgetMCPModel.kt](../model/src/commonMain/kotlin/org/hnau/pinfin/model/budget/manage/BudgetMCPModel.kt) — поднимает Ktor CIO на `::`, порт `8080`, путь `/mcp`, транспорт `mcpStreamableHttp` из `io.modelcontextprotocol:kotlin-sdk-server` 0.15.0. Зарегистрированы тестовые `echo`‑ресурс и `echo`‑инструмент. Флаг `mcpIsEnabled` хранится в `Skeleton`, сервер запускается через `collectLatest` при `true`.

UI: [BudgetMCPProjector.kt](../projector/src/commonMain/kotlin/org/hnau/pinfin/projector/budget/manage/BudgetMCPProjector.kt) — чекбокс и список адресов (через `konnection`). Встроен в [BudgetManageProjector.kt](../projector/src/commonMain/kotlin/org/hnau/pinfin/projector/budget/manage/BudgetManageProjector.kt) ячейкой `"mcp"`.

### 1.2. Почему текущее размещение не годится

`BudgetMCPModel` создаётся в [BudgetManageModel.kt](../model/src/commonMain/kotlin/org/hnau/pinfin/model/budget/manage/BudgetManageModel.kt), а тот — вкладка `BudgetModel`:

1. Вкладки создаются лениво (`BudgetModel.getModel`): после перезапуска приложения на другой вкладке сервер не стартует, хотя флаг включён.
2. `ManageModel.state` строится через `mapWithScope` от выбранного бюджета — при переключении бюджета scope умирает и сервер останавливается.
3. Флаг — per‑budget, а порт один. Два включённых бюджета = два `bind` на 8080.

### 1.3. Что переиспользуем

- Flatten уже реализован: [AnalyticsEntry.kt](../model/src/commonMain/kotlin/org/hnau/pinfin/model/utils/analytics/AnalyticsEntry.kt), `TransactionInfo.toAnalyticsEntries(currency)`. Перевод → две записи: `from` с направлением `Debit`, `to` с направлением `Credit`. Но в `AnalyticsEntry` нет ни id транзакции, ни комментария, поэтому для API нужен свой DTO (см. §3.1); логику разворачивания копируем оттуда.
- Периоды: [AnalyticsPeriod.kt](../model/src/commonMain/kotlin/org/hnau/pinfin/model/utils/analytics/period/AnalyticsPeriod.kt) (`Whole / Months / Years / Days`), [AnalyticsPeriodExt.kt](../model/src/commonMain/kotlin/org/hnau/pinfin/model/utils/analytics/period/AnalyticsPeriodExt.kt) (`periodContaining`, `periods(range)`), [PeriodDurationExt.kt](../model/src/commonMain/kotlin/org/hnau/pinfin/model/utils/analytics/period/PeriodDurationExt.kt) (`PeriodDuration.subperiodsOf(period)`). Используем как есть.
- Правило «среднего по полным подпериодам» — в `calcGroupAmount` в [AnalyticsCalculator.kt](../model/src/commonMain/kotlin/org/hnau/pinfin/model/utils/analytics/AnalyticsCalculator.kt). Повторяем его дословно (см. §4.4). Сам `calcPeriod` **не** переиспользуем: он схлопывает направления в одно нетто и возвращает UI‑структуры (`Filters`).
- Деление: `Amount.div(divisor: Int, scale: DecimalScale)` в [Amount.kt](../data/src/commonMain/kotlin/org/hnau/pinfin/data/Amount.kt) — безопасное, использовать только его.
- Суммирование: `Iterable<Amount>.sum()` там же.
- `BudgetsStorage.list: StateFlow<List<KeyValue<BudgetId, BudgetRepository>>>` — источник списка бюджетов. Название бюджета: `repository.state.value.info.title`.
- `BudgetState` ([BudgetState.kt](../model/src/commonMain/kotlin/org/hnau/pinfin/model/utils/budget/state/BudgetState.kt)): `transactions`, `categories`, `accounts`, `info.currency`.

### 1.4. Ограничения проекта (обязательно)

- **Сохранённое состояние.** `RootModel.Skeleton` сериализуется в JSON целиком, при ошибке десериализации приложение падает на старте. Есть `ignoreUnknownKeys`. Правило: старые ключи не переименовывать, новые поля — только с дефолтами. Удалять поле из `Skeleton` можно (старый ключ будет проигнорирован).
- **`@Pipe`.** Интерфейсы `Dependencies` реализуются KSP‑генератором из commons. Листовые `val` протаскиваются по имени вниз по дереву от места, где они появились (как параметр `impl(...)` корня или параметр функции вроде `withOpener(budgetOpener)`). Новое листовое значение нужно либо добавить в `RootModel.Dependencies.impl(...)` в `app/.../PinFinAppSeed.kt`, либо ввести параметром функции на нужном уровне. Сгенерированный код смотреть в `model/build/generated/ksp/metadata/commonMain/kotlin/...`.
- **Код‑стиль.** `@Fold` для sealed/enum и `fold(...)` вместо `when`; `StateFlow` + `mapState`; `KeyValue` вместо `Pair`; `MutableStateFlow` в скелетах для сохраняемого состояния.
- Даты — `kotlinx.datetime.LocalDate` / `LocalDateRange`, без времени суток и без системных часов в модели.
- Тесты модуля `model`: `./gradlew :model:jvmTest`. Существующие тесты — `model/src/commonTest/kotlin/org/hnau/pinfin/model/utils/analytics/`.
- Строки для LLM (имена и описания инструментов, ключи JSON) — на английском. Строки UI — в `Localization.kt`, по‑русски.

## 2. Целевая архитектура

Слои, зависимости только сверху вниз:

```
projector: BudgetMCPProjector (чекбокс + адреса)                 — без изменений по сути
model:     MCPModel + MCPTools (Ktor + MCP SDK, регистрация tools) — тонкий адаптер: JSON ⇄ DTO, вызовы BudgetsQuery/BudgetQuery
model:     utils/budget/query/  BudgetsQuery, BudgetQuery (интерфейсы) + @Serializable DTO   — контракт, не знает про MCP
model:     utils/budget/query/impl/  BudgetsStorageQuery, BudgetStateQuery (реализации над BudgetsStorage / BudgetState)
model:     utils/budget/query/calc/  чистые функции: flatten, фильтр, таблица аналитики, метаданные
```

### 2.0. Абстракция доступа к данным

```kotlin
/** Точка входа: все бюджеты приложения. */
interface BudgetsQuery {
    suspend fun budgets(): List<BudgetSummary>
    /** null — бюджета с таким id нет. */
    suspend fun budget(id: BudgetId): BudgetQuery?
}

/** Данные одного бюджета. Реализация сама уходит на Dispatchers.Default. */
interface BudgetQuery {
    val id: BudgetId
    suspend fun metadata(): BudgetMetadata
    suspend fun records(filter: RecordsFilter?, offset: Int, limit: Int): RecordsPage
    suspend fun analytics(filter: RecordsFilter?, period: PeriodSpec, groupBy: GroupBy?, aggregation: Aggregation): AnalyticsTable
}
```

Правила:

- Потребители (`MCPTools`, позже AppFunctions) видят **только** эти два интерфейса и DTO. Ни `BudgetsStorage`, ни `BudgetRepository`, ни `BudgetState` наружу из пакета `query` не утекают.
- `suspend` намеренно: реализация делает `withContext(Dispatchers.Default)` и берёт `state.value` внутри; адаптерам не нужно думать о потоках.
- Реализации: `BudgetsStorageQuery(budgetsStorage: BudgetsStorage) : BudgetsQuery` и `BudgetStateQuery(id: BudgetId, state: StateFlow<BudgetState>) : BudgetQuery`. Вторая создаётся первой на каждый вызов `budget(id)` — это дёшево (обёртка над `StateFlow`), кэшировать не нужно.
- Чистые функции из `calc/` — `internal`; вызываются только из `BudgetStateQuery`. Тесты пишутся через `BudgetStateQuery` (публичный контракт), а не через `calc/` напрямую.

### 2.1. Пакет `org.hnau.pinfin.model.utils.budget.query`

Новая директория `model/src/commonMain/kotlin/org/hnau/pinfin/model/utils/budget/query/`. Ни одной ссылки на `io.modelcontextprotocol.*` или `io.ktor.*`.

Корень пакета — контракт (интерфейсы и DTO):

| Файл | Содержимое |
|---|---|
| `BudgetsQuery.kt` | Интерфейс `BudgetsQuery` (§2.0). |
| `BudgetQuery.kt` | Интерфейс `BudgetQuery` (§2.0). |
| `SignedAmount.kt` | Знаковая сумма (`BigDecimal`), сериализуется строкой `"-123.45"`. |
| `Totals.kt` | `Totals(sumDebit: Amount, sumCredit: Amount)` + вычисляемое `sum: SignedAmount`, `plus`, `zero`, `div`. |
| `FlatRecord.kt` | DTO записи + `RecordDirection`. |
| `RecordsFilter.kt` | DTO фильтра + `RecordsFilterDirection`. |
| `RecordsPage.kt` | DTO страницы + константы `MAX_RECORDS_LIMIT`, `DEFAULT_RECORDS_LIMIT`. |
| `AnalyticsRequest.kt` | `PeriodSpec`, `GroupBy`, `Aggregation`, `SubperiodSpec`, `SubperiodUnit`. |
| `AnalyticsTable.kt` | DTO результата. |
| `BudgetMetadata.kt` | `BudgetSummary`, `BudgetMetadata`, `CategoryMeta`, `AccountMeta`, `CurrencyMeta`. |
| `QueryJson.kt` | Единый `Json` для всех DTO (см. §3.7). |

Подпакет `query/impl` — реализации контракта:

| Файл | Содержимое |
|---|---|
| `BudgetsStorageQuery.kt` | `class BudgetsStorageQuery(budgetsStorage: BudgetsStorage) : BudgetsQuery`. |
| `BudgetStateQuery.kt` | `class BudgetStateQuery(id: BudgetId, state: StateFlow<BudgetState>) : BudgetQuery` — `withContext(Dispatchers.Default)` + вызовы `calc/`. |

Подпакет `query/calc` — `internal` чистые функции над `BudgetState`:

| Файл | Содержимое |
|---|---|
| `FlatRecordsExt.kt` | `BudgetState.flatRecords(newestFirst: Boolean = false): List<FlatRecord>`. |
| `RecordsFilterExt.kt` | `FlatRecord.matches(filter: RecordsFilter?): Boolean`. |
| `QueryRecords.kt` | `fun queryRecords(state, filter, offset, limit): RecordsPage`. |
| `PeriodSpecExt.kt` | `PeriodSpec.toAnalyticsPeriod()`, `SubperiodSpec.toPeriodDuration()` с валидацией. |
| `AnalyticsTableCalculator.kt` | `fun calcAnalyticsTable(state, filter, period, groupBy, aggregation): AnalyticsTable`. |
| `BudgetMetadataExt.kt` | `BudgetState.metadata(id): BudgetMetadata`. |

### 2.2. Пакет `org.hnau.pinfin.model.mcp`

| Файл | Содержимое |
|---|---|
| `MCPModel.kt` | Переезд `BudgetMCPModel` с переименованием. Скелет, запуск сервера, `Dependencies { val budgetsQuery: BudgetsQuery }`. |
| `MCPTools.kt` | `fun Server.registerPinFinTools(budgetsQuery: BudgetsQuery)` — четыре инструмента, разбор аргументов, формирование `CallToolResult`. Никаких обращений к `BudgetsStorage`/`BudgetState`. |
| `JsonSchema.kt` | Маленькие хелперы для сборки JSON Schema через `buildJsonObject` (см. §3.6). |

Старый файл `model/.../budget/manage/BudgetMCPModel.kt` удалить.

### 2.3. Размещение в дереве моделей

```
LoadBudgetsModel
└─ ManageModel                       ← здесь создаются BudgetsStorageQuery и MCPModel (scope живёт всё время работы приложения)
   ├─ MCPModel                       ← новый, зависит только от BudgetsQuery
   └─ state: BudgetRootModel / CreateBudgetModel  (scope пересоздаётся при смене бюджета)
      └─ BudgetModel
         └─ BudgetManageModel        ← получает готовый MCPModel через Dependencies, только чтобы показать чекбокс
```

## 3. Спецификация API

Четыре инструмента. Все — read‑only (`ToolAnnotations(readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false)`).

Общие правила:

- Даты — ISO `YYYY-MM-DD`, границы включительные.
- Суммы — строки с десятичной точкой: `Amount` отдаётся своим сериализатором (`toStringExpanded`), `SignedAmount` — со знаком (`"-15.00"`). Никаких JSON‑чисел для денег.
- Идентификаторы — строки как есть: `budget_id` — UUID; `account` — `AccountId.id`; `category` — `CategoryId.id` (первый символ `+` = доход/credit, `-` = расход/debit, например `"-Food"`); `transaction_id` — UUID.
- Ключи JSON — `snake_case` (через `@SerialName`).
- Ошибки — `CallToolResult(isError = true, content = [TextContent("…")])` с понятным сообщением; исключения из `Json.decodeFromJsonElement` и `require(...)` перехватываются одним `runCatching` в обёртке (см. §5.3).
- Ответ — `CallToolResult(content = [TextContent(json)], structuredContent = jsonObject)`: тот же JSON и текстом, и структурно.

### 3.1. Запись (`FlatRecord`)

```json
{
  "transaction_id": "9f1c…",
  "date": "2026-08-14",
  "account": "card",
  "category": "-Food",
  "direction": "debit",
  "amount": "1250.00",
  "comment": "lunch",
  "transfer_counterpart_account": null
}
```

- `direction` — движение денег относительно `account`: `debit` — списание, `credit` — поступление. Для записи‑перевода на счёте `from` — `debit`, на `to` — `credit` (как в `toAnalyticsEntries`).
- `category` — `null` тогда и только тогда, когда запись получена из перевода.
- `transfer_counterpart_account` — второй счёт перевода; `null` для обычных записей.
- `comment` — комментарий записи (`Record.comment`), пустая строка отдаётся как `null`. Для переводов всегда `null`.
- `amount` — `AmountExpression.toAmount(currency.scale)`.

### 3.2. Фильтр (`RecordsFilter`)

Все поля опциональны; отсутствие = без ограничения.

| Поле | Тип | Семантика |
|---|---|---|
| `date_min`, `date_max` | date | `date_min ≤ date ≤ date_max` |
| `query` | string | регистронезависимое вхождение подстроки в `comment`; записи с `comment == null` не проходят |
| `amount_min`, `amount_max` | decimal string | `amount_min ≤ amount ≤ amount_max` |
| `categories` | string[] | `category ∈ categories`; записи‑переводы не проходят |
| `accounts` | string[] | `account ∈ accounts` (у перевода это счёт данной половины, `transfer_counterpart_account` не учитывается) |
| `direction` | `"credit" \| "debit" \| "transfer"` | `credit`/`debit` — только записи с категорией (не переводы) соответствующего направления; `transfer` — только записи‑переводы (обеих половин) |

Пустой массив в `categories`/`accounts` трактовать как «фильтр не задан» (эквивалент `null`), чтобы LLM не получала пустой результат из‑за `[]`.

### 3.3. Инструмент `list_budgets`

Вход: без параметров. Выход:

```json
{ "budgets": [ { "id": "…", "title": "Семья" } ] }
```

Источник: `BudgetsQuery.budgets()`; реализация берёт `budgetsStorage.list.value`, title из `repository.state.value.info.title`. Порядок — как в `list`.

Описание для LLM: `Lists all budgets available in the PinFin app. Use the returned id as budget_id for the other tools.`

### 3.4. Инструмент `get_budget_metadata`

Вход: `{ "budget_id": string }` (required). Выход:

```json
{
  "id": "…",
  "title": "Семья",
  "currency": { "scale": 2 },
  "transactions_count": 812,
  "records_count": 1034,
  "first_date": "2024-01-03",
  "last_date": "2026-09-04",
  "categories": [ { "id": "-Food", "title": "Еда", "direction": "debit" } ],
  "accounts":   [ { "id": "card", "title": "Карта", "balance": "15230.50", "hide_if_zero": false } ]
}
```

- `first_date`/`last_date` — по всем транзакциям, `null` если транзакций нет.
- `records_count` — размер `flatRecords()`.
- `categories` — все из `BudgetState.categories`, отсортированы по `title`; `direction` = `CategoryId.direction`.
- `accounts` — все из `BudgetState.accounts` (включая скрытые), отсортированы по `title`; `balance` — `SignedAmount` из `AccountInfo.amount` (`KeyValue<AmountDirection, Amount>`: credit → `+`, debit → `−`).

Описание: `Returns metadata of a budget: currency scale, date range of data, counts, and the full lists of categories and accounts (ids, titles, directions/balances). Call this before filtering or grouping to learn valid category and account ids.`

### 3.5. Инструмент `list_records`

Вход:

```json
{
  "budget_id": "…",
  "filter": { …RecordsFilter… },
  "offset": 0,
  "limit": 50
}
```

`budget_id` required; `filter` optional; `offset ≥ 0` (default 0); `1 ≤ limit ≤ 200` (default 50; больше 200 — обрезать до 200, не ошибка).

Выход:

```json
{
  "records": [ …FlatRecord… ],
  "total": 1034,
  "offset": 0,
  "limit": 50,
  "has_more": true
}
```

Порядок: `date` по убыванию, затем порядок транзакций в бюджете **в обратном порядке** (как `TransactionsModel`: `transactions.asReversed()`), внутри транзакции — порядок записей (для перевода: сначала `from`, потом `to`). `total` — число записей после фильтра. `has_more = offset + records.size < total`.

Описание: `Returns a page of flat money records of a budget, newest first. Every record is a single movement of money on one account. A transfer between accounts appears as two records without category (debit on the source account, credit on the destination), linked by transaction_id. Use filter to narrow down; use offset/limit to paginate (limit max 200).`

### 3.6. Инструмент `get_analytics`

Вход:

```json
{
  "budget_id": "…",
  "filter": { …RecordsFilter… },
  "period": { "type": "months", "count": 1, "start_day": 1 },
  "group_by": "category",
  "aggregation": { "type": "sum", "incremental": false }
}
```

- `filter` optional. `date_min`/`date_max` задают **внешние границы таблицы**; остальные поля фильтруют записи.
- `period` (`PeriodSpec`) optional, default `{"type":"whole"}`. Варианты (маппятся 1:1 в `AnalyticsPeriod`):
  - `{"type":"whole"}` — одна строка на весь диапазон;
  - `{"type":"months","count":N,"start_day":D}` — N месяцев, начиная с числа D (1..31, для короткого месяца — последний день). Месяц = `count 1`, квартал = `count 3`;
  - `{"type":"years","count":N,"start_month":M,"start_day":D}` — M 1..12;
  - `{"type":"days","count":N,"anchor":"YYYY-MM-DD"}` — N дней, одна из границ = anchor (неделя с понедельника = `count 7` + любой понедельник).
- `group_by` optional: `"category" | "account" | null` (default `null`).
- `aggregation` optional, default `{"type":"sum","incremental":false}`:
  - `{"type":"sum","incremental":Boolean}`;
  - `{"type":"average","subperiod":{"count":N,"unit":"day"|"month"|"year"}}` → `PeriodDuration`.
  - `incremental` допустим только для `sum`; `average` с `incremental` — невозможен по схеме (разные объекты).

Выход:

```json
{
  "range": { "start": "2026-01-01", "end": "2026-09-04" },
  "group_by": "category",
  "aggregation": { "type": "sum", "incremental": false },
  "periods": [
    {
      "start": "2026-01-01",
      "end": "2026-01-31",
      "partial": false,
      "groups": {
        "-Food":   { "sum_debit": "12000.00", "sum_credit": "0.00", "sum": "-12000.00" },
        "+Salary": { "sum_debit": "0.00", "sum_credit": "90000.00", "sum": "90000.00" }
      },
      "sum_debit": "12000.00",
      "sum_credit": "90000.00",
      "sum": "78000.00"
    }
  ],
  "sum_debit": "…",
  "sum_credit": "…",
  "sum": "…"
}
```

- `range` — фактические внешние границы (§4.3). Если подходящих записей нет: `range: null`, `periods: []`, итоги нули.
- `periods` — календарные периоды, не обрезанные по `range` (как в приложении: строка «10 авг – 9 сен» так и называется). `partial = true`, если период выходит за `range` хотя бы с одной стороны.
- `groups` — присутствует только при `group_by != null`. Ключ — id категории (для переводов: `"transfer"`, поскольку у них нет категории) или id счёта. Группы с нулевыми `sum_debit` и `sum_credit` в строке **опускаются**.
- `sum_debit`, `sum_credit`, `sum` строки — итоги по всем записям строки без группировки (`sum = sum_credit − sum_debit`). Итоги таблицы — по всему `range`.
- При `incremental = true` значения строки `i` (и групп, и итогов строки) — накопленная сумма по строкам `0..i`. Итоги таблицы — обычные (равны последней строке).
- При `average` значения строки — среднее по подпериодам внутри периода (§4.4). Итоги таблицы при `average` — среднее по подпериодам всего `range` (та же формула, где «период» = `range`).

Описание: `Builds an analytics table for a budget. Rows are calendar periods (whole range, N months from a start day, N years, or N days from an anchor) laid over the date bounds of the filter; columns are groups (categories, accounts, or none). Each cell and each row carries sum_debit (expenses/outflow), sum_credit (income/inflow) and sum (credit minus debit). aggregation "sum" totals the records (set incremental=true to get running totals, e.g. how savings on an account grow); "average" divides each period into subperiods and returns the average per subperiod (e.g. average per month within each year). All arithmetic is done server-side; do not recompute.`

### 3.7. JSON‑конфигурация

`QueryJson.kt`:

```kotlin
val queryJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false     // отсутствующее поле = null при чтении; null при записи не пишем, КРОМЕ явно нужных
    encodeDefaults = true
    classDiscriminator = "type"
}
```

Замечание: поля, которые по контракту должны быть в ответе даже при `null` (`category`, `comment`, `transfer_counterpart_account`, `first_date`, `last_date`, `range`, `group_by`), пометить `@EncodeDefault` **или** отказаться от `explicitNulls = false` и вместо этого дать всем опциональным полям входных DTO дефолт `= null`. Исполнителю выбрать второе: `explicitNulls = true` (дефолт), все опциональные поля входных DTO с `= null` — тогда и вход читается без обязательных `null`, и выход всегда полный.

### 3.8. JSON Schema инструментов

Схемы описываются руками через `buildJsonObject` (SDK не генерирует их из сериализаторов). Чтобы не дублировать, в `JsonSchema.kt` завести хелперы:

```kotlin
internal fun schemaObject(
    description: String? = null,
    required: List<String> = emptyList(),
    properties: JsonObjectBuilder.() -> Unit,
): JsonObject
internal fun schemaString(description: String, enum: List<String>? = null, format: String? = null): JsonObject
internal fun schemaInteger(description: String, minimum: Int? = null, maximum: Int? = null): JsonObject
internal fun schemaBoolean(description: String): JsonObject
internal fun schemaArray(description: String, items: JsonObject): JsonObject
internal fun schemaOneOf(description: String, variants: List<JsonObject>): JsonObject
```

и собрать `recordsFilterSchema`, `periodSpecSchema`, `aggregationSchema` как константы, переиспользуемые в `list_records` и `get_analytics`. В `ToolSchema` передаются `properties` и `required` верхнего уровня. Для `period`/`aggregation` использовать `oneOf` с `const` для `type`. `outputSchema` заполнять не нужно.

## 4. Алгоритмы

### 4.1. Flatten (`BudgetState.flatRecords()`)

```kotlin
fun BudgetState.flatRecords(): List<FlatRecord> {
    val scale = info.currency.scale
    return transactions.flatMap { (id, transaction) ->
        val date = transaction.timestamp
        transaction.type.fold(
            ifEntry = { idWithAccount, records ->
                records.map { record ->
                    FlatRecord(
                        transactionId = id, date = date,
                        account = idWithAccount.key,
                        category = record.idWithCategory.key,
                        direction = record.idWithCategory.key.direction.toRecordDirection(),
                        amount = record.amount.toAmount(scale),
                        comment = record.comment.text.takeIf(String::isNotBlank),
                        transferCounterpartAccount = null,
                    )
                }
            },
            ifTransfer = { from, to, amountExpression ->
                val amount = amountExpression.toAmount(scale)
                listOf(
                    FlatRecord(id, date, from.key, null, RecordDirection.Debit,  amount, null, to.key),
                    FlatRecord(id, date, to.key,   null, RecordDirection.Credit, amount, null, from.key),
                )
            },
        )
    }
}
```

Порядок результата — порядок `transactions` в `BudgetState` (хронологический, как хранится). Сортировку «новые сверху» делать в `queryRecords`.

Производительность: `flatRecords()` — O(записей), считается на каждый вызов инструмента; `AmountExpression.toAmount` кэширует результат внутри себя. Этого достаточно, кэшировать `List<FlatRecord>` не нужно.

### 4.2. Фильтр и пагинация (`queryRecords`)

```kotlin
fun queryRecords(state: BudgetState, filter: RecordsFilter?, offset: Int, limit: Int): RecordsPage {
    val effectiveLimit = limit.coerceIn(1, MAX_LIMIT)   // MAX_LIMIT = 200
    val effectiveOffset = offset.coerceAtLeast(0)
    val all = state.flatRecords().asReversed()   // новые сверху: обратный порядок транзакций…
    // …но внутри одной транзакции порядок записей должен остаться прямым:
    // поэтому либо разворачивать по транзакциям (groupBy transactionId сохраняя порядок), либо
    // строить flatRecords от transactions.asReversed() — исполнителю выбрать второе: параметр
    // `newestFirst: Boolean` у flatRecords, который разворачивает список транзакций до flatMap.
    val filtered = all.filter { it.matches(filter) }
    val page = filtered.drop(effectiveOffset).take(effectiveLimit)
    return RecordsPage(page, total = filtered.size, offset = effectiveOffset, limit = effectiveLimit,
                       hasMore = effectiveOffset + page.size < filtered.size)
}
```

`date` по убыванию при `transactions.asReversed()` получается автоматически только если транзакции хранятся отсортированными по дате. Это так в приложении (список транзакций показывает `asReversed()` без сортировки), но для надёжности сделать `sortedByDescending { it.date }` — `sortedBy` стабилен, порядок внутри одной даты сохранится.

`FlatRecord.matches(filter)`:

```kotlin
fun FlatRecord.matches(filter: RecordsFilter?): Boolean {
    filter ?: return true
    if (filter.dateMin != null && date < filter.dateMin) return false
    if (filter.dateMax != null && date > filter.dateMax) return false
    if (filter.amountMin != null && amount < filter.amountMin) return false
    if (filter.amountMax != null && amount > filter.amountMax) return false
    filter.query?.takeIf(String::isNotBlank)?.let { q ->
        if (comment?.contains(q, ignoreCase = true) != true) return false
    }
    filter.categories?.takeIf { it.isNotEmpty() }?.let { if (category == null || category !in it) return false }
    filter.accounts?.takeIf { it.isNotEmpty() }?.let { if (account !in it) return false }
    filter.direction?.let { d ->
        val ok = d.fold(
            ifCredit   = { category != null && direction == RecordDirection.Credit },
            ifDebit    = { category != null && direction == RecordDirection.Debit },
            ifTransfer = { category == null },
        )
        if (!ok) return false
    }
    return true
}
```

### 4.3. Таблица аналитики: строки

```
records  = state.flatRecords().filter { it.matches(filter) }      // включая date_min/date_max
if records.isEmpty(): return AnalyticsTable(range = null, periods = [], totals = zero)

rangeStart = filter?.dateMin ?: records.minOf { it.date }
rangeEnd   = filter?.dateMax ?: records.maxOf { it.date }
range      = rangeStart..rangeEnd

analyticsPeriod = periodSpec.toAnalyticsPeriod()
rows = analyticsPeriod.periods(range)            // NonEmptyList<LocalDateRange>, для Whole = [range]
partial(row) = row.start < range.start || row.endInclusive > range.endInclusive
```

`PeriodSpec.toAnalyticsPeriod()`: `Whole → AnalyticsPeriod.Whole`, `Months(count, startDay) → AnalyticsPeriod.Months(count, startDay)`, `Years(count, startMonth: Int, startDay) → AnalyticsPeriod.Years(count, Month(startMonth), startDay)`, `Days(count, anchor) → AnalyticsPeriod.Days(count, anchor)`. Валидация: `count ≥ 1`, `1 ≤ start_day ≤ 31`, `1 ≤ start_month ≤ 12` — через `require` с текстом ошибки.

Ключ группы записи:

```kotlin
private fun FlatRecord.groupKey(groupBy: GroupBy?): String? = groupBy?.fold(
    ifCategory = { category?.id ?: TRANSFER_GROUP_KEY },   // "transfer"
    ifAccount  = { account.id },
)
```

### 4.4. Значения ячеек

Базовая операция — `totalsOf(records: List<FlatRecord>): Totals`: `sumDebit = Σ amount где direction == Debit`, `sumCredit = Σ amount где direction == Credit`.

**Sum.** Для строки `row`: `rowRecords = records.filter { it.date in row }`; итоги строки `totalsOf(rowRecords)`; группы `rowRecords.groupBy { it.groupKey(groupBy) }.mapValues(::totalsOf)`, затем убрать группы с `sumDebit == zero && sumCredit == zero`.

**Sum + incremental.** Идём по строкам по порядку, храним `accTotals: Totals` и `accGroups: Map<String, Totals>`; значения строки = аккумулятор после прибавления строки. Итоги таблицы = `totalsOf(records)` (совпадает с последней строкой).

**Average.** Для строки `row` с подпериодом `subperiod: PeriodDuration`:

```
subperiods = subperiod.subperiodsOf(row)               // NonEmptyList<LocalDateRange>
isFull(sp) = sp.endInclusive <= row.endInclusive
          && sp.start >= range.start && sp.endInclusive <= range.endInclusive
full = subperiods.filter(isFull)
used = if (full.isNotEmpty()) full else subperiods     // fallback как в calcGroupAmount
divisor = used.size
// суммируем только записи из used-подпериодов (не всей строки!), затем делим:
usedRecords = rowRecords.filter { r -> used.any { r.date in it } }
totals(row)  = totalsOf(usedRecords) / divisor          // Totals.div(divisor, scale) — покомпонентно через Amount.div
groups(row)  = usedRecords.groupBy(groupKey).mapValues { totalsOf(it) / divisor }, нули убрать
```

Замечание: здесь `range` играет роль `totalRange` из `calcGroupAmount` (диапазон данных), а `row` — роль `period`. Отличие от приложения — «диапазон данных» ограничен ещё и `date_min/date_max` фильтра; это намеренно: LLM явно задала границы.

Итоги таблицы при `average`: та же формула при `row = range` (подпериоды выравниваются по `range.start`).

`Totals.sum` вычисляется всегда как `sumCredit − sumDebit` (`SignedAmount`), отдельно не хранится.

### 4.5. Метаданные

```
first/last = transactions.minOf/maxOf { it.value.timestamp } (null если пусто)
records_count = flatRecords().size
categories = state.categories.sortedBy { it.value.title }.map { (id, info) -> CategoryMeta(id, info.title, id.direction) }
accounts   = state.accounts.sortedBy { it.value.title }.map { (id, info) ->
               AccountMeta(id, info.title, balance = SignedAmount.of(info.amount), hideIfZero = info.hideIfAmountIsZero) }
```

`SignedAmount.of(KeyValue<AmountDirection, Amount>)`: `Credit → +value`, `Debit → −value`.

## 5. Изменения по файлам

### 5.1. Модуль `model`, слой запросов (новые файлы, §2.1)

Интерфейсы — как в §2.0, без изменений. Реализации:

```kotlin
// impl/BudgetsStorageQuery.kt
class BudgetsStorageQuery(private val budgetsStorage: BudgetsStorage) : BudgetsQuery {
    override suspend fun budgets(): List<BudgetSummary> = budgetsStorage.list.value.map { (id, repository) ->
        BudgetSummary(id = id, title = repository.state.value.info.title)
    }
    override suspend fun budget(id: BudgetId): BudgetQuery? = budgetsStorage.list.value
        .firstOrNull { it.key == id }
        ?.let { (id, repository) -> BudgetStateQuery(id = id, state = repository.state) }
}

// impl/BudgetStateQuery.kt
class BudgetStateQuery(override val id: BudgetId, private val state: StateFlow<BudgetState>) : BudgetQuery {
    override suspend fun metadata(): BudgetMetadata = calc { it.metadata(id) }
    override suspend fun records(filter: RecordsFilter?, offset: Int, limit: Int): RecordsPage =
        calc { queryRecords(it, filter, offset, limit) }
    override suspend fun analytics(filter: RecordsFilter?, period: PeriodSpec, groupBy: GroupBy?, aggregation: Aggregation): AnalyticsTable =
        calc { calcAnalyticsTable(it, filter, period, groupBy, aggregation) }
    private suspend inline fun <T> calc(crossinline block: (BudgetState) -> T): T =
        withContext(Dispatchers.Default) { block(state.value) }
}
```

Все DTO — `@Serializable data class` с `@SerialName` в `snake_case`. Ниже сигнатуры; поля по §3. Файлы `calc/` помечены `internal`.

```kotlin
// SignedAmount.kt
@Serializable(SignedAmount.Serializer::class)
@JvmInline value class SignedAmount(val value: BigDecimal) {
    object Serializer : MappingKSerializer<String, SignedAmount>(String.serializer(),
        Mapper(direct = { SignedAmount(it.toBigDecimal()) }, reverse = { it.value.toStringExpanded() }))
    companion object {
        fun of(directioned: KeyValue<AmountDirection, Amount>): SignedAmount
        fun creditMinusDebit(credit: Amount, debit: Amount): SignedAmount
    }
}

// Totals.kt
@Serializable data class Totals(
    @SerialName("sum_debit") val sumDebit: Amount,
    @SerialName("sum_credit") val sumCredit: Amount,
) {
    @EncodeDefault val sum: SignedAmount = SignedAmount.creditMinusDebit(sumCredit, sumDebit)
    operator fun plus(other: Totals): Totals
    fun div(divisor: Int, scale: DecimalScale): Totals
    val isZero: Boolean
    companion object { val zero: Totals }
}
```

Внимание: у `Totals` вычисляемое свойство `sum` должно попасть в JSON — либо `@EncodeDefault val sum` в теле класса (kotlinx сериализует свойства с backing field из тела класса, если они `@Serializable`‑совместимы и не `@Transient`), либо сделать его параметром конструктора с дефолтом. Исполнителю проверить тестом на сериализацию (§6).

```kotlin
// FlatRecord.kt
@Fold @Serializable enum class RecordDirection { @SerialName("debit") Debit, @SerialName("credit") Credit }

@Serializable data class FlatRecord(
    @SerialName("transaction_id") val transactionId: Transaction.Id,
    @SerialName("date") val date: LocalDate,
    @SerialName("account") val account: AccountId,
    @SerialName("category") val category: CategoryId?,
    @SerialName("direction") val direction: RecordDirection,
    @SerialName("amount") val amount: Amount,
    @SerialName("comment") val comment: String?,
    @SerialName("transfer_counterpart_account") val transferCounterpartAccount: AccountId?,
)

// RecordsFilter.kt
@Fold @Serializable enum class RecordsFilterDirection {
    @SerialName("credit") Credit, @SerialName("debit") Debit, @SerialName("transfer") Transfer }

@Serializable data class RecordsFilter(
    @SerialName("date_min") val dateMin: LocalDate? = null,
    @SerialName("date_max") val dateMax: LocalDate? = null,
    @SerialName("query") val query: String? = null,
    @SerialName("amount_min") val amountMin: Amount? = null,
    @SerialName("amount_max") val amountMax: Amount? = null,
    @SerialName("categories") val categories: List<CategoryId>? = null,
    @SerialName("accounts") val accounts: List<AccountId>? = null,
    @SerialName("direction") val direction: RecordsFilterDirection? = null,
)

// RecordsPage.kt
@Serializable data class RecordsPage(
    @SerialName("records") val records: List<FlatRecord>,
    @SerialName("total") val total: Int,
    @SerialName("offset") val offset: Int,
    @SerialName("limit") val limit: Int,
    @SerialName("has_more") val hasMore: Boolean,
)
const val MAX_RECORDS_LIMIT = 200
const val DEFAULT_RECORDS_LIMIT = 50

// calc/QueryRecords.kt
internal fun queryRecords(state: BudgetState, filter: RecordsFilter?, offset: Int, limit: Int): RecordsPage

// AnalyticsRequest.kt
@Fold @Serializable sealed interface PeriodSpec {
    @Serializable @SerialName("whole") data object Whole : PeriodSpec
    @Serializable @SerialName("months") data class Months(val count: Int, @SerialName("start_day") val startDay: Int) : PeriodSpec
    @Serializable @SerialName("years") data class Years(val count: Int, @SerialName("start_month") val startMonth: Int, @SerialName("start_day") val startDay: Int) : PeriodSpec
    @Serializable @SerialName("days") data class Days(val count: Int, val anchor: LocalDate) : PeriodSpec
}
// calc/PeriodSpecExt.kt
internal fun PeriodSpec.toAnalyticsPeriod(): AnalyticsPeriod     // с require-валидацией
internal fun SubperiodSpec.toPeriodDuration(): PeriodDuration

@Fold @Serializable enum class GroupBy { @SerialName("category") Category, @SerialName("account") Account }

@Fold @Serializable sealed interface Aggregation {
    @Serializable @SerialName("sum") data class Sum(val incremental: Boolean = false) : Aggregation
    @Serializable @SerialName("average") data class Average(val subperiod: SubperiodSpec) : Aggregation
}
@Serializable data class SubperiodSpec(val count: Int, val unit: SubperiodUnit)
@Fold @Serializable enum class SubperiodUnit { @SerialName("day") Day, @SerialName("month") Month, @SerialName("year") Year }

// AnalyticsTable.kt
@Serializable data class DateRangeDto(val start: LocalDate, val end: LocalDate)   // LocalDateRange ⇄ DTO
@Serializable data class AnalyticsRow(
    val start: LocalDate, val end: LocalDate, val partial: Boolean,
    val groups: Map<String, Totals>? ,          // null при group_by == null
    @SerialName("sum_debit") val sumDebit: Amount, @SerialName("sum_credit") val sumCredit: Amount, val sum: SignedAmount,
)
@Serializable data class AnalyticsTable(
    val range: DateRangeDto?,
    @SerialName("group_by") val groupBy: GroupBy?,
    val aggregation: Aggregation,
    val periods: List<AnalyticsRow>,
    @SerialName("sum_debit") val sumDebit: Amount, @SerialName("sum_credit") val sumCredit: Amount, val sum: SignedAmount,
)
```

Для `AnalyticsRow`/`AnalyticsTable` итоги можно хранить как `Totals` и «расплющивать» кастомным сериализатором, но проще держать три поля явно и заполнять из `Totals` фабрикой `AnalyticsRow.create(row, partial, groups, totals: Totals)`.

```kotlin
// calc/AnalyticsTableCalculator.kt
internal fun calcAnalyticsTable(
    state: BudgetState, filter: RecordsFilter?, period: PeriodSpec, groupBy: GroupBy?, aggregation: Aggregation,
): AnalyticsTable
const val TRANSFER_GROUP_KEY = "transfer"

// BudgetMetadata.kt
@Serializable data class BudgetSummary(val id: BudgetId, val title: String)
@Serializable data class BudgetsList(val budgets: List<BudgetSummary>)
@Serializable data class CurrencyMeta(val scale: Long)
@Serializable data class CategoryMeta(val id: CategoryId, val title: String, val direction: RecordDirection)
@Serializable data class AccountMeta(val id: AccountId, val title: String, val balance: SignedAmount, @SerialName("hide_if_zero") val hideIfZero: Boolean)
@Serializable data class BudgetMetadata(
    val id: BudgetId, val title: String, val currency: CurrencyMeta,
    @SerialName("transactions_count") val transactionsCount: Int, @SerialName("records_count") val recordsCount: Int,
    @SerialName("first_date") val firstDate: LocalDate?, @SerialName("last_date") val lastDate: LocalDate?,
    val categories: List<CategoryMeta>, val accounts: List<AccountMeta>,
)
// calc/BudgetMetadataExt.kt
internal fun BudgetState.metadata(id: BudgetId): BudgetMetadata
```

`AmountDirection` → `RecordDirection`: маленький маппер `AmountDirection.toRecordDirection()` через `fold`.

### 5.2. Модуль `model`, `MCPModel.kt`

Перенос `BudgetMCPModel` → `org.hnau.pinfin.model.mcp.MCPModel`:

```kotlin
class MCPModel(private val scope: CoroutineScope, private val dependencies: Dependencies, private val skeleton: Skeleton) {

    @Pipe interface Dependencies { val budgetsQuery: BudgetsQuery }

    @Serializable data class Skeleton(
        val mcpIsEnabled: MutableStateFlow<Boolean> = false.toMutableStateFlowAsInitial(),   // ключ не менять
    )
    val mcpIsEnabled: MutableStateFlow<Boolean> get() = skeleton.mcpIsEnabled

    init { /* как сейчас: collectLatest → launchMCP() */ }
    private suspend fun launchMCP(): Nothing { /* как сейчас */ }

    private fun createMcpServer(): Server = Server(
        serverInfo = Implementation(name = "pinfin", version = "0.2.0"),
        options = ServerOptions(capabilities = ServerCapabilities(tools = ServerCapabilities.Tools())),   // resources убрать
    ).apply { registerPinFinTools(dependencies.budgetsQuery) }

    companion object { const val MCP_PORT = 8080; const val MCP_PATH = "/mcp" }
}
```

Листовое значение `budgetsQuery` появляется как параметр функции `ManageModel.Dependencies.mcp(budgetsQuery)` (§5.4), генератор положит его в `MCPModelDependenciesImpl`.

### 5.3. Модуль `model`, `MCPTools.kt`

```kotlin
internal fun Server.registerPinFinTools(budgetsQuery: BudgetsQuery) {
    addTool(name = "list_budgets", description = …, inputSchema = ToolSchema(), toolAnnotations = readOnly) { … }
    addTool(name = "get_budget_metadata", description = …, inputSchema = ToolSchema(properties = …, required = listOf("budget_id")), …) { … }
    addTool(name = "list_records", …) { … }
    addTool(name = "get_analytics", …) { … }
}
```

Общая обёртка:

```kotlin
private suspend inline fun <reified I, reified O> handle(
    request: CallToolRequest,
    crossinline block: suspend (I) -> O,
): CallToolResult = runCatching {
    val input = queryJson.decodeFromJsonElement<I>(request.params.arguments ?: JsonObject(emptyMap()))
    val output = block(input)
    val element = queryJson.encodeToJsonElement(output).jsonObject
    CallToolResult(content = listOf(TextContent(element.toString())), structuredContent = element)
}.getOrElse { e ->
    CallToolResult(content = listOf(TextContent("Error: ${e.message ?: e::class.simpleName}")), isError = true)
}
```

Входные DTO инструментов (внутренние, тут же в файле): `BudgetIdInput(budgetId)`, `ListRecordsInput(budgetId, filter = null, offset = 0, limit = DEFAULT_RECORDS_LIMIT)`, `AnalyticsInput(budgetId, filter = null, period: PeriodSpec = PeriodSpec.Whole, groupBy: GroupBy? = null, aggregation: Aggregation = Aggregation.Sum())`. Для `list_budgets` — `Unit`‑подобный пустой `@Serializable object`/`data class NoInput`.

Поиск бюджета:

```kotlin
private suspend fun BudgetsQuery.budgetOrThrow(id: BudgetId): BudgetQuery =
    budget(id) ?: throw IllegalArgumentException("Budget $id not found. Call list_budgets to get valid ids.")
```

`withContext(Dispatchers.Default)` в обёртке `handle` не нужен: потоками занимается `BudgetStateQuery`. Обёртка только декодирует вход, вызывает `block` и кодирует выход.

`budget_id` парсить через `BudgetId` (сериализатор UUID уже есть); ошибка парсинга UUID попадёт в общий `runCatching`.

### 5.4. `ManageModel.kt`

```kotlin
@Pipe interface Dependencies {
    val preferences: Preferences
    val budgetsStorage: BudgetsStorage
    fun mcp(budgetsQuery: BudgetsQuery): MCPModel.Dependencies     // новое; budgetsQuery — листовое значение
    @Pipe interface WithOpener { … без изменений … }
    fun withOpener(budgetOpener: BudgetOpener, mcp: MCPModel): WithOpener   // новый параметр-лист
}

@Serializable data class Skeleton(
    var budgetSkeleton: … , var create: … , var icon: … ,
    val mcp: MCPModel.Skeleton = MCPModel.Skeleton(),      // новый ключ с дефолтом
)

val mcp: MCPModel = MCPModel(
    scope = scope,
    dependencies = dependencies.mcp(budgetsQuery = BudgetsStorageQuery(dependencies.budgetsStorage)),
    skeleton = skeleton.mcp,
)

private val dependenciesWithOpener = dependencies.withOpener(
    budgetOpener = selectedBudgetPreference.update,
    mcp = mcp,
)
```

Порядок инициализации: `mcp` объявить **выше** `dependenciesWithOpener`, иначе в `withOpener` уйдёт `null`.

### 5.5. `BudgetManageModel.kt`

- Из `Dependencies` убрать `fun mcp(): BudgetMCPModel.Dependencies`, добавить `val mcp: MCPModel` (лист; генератор протащит его от параметра `withOpener(..., mcp)` через `BudgetRootModel.Dependencies → BudgetModel.Dependencies → BudgetManageModel.Dependencies` по имени).
- Из `Skeleton` убрать поле `mcp` (старый ключ в сохранённом JSON будет проигнорирован).
- Убрать создание `BudgetMCPModel`; оставить `val mcp: MCPModel get() = dependencies.mcp`, чтобы проектор не менялся.

Проверить после сборки сгенерированные `…DependenciesImpl.kt`: `BudgetManageModelDependenciesImpl` должен получить `override val mcp: MCPModel`, а `ManageModelDependenciesWithOpenerImpl` — параметр `mcp`.

### 5.6. Модуль `projector`

- `BudgetMCPProjector.kt`: заменить тип `model: BudgetMCPModel` на `MCPModel`, константы `MCPModel.MCP_PORT/MCP_PATH`. Остальное без изменений.
- `BudgetManageProjector.kt`: без изменений (`model.mcp` остаётся).
- Локализация: текст `accessByMCP` можно уточнить, что доступ ко **всем** бюджетам: `"Доступ через MCP (все бюджеты)"` — на усмотрение исполнителя.

### 5.7. Модуль `app`

Изменений в `PinFinAppSeed.kt` не требуется: новых листовых зависимостей с корня нет. Если генератор `@Pipe` всё же потребует что‑то в `impl(...)` — это признак ошибки в §5.4/§5.5, а не повод менять корень.

### 5.8. Прочее

- `opencode.json` — не менять (URL тот же).
- Удалить `model/.../budget/manage/BudgetMCPModel.kt`.
- Обновить память/документацию: этот файл — источник правды по API; при изменении полей править §3.

## 6. Тесты (`model/src/commonTest/kotlin/org/hnau/pinfin/model/utils/budget/query/`)

Фикстуру строить руками из `BudgetState` (см. как в `AnalyticsCalculatorTest` создаются `CategoryInfo.createDefault`, `AccountInfo.createDefault`, `AmountExpression`). Понадобится `BudgetStatePrototype` для конструктора `BudgetState` — использовать `BudgetStatePrototype.empty`, `BudgetInfo.create(id, null)`. Вызывать через публичный контракт: `BudgetStateQuery(id, MutableStateFlow(state))` и `runTest { … }` (`kotlinx-coroutines-test`; если зависимости в `commonTest` нет — добавить в `model/build.gradle.kts`). Функции `calc/` — `internal`, из тестов того же модуля они тоже доступны, но тесты §6.1–6.4 писать через `BudgetQuery`, чтобы контракт был покрыт.

1. **`FlatRecordsTest`** — entry с двумя записями даёт две записи с категориями и направлениями из `CategoryId`; перевод даёт две записи: `from/debit/counterpart=to`, `to/credit/counterpart=from`, `category == null`, `comment == null`; пустой комментарий → `null`.
2. **`RecordsFilterTest`** — по одному тесту на каждое поле; `direction=credit` не пропускает входящую половину перевода; `direction=transfer` пропускает обе половины; `categories=[]` эквивалентно `null`; `query` регистронезависим.
3. **`QueryRecordsTest`** — сортировка новые сверху, порядок записей внутри транзакции прямой; `total/has_more/offset/limit`; `limit=500` режется до 200; `offset` за пределами даёт пустую страницу и `has_more=false`.
4. **`AnalyticsTableCalculatorTest`**:
   - `whole`, без групп: одна строка = `range`, `partial=false`, итоги совпадают с суммами записей;
   - `months(1, 1)` за три месяца, `group_by=category`: три строки, нулевые группы отсутствуют, перевод попадает в группу `"transfer"`, `sum = credit − debit`;
   - `date_min/date_max` внутри данных: `range` = границы фильтра, крайние строки `partial=true`;
   - `incremental=true`: строки накапливаются, итоги таблицы равны последней строке;
   - `average(month)` по годовой строке с данными за 2.5 месяца: делитель = число полных месяцев (2), третий неполный не учитывается; fallback, когда полных нет;
   - `group_by=account` с переводом: у `from` растёт `sum_debit`, у `to` — `sum_credit`, итоги строки по переводу дают `sum = 0`;
   - пустой результат фильтра: `range=null`, `periods=[]`, нули.
5. **`QuerySerializationTest`** — `queryJson.encodeToString` для `FlatRecord`, `Totals`, `AnalyticsTable` содержит ожидаемые ключи `snake_case`, `sum` присутствует, суммы — строки; `decodeFromString` входных DTO из JSON‑примеров §3.5/§3.6 (в т. ч. `period` всех четырёх типов и обе `aggregation`); отсутствующие поля читаются как `null`/дефолт.
6. **`PeriodSpecTest`** — маппинг в `AnalyticsPeriod`, `require` на невалидные `count/start_day/start_month`.
7. **`BudgetsStorageQueryTest`** — фейковый `BudgetsStorage` с двумя репозиториями: `budgets()` отдаёт id и title обоих, `budget(unknown)` → `null`, `budget(id).id == id`.

Запуск: `./gradlew :model:jvmTest`.

## 7. Ручная проверка

1. Собрать и запустить desktop‑приложение (`./gradlew :app:run` или как принято в проекте), открыть «Управление бюджетом», включить «Доступ через MCP». Убедиться, что после перезапуска приложения на вкладке «Транзакции» сервер поднят (проверить с клиента, не открывая вкладку управления).
2. Клиент: `npx @modelcontextprotocol/inspector` → Streamable HTTP → `http://localhost:8080/mcp`. Проверить `tools/list` (4 инструмента), вызовы:
   - `list_budgets` → есть id;
   - `get_budget_metadata` → категории и счета совпадают с приложением, `balance` совпадает со вкладкой «Счета»;
   - `list_records` с `direction=transfer` → только переводы, парами по `transaction_id`;
   - `get_analytics` `months(1,1)` + `group_by=category` за текущий год → суммы по категориям совпадают с экраном аналитики (сумма, месяц с 1‑го числа);
   - `get_analytics` `group_by=account`, `filter.accounts=[<счёт>]`, `sum incremental` → последняя строка равна `balance` счёта из метаданных (при отсутствии `date_min`).
3. Ошибки: неверный `budget_id` → `isError=true` с подсказкой вызвать `list_budgets`; `start_day=40` → `isError` с текстом валидации.
4. Переключить бюджет в приложении — сервер не должен перезапускаться (адреса в UI не мигают, inspector не теряет сессию).

## 8. Порядок работ

1. Контракт (`BudgetsQuery`, `BudgetQuery`, DTO) + `calc/` + `impl/` + тесты (§5.1, §6). Не трогает UI, можно проверять `jvmTest`.
2. `MCPModel` + `MCPTools` поверх `BudgetsQuery` (§5.2, §5.3), пока на старом месте в дереве (временно создать `BudgetsStorageQuery` из `budgetsStorage`, который уже есть в `BudgetManageModel.Dependencies`) — проверить инструменты через inspector.
3. Переезд в `ManageModel` (§5.4, §5.5, §5.6), удаление старого файла, проверка сгенерированных `Impl`.
4. Ручная проверка (§7).

## 9. Вне скоупа (для следующих задач)

- Android AppFunctions поверх `BudgetsQuery`/`BudgetQuery` (нужен только экземпляр `BudgetsStorageQuery`).
- Перевод аналитики приложения (`PeriodsAnalyticsModel`/`calcPeriod`) на `BudgetQuery.analytics`.
- Аутентификация MCP‑сервера (токен в заголовке), выбор порта.

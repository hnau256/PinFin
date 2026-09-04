# Аналитика v2: периоды с настраиваемым началом

Статус: план согласован с автором, код не менялся. Дата: 2026-09-04.

## 1. Что есть сейчас

Вкладка «Аналитика» (`BudgetTab.Analytics`) состоит из двух под-вкладок (`AnalyticsTab`):

- `Accounts` — список счетов с балансами (`AccountsModel` / `AccountsProjector`). Работает, к периодам отношения не имеет.
- `Graph` — «график» по периодам. Именно его нужно переделать.

### 1.1. Как устроен `Graph`

Модель (`model/.../budget/analytics/tab/graph/`):

| Класс | Роль |
|---|---|
| `GraphModel` | Хранит `config: MutableStateFlow<AnalyticsConfig>` и состояние `Configured / Configure` (просмотр или экран настроек). |
| `GraphConfigFlowModel` → `GraphConfigModel` | На каждое изменение `BudgetState` пересчитывает список страниц через `AnalyticsPagesProvider` и создаёт `GraphPagesModel`. |
| `GraphPagesModel` | Список периодов (`AnalyticsPage`), текущий индекс, кнопки «назад / вперёд». |
| `GraphPageModel` | Считает суммы по элементам страницы (категории/счета), выдаёт `State` с двумя половинами (доходы / расходы) и `Filters` для перехода в список транзакций. |
| `GraphConfigureModel` | Экран настроек: `ConfigSplitPeriodModel` (Весь период / Выбранный период + Y/M/D) и `ConfigOperationModel` (Сумма / Среднее + подпериод Y/M/D). |

Чистые вычисления (`model/.../utils/analytics/`): `AnalyticsEntry` (транзакция → плоские записи), `AnalyticsPagesProvider` (диапазон дат → страницы), `LocalDateRange.splitToPeriods`, конфиги `AnalyticsConfig / AnalyticsSplitConfig / AnalyticsPageConfig / AnalyticsViewConfig`.

Проектор (`projector/.../budget/analytics/graph/`) зеркалит модель один в один.

### 1.2. Найденные проблемы

0. **Падение при «Среднее в месяц» (подтверждено автором: «при попытке посмотреть траты в месяц вылетает»).** Причина: `GraphPageModel.div` ([GraphPageModel.kt](../model/src/commonMain/kotlin/org/hnau/pinfin/model/budget/analytics/tab/graph/configured/GraphPageModel.kt), приватный `operator fun Amount.div(divisor: Int)`) делит `BigDecimal` из ionspin bignum без `DecimalMode`. Суммы создаются через `String.toBigDecimal()` без режима, поэтому библиотека выбирает «неограниченную точность» и на бесконечной дроби (например `100 / 3`) бросает `ArithmeticException("Non-terminating result of division operation…")` — проверено по исходникам bignum 0.3.10, `BigDecimal.divide`. Исключение летит из `LoadableStateFlow` в `GraphPageModel.state` и валит приложение. Лечится делением с `currency.scale.decimalMode` (как уже делает `AmountExpression.toAmount`).
1. **Начало периода нельзя задать (подтверждено автором: «начало любого периода с 1‑го января делает поведение непонятным»).** В `AnalyticsSplitConfig.Period.Fixed` есть поле `startOfOneOfPeriods`, но UI его не редактирует. Хуже: `ConfigSplitPeriodModel.editablePeriod` жёстко подставляет 1 января текущего года, а `GraphModel.defaultConfig` — 1‑е число текущего месяца. После первого сохранения настроек «якорь» молча меняется.
2. **Дрейф дат при длительности в месяцах.** `splitToPeriods` прибавляет `duration` итеративно: 31 янв + 1 мес = 28 фев, потом 28 мар, 28 апр… Период «с 31‑го» превращается в «с 28‑го».
3. **Смешение источников времени.** Диапазон страниц берётся из транзакций (`AnalyticsPagesProvider.calcTotalRange`), но при пустом бюджете и в дефолтах конфига используется `Clock.System`. По решению автора аналитика должна быть чистой функцией от бюджета: системные часы из модели уходят полностью.
4. **Среднее делит на все подпериоды, включая неполные** (`GraphPageModel.calcItemAmount`): последний, текущий подпериод занижает среднее.
5. **Фильтр по периоду невидим.** При тапе по категории открывается список транзакций с `Filters.period`, но `FilterModel` хранит период как неизменяемое поле (`//TODO`), а `FilterProjector` его не показывает. Пользователь видит «обрезанный» список и не понимает почему, снять фильтр нельзя.
6. **Мёртвые настройки.** `groupBy`, `usedAccounts`, `usedCategories`, `AnalyticsViewConfig.View` есть в конфиге, но из UI не меняются (всегда «по категориям», все счета).
7. **Пересчёт.** `GraphPageModel` для каждого элемента страницы фильтрует все записи страницы заново: O(элементы × подпериоды × записи). На странице терпимо, но при группировке по 30+ категориям с «средним по дням» уже заметно. Индикатор пересчёта (`Delayed.isInProgress`) не показывается (`//TODO handle delayed` в `GraphConfigProjector`).
8. **Мелочи.** Заголовок вкладки `Graph` — «Категории» (`AnalyticsTabExt`). Выбранная страница хранится индексом — при смене конфига или появлении старых транзакций индекс «уезжает».

### 1.3. Важные ограничения проекта

- **Сохранённое состояние.** Весь `RootModel.Skeleton` сериализуется в JSON и восстанавливается при запуске (`AppModel` в commons). Есть `ignoreUnknownKeys`, но **нет fallback при ошибке десериализации** — несовместимое изменение `@Serializable` Skeleton/enum уронит приложение на старте. Правило для всей доработки: старые ключи не переименовывать, новые поля добавлять с дефолтами, старые вложенные структуры либо оставлять совместимыми, либо класть под новый ключ (старый ключ будет проигнорирован).
- **Зависимости через `@Pipe`.** Интерфейсы `Dependencies` собираются KSP‑генератором из commons; листовые значения (`budgetRepository`, `localization`, `dateTimeFormatter`, `transactionsOpener`…) протаскиваются автоматически. Новый листовой параметр в глубине дерева потребует добавить его в `impl(...)` корня (`RootModel.Dependencies.impl` / `RootProjector.Dependencies.impl` в модуле `app`).
- **Код‑стиль.** `@Fold` для sealed/enum + `fold(...)` вместо `when`, `@SealUp` для «состояние‑модель / состояние‑скелет», `Editable` + `editable { }` для форм, `StateFlow` + `mapState / mapWithScope / derivedStateFlowOf`, скелеты с `MutableStateFlow` для сохраняемого UI‑состояния.
- Даты — `kotlinx.datetime.LocalDate` (время суток из транзакций убрано последним коммитом), `LocalDateRange`, `DatePeriod`.
- Тестов в `model` нет (`commonTest` есть только в `data`). JVM‑таргет модуля `model` называется `jvm` (`./gradlew :model:jvmTest`), у `app` — `desktop`.

## 2. Целевое решение

### 2.1. Модель периода

Якорь хранится **в терминах своей единицы**, а не как искусственная дата: «месяц с 10‑го» — это буквально `startDay = 10`. Никаких констант вроде `2000-01-01`.

```kotlin
@Fold @Serializable
sealed interface AnalyticsPeriod {

    /** Все транзакции одним периодом. */
    @Serializable @SerialName("whole")
    data object Whole : AnalyticsPeriod

    /** N месяцев, начиная с числа startDay (1..31, при коротком месяце — его последний день). */
    @Serializable @SerialName("months")
    data class Months(
        val count: Int,      // ≥ 1; 1 = месяц, 3 = квартал
        val startDay: Int,   // 1..31
    ) : AnalyticsPeriod

    /** N лет, начиная с месяца и числа (например 1 марта). */
    @Serializable @SerialName("years")
    data class Years(
        val count: Int,
        val startMonth: Month,
        val startDay: Int,   // 1..31, для февраля клампится к 28/29
    ) : AnalyticsPeriod

    /** N дней от конкретной даты (7 дней от понедельника = неделя). */
    @Serializable @SerialName("days")
    data class Days(
        val count: Int,
        val anchor: LocalDate,   // реальная дата, выбранная пользователем; дефолт — дата первой транзакции
    ) : AnalyticsPeriod
}

/** Длительность без якоря — для подпериода в «Среднем» (подпериоды выравниваются по началу периода страницы). */
@Serializable
data class PeriodDuration(
    val count: Int,        // ≥ 1
    val unit: PeriodUnit,  // Day | Month | Year
)

@Fold @EnumValues @Serializable
enum class PeriodUnit { Day, Month, Year }
```

Пресеты — готовые значения: Неделя = `Days(7, понедельник…)`, Месяц = `Months(1, 1)`, Квартал = `Months(3, 1)`, Год = `Years(1, JANUARY, 1)`, Весь период = `Whole`. «Свой» = любой из трёх вариантов с произвольным `count`.

Границы периодов считаются **индексной арифметикой в единице периода**, поэтому дрейфа нет и умножать `DatePeriod` не нужно:

- `Months`: сквозной индекс месяца `m = year * 12 + (month - 1)`; период `i` покрывает месяцы `[i * count, (i + 1) * count)`; начало = `clampDay(monthOf(i * count), startDay)`, конец = день перед началом периода `i + 1`. Для `count > 1` фаза выровнена по январю (кварталы: янв/апр/июл/окт). «С 31‑го» даёт 31 янв → 28 фев → 31 мар → 30 апр: «последний день месяца» без накопления ошибки.
- `Years`: то же по индексу года; для `count > 1` фаза — годы, кратные `count`.
- `Days`: `k = floorDiv(daysBetween(anchor, date), count)`; начало = `anchor + k * count дней`.

Единый API поверх вариантов:

- `fun AnalyticsPeriod.periodContaining(date: LocalDate): LocalDateRange` (для `Whole` требует диапазон данных)
- `fun AnalyticsPeriod.next(period: LocalDateRange): LocalDateRange` / `previous`
- `fun AnalyticsPeriod.periods(range: LocalDateRange): NonEmptyList<LocalDateRange>` — от периода, содержащего `range.start`, до содержащего `range.endInclusive`, без обрезки по данным (страница «10 авг – 9 сен» так и называется, даже если транзакции начались 15 авг).

Полный диапазон данных: `первая транзакция .. последняя транзакция`. Никаких обращений к системным часам: аналитика — детерминированная функция от `BudgetState` и конфига. При пустом бюджете периодов нет, показывается пустое состояние («Транзакции отсутствуют»).

### 2.2. Как пользователь задаёт начало периода

Контрол якоря определяется вариантом `AnalyticsPeriod`:

| Вариант | Контрол | Пример подписи |
|---|---|---|
| `Months` | день месяца 1…31 (ряд чипов или поле +/−) | «Месяц с 10‑го числа», «3 месяца с 10‑го числа» |
| `Years` | месяц + день | «Год с 1 марта» |
| `Days`, `count` кратен 7 | день недели (якорь = ближайшая такая дата не позже даты первой транзакции) | «Неделя с понедельника», «2 недели с понедельника» |
| `Days`, иначе | Material3 `DatePicker` (как в `DateProjector`) | «10 дней, один из периодов начинается 01.09.2026» |

Пресеты длительности чипами: **Неделя · Месяц · Квартал · Год · Весь период · Свой**. «Свой» раскрывает поле «число» (`NonNegativeCountModel`, минимум 1) и переключатель единицы Дни / Месяцы / Годы. При смене пресета или единицы якорь сохраняется, если совместим (день месяца остаётся при переходе месяц → квартал, день недели — при 7 → 14 дней), иначе сбрасывается на дефолт единицы (день 1, 1 января, понедельник, дата последней транзакции для календаря).

Дефолт: `Months(count = 1, startDay = 1)`. Единственный якорь‑дата — `Days.anchor`; его дефолт — дата первой транзакции бюджета (функция от бюджета, не от часов). При переключении единицы совместимая часть якоря сохраняется (день месяца при месяц → квартал, день недели при 7 → 14 дней), иначе берётся дефолт варианта.

### 2.3. Конфиг аналитики

```kotlin
@Serializable
data class AnalyticsConfig(
    val period: AnalyticsPeriod,
    val groupBy: GroupBy? = GroupBy.Category,     // Category | Account | null = только итог
    val operation: Operation = Operation.Sum,     // Sum | Average(subperiod: PeriodDuration)
    val accounts: NonEmptySet<AccountId>? = null, // ограничение (фаза 4)
    val categories: NonEmptySet<CategoryId?>? = null,
)
```

Хранится, как и сейчас, в скелете модели (локально на устройстве). См. открытый вопрос №1.

### 2.4. Навигация по периодам

- Выбранный период хранить **датой начала** (`selectedPeriodStart: MutableStateFlow<LocalDate?>`), `null` = «период, содержащий последнюю транзакцию» (последняя страница). При смене конфига или данных берётся период, содержащий сохранённую дату; если она вне диапазона — ближайший.
- Заголовок периода: для месяца с 1‑го — «Сентябрь 2026»; для года с 1 янв — «2026»; иначе «10 авг – 9 сен 2026» (`PeriodFormatter` в проекторе, рядом с `DateTimeFormatter`).
- «‹ ›» как сейчас + тап по заголовку открывает список периодов (nice‑to‑have, фаза 3).

### 2.5. Расчёт

Чистая функция в `model/.../utils/analytics/AnalyticsCalculator.kt`:

```kotlin
fun calcPeriod(
    entries: List<AnalyticsEntry>,   // уже отфильтрованные по периоду
    period: LocalDateRange,
    config: AnalyticsConfig,
    groups: List<GroupKey>,          // категории / счета из BudgetState
): PeriodResult
```

Один проход по записям: ключ группы + индекс подпериода → накопление `KeyValue<AmountDirection, Amount>`. Результат — те же «половины» (доходы / расходы), отсортированные по убыванию, с `Filters` для перехода в транзакции, плюс `total`. Деление при среднем — только через `divide(divisor, currency.scale.decimalMode)` (см. проблему 0).

#### Среднее и неполные подпериоды: варианты

Пример: период «год 2026», операция «среднее в месяц», последняя транзакция 4 сентября 2026, первая — 15 марта 2024. В 2026 году 8 полных месяцев (январь–август) и неполный сентябрь (данные за 4 дня). Пусть расходы Jan–Aug = 800 000, сентябрь = 10 000.

| Вариант | Формула | Результат в примере | Плюсы | Минусы |
|---|---|---|---|---|
| **(а) только полные подпериоды** | сумма по полным / число полных | 800 000 / 8 = 100 000 | Честное «сколько уходит за месяц»; число стабильно в течение месяца и меняется только 1‑го числа | Текущий месяц не влияет на среднее вообще; если период короче подпериода (например «месяц» + «среднее в месяц»), полных подпериодов нет — нужен fallback |
| (б) все пересекающиеся подпериоды (как сейчас) | вся сумма / число подпериодов | 810 000 / 9 = 90 000 | Простота | В начале месяца среднее резко проваливается и растёт весь месяц; на 1‑е число врёт сильнее всего |
| (в) нормировка по дням | вся сумма / прошедшие дни × средняя длина подпериода | 810 000 / 247 × 30.44 ≈ 99 850 | Учитывает текущий месяц пропорционально; работает и для коротких периодов | «Месяц» превращается в 30.44 дня, цифры «не сходятся» с суммами по месяцам; крупные разовые траты в начале месяца искажают |
| (г) дробный вес текущего подпериода | вся сумма / (полные + прошедшая доля текущего) | 810 000 / (8 + 4/30) ≈ 99 590 | Компромисс (а) и (в): полные месяцы считаются как есть, текущий — пропорционально | Сложнее объяснить; та же чувствительность к крупным тратам в начале месяца |


Что считать «полным» подпериодом (для (а) и (г)): он целиком лежит внутри периода страницы **и** внутри диапазона данных `первая транзакция … последняя транзакция`. Так первый частичный месяц (данные с 15 марта 2024) и последний (сентябрь до 4‑го) исключаются одинаково. Следствие «функции от бюджета»: сентябрь станет полным не по календарю, а когда появится транзакция 30 сентября или позже; это ожидаемо и предсказуемо.

**Принятое решение (автор: «сделать как логичнее, не усложнять»): вариант (а)** — делим на число полных подпериодов, подпись «среднее за месяц · 8 полных месяцев». Fallback, когда полных подпериодов нет (период короче подпериода или данных меньше одного подпериода): делим на число пересекающихся подпериодов, как в (б), с пометкой «неполные данные». Реализация — одна строка: `divisor = fullCount.takeIf { it > 0 } ?: intersectingCount`. Прогнозы и нормировка по дням не делаются.

Покрыть unit‑тестами (`model/src/commonTest`): границы периодов (10‑е число, 31‑е число, квартал с фазой по январю, год с 1 марта и 29 февраля, 14 дней, «весь период»), `periodContaining` / `next` / `previous`, суммы/среднее на синтетических записях.

### 2.6. Структура кода (новые пакеты вместо `graph`)

Модель `model/.../budget/analytics/tab/periods/`:

| Класс | Аналог сейчас | Изменения |
|---|---|---|
| `PeriodsAnalyticsModel` | `GraphModel` | Skeleton: `config`, `selectedPeriodStart`, `state` (`configured / configure`). |
| `PeriodsFlowModel` → `PeriodsModel` | `GraphConfigFlowModel` → `GraphConfigModel` + `GraphPagesModel` | Периоды из `BudgetState + config` (без часов); выбор по дате начала; prev/next; `Delayed` наружу. |
| `PeriodModel` | `GraphPageModel` | Использует `AnalyticsCalculator`. |
| `configure/AnalyticsConfigureModel` | `GraphConfigureModel` | Собирает `PeriodConfigModel`, `GroupByConfigModel`, `OperationConfigModel` через `editable { }`. |
| `configure/period/PeriodConfigModel` | `ConfigSplitPeriodModel` | Пресеты + «Свой» (`DurationModel`) + `AnchorModel`. |
| `configure/period/DurationModel` | `ConfigPeriodModel` (Y/M/D) | Одно число (`NonNegativeCountModel`, ≥ 1) + единица `PeriodUnit`; выдаёт `Editable<PeriodDuration>`; в `PeriodConfigModel` вместе с `AnchorModel` собирается в `Editable<AnalyticsPeriod>`. |
| `configure/period/AnchorModel` | — | Состояние якоря по единице: `startDay` / `startMonth + startDay` / `anchor: LocalDate` (+ представление днём недели при кратности 7). |
| `configure/OperationConfigModel` | `ConfigOperationModel` | Подпериод — тоже `DurationModel`. |
| `configure/GroupByConfigModel` | — | Выбор `GroupBy?`. |

Чистая часть `model/.../utils/analytics/`: `period/AnalyticsPeriod.kt`, `period/AnalyticsPeriodExt.kt`, `AnalyticsConfig.kt` (новый), `AnalyticsCalculator.kt`, `PeriodResult.kt`; `AnalyticsEntry` остаётся.

Проектор `projector/.../budget/analytics/periods/` зеркалит: `PeriodsAnalyticsProjector`, `PeriodsProjector` (шапка с периодом и стрелками), `PeriodProjector` (список, как `GraphPageProjector`), `configure/*Projector`, `AnchorProjector`, `utils/PeriodFormatter.kt`.

Старый пакет `graph` удаляется в последней фазе, когда новый подключён к `AnalyticsTab.Graph` (само имя enum‑значения **оставить** `Graph` или добавить `@SerialName("Graph")` — оно лежит в сохранённом состоянии).

## 3. План работ по фазам

Каждая фаза оставляет проект компилируемым и запускаемым.

### Фаза 0 — подготовка (маленькие безопасные правки)

0. **Хотфикс падения (проблема 0):** в `GraphPageModel.div` делить через `value.divide(divisor.toBigDecimal(), currency.scale.decimalMode)`. `Currency` доступна из `BudgetState.info.currency` в `GraphConfigModel` — пробросить в `GraphPagesModel` → `GraphPageModel` параметром конструктора. Заодно защитить `Amount`: добавить в `data` функцию `Amount.div(divisor: Int, scale: DecimalScale)` и убрать локальный небезопасный `div`. Можно выпустить отдельным коммитом до всей переделки.
1. Добавить `model/src/commonTest` (kotlin‑test уже подключён конвенционным плагином — проверить на `data`).
2. Написать тесты на текущий `splitToPeriods`, зафиксировать баг с дрейфом (тест падает) — станет регрессионным.
3. Исправить заголовок вкладки: `AnalyticsTabExt.title` → `localization.analytics` / новая строка «По периодам».
4. `FilterModel.Skeleton.period` → `MutableStateFlow<LocalDateRange?>`, включить в `combineState`; в `FilterProjector` добавить чип «Период: 10 авг – 9 сен» с кнопкой сброса. Закрывает проблему 5 независимо от остального.

### Фаза 1 — домен периодов (только `model`, без UI)

1. `utils/analytics/period/AnalyticsPeriod.kt`, `PeriodDuration.kt`, `PeriodUnit.kt` + `AnalyticsPeriodExt.kt` (`periodContaining`, `next`, `previous`, `periods(range)`).
2. Тесты из п. 2.5.
3. `AnalyticsConfig` (новый) и `AnalyticsCalculator` + `PeriodResult`; тесты на суммы/среднее.
4. Убрать все `Clock.System` из аналитики: диапазон — только из транзакций; пустой бюджет → пустое состояние без страниц; дефолт `Days.anchor` — дата первой транзакции (см. п. 2.2).

### Фаза 2 — новая модель и проектор просмотра

1. `PeriodsAnalyticsModel`, `PeriodsModel`, `PeriodModel` + проекторы; переиспользовать вёрстку `GraphPageProjector` (заголовки «Всего / Доходы / Расходы», строки с `LinearProgressIndicator`, `SwitchHue`).
2. Подключить к `AnalyticsModel.Skeleton` под **новым ключом** (`periods`), поле `graph` удалить — старые данные будут проигнорированы `ignoreUnknownKeys`.
3. Обработка `Delayed.isInProgress` — тонкий `LinearProgressIndicator` под шапкой при пересчёте.
4. Заголовок периода через `PeriodFormatter`; выбор периода по дате начала; дефолт — период последней транзакции.
5. Проверить переход в транзакции с фильтром (теперь видимым).

### Фаза 3 — экран настроек

1. `PeriodConfigModel` (пресеты чипами + «Свой» через `DurationModel`: число + единица), `AnchorModel` + `AnchorProjector` (четыре режима контрола из п. 2.2).
2. `GroupByConfigModel` (Категории / Счета / Только итог), `OperationConfigModel` (перенос).
3. `AnalyticsConfigureModel` собирает `Editable<AnalyticsConfig>`; FAB «Сохранить» активен только при корректном и изменённом конфиге (как сейчас).
4. Карточка конфига над списком: одна строка‑резюме «Месяц с 10‑го · по категориям · сумма» + шестерёнка.
5. Локализация: новые строки в `Localization` (пресеты, «Начало периода», «День месяца», «Группировка», «Только итог», склонения формата периода).
6. Nice‑to‑have: список периодов по тапу на заголовок; мини‑обзор (столбики итогов по периодам) над списком с переходом к периоду по тапу.

### Фаза 4 — ограничение по счетам / категориям в аналитике

1. В `AnalyticsConfigureModel` добавить `SelectAccountsModel` / `SelectCategoriesModel` (уже есть в `model/filter/pageable`, UI — `SelectAccountsProjector.Page` / `SelectCategoriesProjector.Page`).
2. Пробросить `accounts / categories` в `AnalyticsCalculator` и в `Filters` при переходе к транзакциям.

### Фаза 5 — чистка

1. Удалить `budget/analytics/tab/graph/**` в модели и проекторе (включая `ConfigPeriodModel`, `PeriodPart`/`PeriodParts` — их заменяет `DurationModel`), `AnalyticsSplitConfig`, `AnalyticsPageConfig`, `AnalyticsViewConfig`, `AnalyticsPagesProvider`, `AnalyticsPage`, оба `splitToPeriods`, пустой `GroupKey.kt`.
2. Убрать неиспользуемые строки локализации (`inclusivePeriod`, `fixedPeriod`, `sumFor`, `avgFor` — если не переиспользованы).
3. Прогнать `./gradlew :model:jvmTest`, десктоп‑сборку `app`, android `assembleDebug` (инкрементит `versionCode` в `android/version.properties` — это ожидаемо).

### Оценка объёма

Фаза 0 — 0.5 дня; фаза 1 — 1 день; фаза 2 — 1–1.5 дня; фаза 3 — 1.5–2 дня; фаза 4 — 0.5 дня; фаза 5 — 0.5 дня. Итого ≈ 5–6 рабочих дней с тестами.

## 4. Решения автора (2026-09-04) и открытые вопросы

Решено:

1. **Конфиг аналитики хранится локально, в Skeleton** (не в `BudgetConfig`).
2. **Произвольный диапазон «с даты по дату» — не сейчас**, добавим потом. Пресеты: Неделя · Месяц · Квартал · Год · Весь период · Свой.
4. **Вкладка «Счета» внутри аналитики остаётся** как есть.
5. **Симптомы «работает плохо»:** падение при просмотре среднего в месяц (проблема 0) и непонятное поведение из‑за начала любого периода с 1 января (проблема 1). Оба закрываются: первое хотфиксом в фазе 0, второе — фазами 1–3.
6. **Fallback при несовместимом сохранённом состоянии в commons не нужен:** состояние служит для восстановления после перезапуска Android‑приложения и сбрасывается при переустановке. Правило «новые ключи с дефолтами, старые не переименовывать» всё равно соблюдаем, чтобы не ловить краш при обновлении без переустановки.

3. **Среднее по неполным подпериодам** — вариант (а) с fallback на (б), см. п. 2.5. Прогнозов нет.
8. **Аналитика не зависит от системных часов** — только от `BudgetState` и конфига. `Clock.System` из пакета аналитики убирается целиком.

7. **«Свой» — одна единица: N дней / месяцев / лет.** Смешанные длительности (1 месяц + 15 дней) не разрешаются. Якорь по единице: месяцы → день месяца, годы → месяц и день, дни кратно 7 → день недели, иначе → календарь. Для подпериода «Среднего» — то же.

Открытых вопросов нет.

# Компоненты фронтенда

## Дерево компонентов

```mermaid
graph TD
    main["main.tsx\n(точка входа)"]
    App["App.tsx\n(корень)"]
    QCP["QueryClientProvider\n(кеш запросов)"]
    Router["BrowserRouter\n(роутер)"]
    Layout["Layout\n(шапка + навбар)"]
    NavBar["NavBar\n(нижняя навигация)"]

    main --> App
    App --> QCP
    QCP --> Router
    Router --> Layout
    Layout --> NavBar

    Layout --> DiaryPage
    Layout --> AddEntryPage
    Layout --> ProductsPage
    Layout --> SettingsPage

    DiaryPage --> DayTotal
    DiaryPage --> EntryItem

    AddEntryPage --> NutritionBadge
    AddEntryPage --> Input
    AddEntryPage --> Button

    EntryItem --> NutritionBadge

    DayTotal -.->|"goal из useMe()"| User[(Пользователь)]

    style main fill:#f3f4f6
    style App fill:#f3f4f6
    style QCP fill:#dbeafe,stroke:#3b82f6
    style Router fill:#dbeafe,stroke:#3b82f6
```

---

## Слои приложения

```mermaid
flowchart TB
    subgraph Pages["Страницы (pages/)"]
        DiaryPage
        AddEntryPage
        ProductsPage
        SettingsPage
    end

    subgraph Components["Компоненты (components/)"]
        DayTotal
        EntryItem
        NutritionBadge
        subgraph UI["ui/ — базовые кирпичики"]
            Button
            Input
        end
    end

    subgraph Hooks["Хуки (hooks/) — данные"]
        useEntries
        useProducts
        useUser
    end

    subgraph API["API (api/) — сетевые запросы"]
        client["client.ts\n(HTTP-клиент)"]
        entries_api["entries.ts"]
        products_api["products.ts"]
        users_api["users.ts"]
    end

    Backend[("Backend\nSpring Boot :8080")]

    Pages --> Components
    Pages --> Hooks
    Hooks --> API
    API --> Backend

    style Pages fill:#fef9c3,stroke:#ca8a04
    style Components fill:#dcfce7,stroke:#16a34a
    style Hooks fill:#e0e7ff,stroke:#4f46e5
    style API fill:#fee2e2,stroke:#dc2626
```

---

## Страницы

### DiaryPage — Дневник питания

**Главная страница.** Показывает всё съеденное за выбранный день.

```
┌────────────────────────────┐
│  ← 12 июня 2025  →        │  ← навигация по датам
├────────────────────────────┤
│  Итого за день             │
│  Калории  [████████░░] 80% │  ← DayTotal
│  Белки    [██████░░░░] 65% │
│  Жиры     [█████░░░░░] 55% │
│  Углеводы [███░░░░░░░] 30% │
├────────────────────────────┤
│  Завтрак                   │
│  Овсянка  200г             │  ← EntryItem
│  200 ккал · Б 7г · Ж 4г   │
├────────────────────────────┤
│  Обед                      │
│  Куриная грудка  150г      │  ← EntryItem
│  248 ккал · Б 46г · Ж 5г  │
└────────────────────────────┘
                         [+]   ← кнопка → AddEntryPage
```

**Откуда берёт данные:** `useEntriesByDate(date)` + `useMe()` (для цели)

---

### AddEntryPage — Добавить запись

**Двухшаговая форма.** Шаг 1: найти продукт. Шаг 2: ввести граммы.

```
Шаг 1: Поиск                   Шаг 2: Количество
┌────────────────────┐          ┌────────────────────┐
│ Поиск продукта     │          │ [Гречка ×]         │
│ [гречка_________] │  ──────▶ │ 200 ккал на 100г   │
│                    │          │                    │
│ Гречка варёная     │          │ Количество, г      │
│ 92 ккал · Б 4г    │          │ [150______________]│
│                    │          │                    │
│ Гречневая каша     │          │ [Завтрак] [Обед]   │
│ 340 ккал · Б 12г  │          │ [Ужин   ] [Перекус]│
│                    │          │                    │
│                    │          │ [Добавить]         │
└────────────────────┘          └────────────────────┘
```

**Откуда берёт данные:** `useProductSearch(query)` + `useCreateEntry()`

---

### ProductsPage — Справочник продуктов

**Поиск и просмотр продуктов.** В будущем — создание своих продуктов.

**Откуда берёт данные:** `useProductSearch(query)`

---

### SettingsPage — Настройки

**Форма для дневной Цели КБЖУ.** Калории обязательны, БЖУ — нет.

**Откуда берёт данные:** `useMe()` + `useUpdateDailyGoal()`

---

## Компоненты

### DayTotal

Блок итогов дня с прогресс-барами.

**Пропсы:**
| Проп | Тип | Описание |
|---|---|---|
| `total` | `Nutrition` | Суммарное КБЖУ за день |
| `goal` | `DailyGoal \| null` | Дневная цель; если `null` — прогресс-бары не показываются |

Если фактическое значение превышает цель — бар и число окрашиваются в красный.

---

### EntryItem

Одна строка в списке записей. Показывает название, граммы, КБЖУ, кнопку удаления.

**Пропсы:**
| Проп | Тип | Описание |
|---|---|---|
| `entry` | `Entry` | Данные записи |

Вызывает `useDeleteEntry()` при нажатии на крестик — запись удаляется и список обновляется автоматически.

---

### NutritionBadge

Инлайн-строка с КБЖУ: `250 ккал · Б 20 г · Ж 8 г · У 30 г`

Используется везде, где нужно компактно показать нутриенты.

**Пропсы:**
| Проп | Тип | Описание |
|---|---|---|
| `nutrition` | `Nutrition` | Значения КБЖУ |
| `className?` | `string` | Дополнительные CSS-классы |

---

### Button

Кнопка с вариантами стиля и состоянием загрузки.

| Вариант | Когда использовать |
|---|---|
| `primary` | Главное действие (сохранить, добавить) |
| `secondary` | Второстепенное действие |
| `ghost` | Ненавязчивые действия в тексте |
| `danger` | Удаление, деструктивные действия |

При `loading={true}` показывает спиннер и блокирует повторный клик.

---

### Input

Текстовое поле с лейблом и сообщением об ошибке.

| Проп | Тип | Описание |
|---|---|---|
| `label?` | `string` | Подпись над полем |
| `error?` | `string` | Ошибка под полем (красная рамка) |

---

## Хуки (hooks/)

Хук — функция, которая знает **как получить данные** и держит их в кеше.
Компонент не знает про `fetch` — он просто вызывает хук.

```mermaid
sequenceDiagram
    participant C as DiaryPage
    participant H as useEntriesByDate("2025-06-12")
    participant Q as TanStack Query (кеш)
    participant A as GET /api/entries?date=...

    C->>H: вызов хука
    H->>Q: queryKey: ['entries', '2025-06-12']
    
    alt Данные свежие (< 60 сек)
        Q-->>H: данные из кеша
    else Нет в кеше / устарели
        Q->>A: HTTP запрос
        A-->>Q: JSON
        Q->>Q: Сохранить в кеш
        Q-->>H: данные
    end
    
    H-->>C: { data, isLoading, isError }
```

| Хук | Что делает |
|---|---|
| `useEntriesByDate(date)` | Записи за дату |
| `useCreateEntry()` | Создать запись, обновить кеш |
| `useDeleteEntry()` | Удалить запись, обновить кеш |
| `useProductSearch(query)` | Поиск продуктов (от 2 символов) |
| `useCreateProduct()` | Создать продукт |
| `useMe()` | Данные пользователя и его цель |
| `useUpdateDailyGoal()` | Сохранить новую цель |

---

## API-клиент (api/)

```mermaid
flowchart LR
    Hook["Хук\nuseEntries.ts"] --> APIModule["API-модуль\nentries.ts"]
    APIModule --> Client["client.ts\nhttp.get / http.post"]
    Client --> Fetch["fetch()\n(браузер)"]
    Fetch --> SW["Service Worker"]
    SW --> Backend["Backend :8080"]
```

`client.ts` содержит один объект `http` с методами `get`, `post`, `put`, `delete`.
Он добавляет `Content-Type: application/json` и единообразно обрабатывает ошибки — бросает `HttpError` с кодом статуса.

Каждый API-модуль (`entries.ts`, `products.ts` и т.д.) — просто набор функций, каждая соответствует одному REST-эндпоинту.

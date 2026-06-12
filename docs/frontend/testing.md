# Тестирование фронтенда

## Стек

| Инструмент | Роль |
|---|---|
| **Vitest** | Test runner, совместим с Vite — нет отдельной конфигурации |
| **@testing-library/react** | Рендеринг компонентов и запросы к DOM |
| **@testing-library/user-event** | Симуляция действий пользователя (клик, ввод) |
| **@testing-library/jest-dom** | Дополнительные матчеры (`toBeInTheDocument`, `toHaveLength`) |
| **MSW (Mock Service Worker)** | Перехват `fetch` на сетевом уровне — бэкенд не нужен |

---

## Как это работает

В браузере MSW регистрирует Service Worker, который перехватывает `fetch`. В тестах (Node/jsdom) MSW использует `@mswjs/interceptors` — тот же механизм, но без SW:

```
[Компонент]
    │ вызывает хук
    ▼
[TanStack Query]
    │ делает fetch('/api/products')
    ▼
[MSW Interceptor]          ← перехватывает в тесте
    │ находит handler
    │ возвращает mock-ответ
    ▼
[Хук получает данные]
    ▼
[Компонент рендерится с данными]
```

Бэкенд не запускается. Тесты быстрые и детерминированные.

---

## Два слоя тестирования

Фронтенд тестируется двумя слоями — по аналогии с бэкенд-стратегией (контроллер / сервис).

### Слой 1 — Хуки (аналог Service-тестов)

**Цель:** убедиться, что хук правильно вызывает API, обрабатывает ответы и инвалидирует кеш.

**Файл:** `src/hooks/useXxx.test.ts`

**Что тестируем:**
- Успешный запрос: данные попадают в `result.current.data`
- Граничные условия: `enabled: false` при коротком query
- Ошибка сервера: `result.current.isError === true`
- Инвалидация кеша после мутации (через `queryClient`)

```ts
import { renderHook, waitFor } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { createWrapper } from '@/test/wrapper'
import { useProductSearch } from './useProducts'

describe('useProductSearch', () => {
  it('не делает запрос при query короче 2 символов', () => {
    const { result } = renderHook(
      () => useProductSearch('г'),
      { wrapper: createWrapper() },
    )
    expect(result.current.isLoading).toBe(false)
    expect(result.current.data).toBeUndefined()
  })

  it('возвращает данные при query >= 2 символов', async () => {
    const { result } = renderHook(
      () => useProductSearch('гречка'),
      { wrapper: createWrapper() },
    )
    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.content).toHaveLength(1)
  })
})
```

**Правила:**
- Каждый тест получает свой `createWrapper()` — изолированный `QueryClient`.
- Переопределяй handler через `server.use(...)` для тестирования ошибок; MSW сбрасывает его после теста автоматически.
- Для мутаций: `await act(() => result.current.mutateAsync(...))`, затем `waitFor(() => ...)`.
- Не тестируй, что данные отрисовались в DOM — это задача слоя 2.

---

### Слой 2 — Страницы (аналог Controller-тестов)

**Цель:** убедиться, что пользователь видит правильный UI и правильные запросы отправляются при его действиях.

**Файл:** `src/pages/XxxPage.test.tsx`

**Что тестируем:**
- Начальный рендер: нужные элементы присутствуют в DOM
- Валидация формы: ошибки появляются при неверном вводе
- Успешный сценарий: после действия пользователя происходит навигация / запрос / обновление UI
- Пустые состояния: `«Ничего не найдено»`, лоадеры

```tsx
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { createWrapper } from '@/test/wrapper'
import { ProductNewPage } from './ProductNewPage'

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom')
  return { ...actual, useNavigate: () => mockNavigate }
})

describe('ProductNewPage', () => {
  it('показывает ошибку если название пустое', async () => {
    const user = userEvent.setup()
    render(<ProductNewPage />, { wrapper: createWrapper() })

    await user.click(screen.getByRole('button', { name: /сохранить/i }))

    expect(await screen.findByText(/обязательное поле/i)).toBeInTheDocument()
  })

  it('создаёт продукт и переходит на /products', async () => {
    const user = userEvent.setup()
    render(<ProductNewPage />, { wrapper: createWrapper() })

    await user.type(screen.getByLabelText(/название/i), 'Гречка')
    await user.type(screen.getByLabelText(/калории/i), '110')
    // ... остальные поля
    await user.click(screen.getByRole('button', { name: /сохранить/i }))

    await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/products'))
  })
})
```

**Правила:**
- Используй `screen.getByRole` и `screen.getByLabelText` — они проверяют доступность, а не детали реализации.
- Не используй `getByTestId` — это хрупко. Если элемент нельзя найти по роли/label, это сигнал о проблеме с доступностью.
- Для асинхронных изменений: `await screen.findBy*` или `await waitFor(...)`.
- `useNavigate` мокируй через `vi.mock('react-router-dom', ...)` — стандартный паттерн для проверки навигации.
- Каждый `render` получает свой `createWrapper()`.

---

## Инфраструктура тестов

Все вспомогательные файлы живут в `src/test/`.

### `setup.ts` — глобальная настройка

Запускается перед каждым тестовым файлом (указан в `vite.config.ts → test.setupFiles`).

```ts
// src/test/setup.ts
import '@testing-library/jest-dom/vitest'  // подключает кастомные матчеры
import { cleanup } from '@testing-library/react'
import { afterAll, afterEach, beforeAll } from 'vitest'
import { server } from './server'

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => {
  server.resetHandlers()  // сбрасывает server.use() из конкретного теста
  cleanup()               // размонтирует компоненты, предотвращает утечки между тестами
})
afterAll(() => server.close())
```

`onUnhandledRequest: 'error'` — если тест делает запрос без зарегистрированного handler, тест упадёт. Это защита от случайных реальных запросов.

---

### `handlers.ts` — дефолтные MSW-хэндлеры

Описывают «счастливый путь» для всего приложения. Каждый хэндлер — минимальный ответ, который позволяет компонентам рендериться без ошибок.

```ts
// src/test/handlers.ts
import { http, HttpResponse } from 'msw'
import type { Product } from '@/types'

export const mockProduct: Product = {
  id: 1,
  name: 'Гречка',
  // ...
}

export const handlers = [
  http.get('/api/products', () => HttpResponse.json({ content: [mockProduct], ... })),
  http.post('/api/products', () => HttpResponse.json(mockProduct, { status: 201 })),
  http.delete('/api/products/:id', () => new HttpResponse(null, { status: 204 })),
]
```

**Правило:** добавляй новый handler сюда при каждом новом API-эндпоинте — иначе тесты, которые косвенно рендерят зависимые компоненты, упадут с `onUnhandledRequest: 'error'`.

Для тестирования ошибок переопределяй handler локально:

```ts
it('показывает ошибку сервера', async () => {
  server.use(
    http.post('/api/products', () =>
      HttpResponse.json({ message: 'Server error' }, { status: 500 })
    )
  )
  // ... тест
})
// После теста afterEach автоматически вернёт дефолтный handler
```

---

### `server.ts` — MSW-сервер

```ts
// src/test/server.ts
import { setupServer } from 'msw/node'
import { handlers } from './handlers'

export const server = setupServer(...handlers)
```

Один файл, импортируй `server` везде где нужно `server.use(...)`.

---

### `wrapper.tsx` — фабрика обёртки

Каждый тест получает **свой** `QueryClient` — без этого данные из одного теста утекают в другой.

```ts
// src/test/wrapper.tsx
export function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },   // не ретраить в тестах — тест упадёт сразу
      mutations: { retry: false },
    },
  })

  return function Wrapper({ children }: { children: ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>{children}</MemoryRouter>
      </QueryClientProvider>
    )
  }
}
```

`retry: false` — без этого при ошибке React Query будет делать 3 повторных запроса, тест зависнет на несколько секунд.

---

## Что тестировать, что нет

### Тестируй:
- Бизнес-правила валидации формы (пустые поля, некорректные значения)
- Условный рендер: пустые состояния, лоадеры, сообщения об ошибках
- Интеграцию хука с API: правильный HTTP-метод, инвалидация кеша
- Граничные условия хука: `enabled: false`, ошибка 4xx/5xx
- Навигацию после успешного действия

### Не тестируй:
- CSS-классы и стили — это детали реализации
- Внутреннее состояние компонента (`useState`) напрямую — тестируй только наблюдаемый результат
- Компоненты `Button` и `Input` в изоляции — они слишком простые, покрываются тестами страниц
- Точное содержимое `NutritionBadge` — достаточно что он в DOM, конкретные числа могут меняться
- E2E-сценарии (от логина до сохранения данных) — это задача для Playwright, не RTL

---

## Команды

```bash
# Запустить тесты в watch-режиме (для разработки)
npm test

# Однократный прогон (для CI)
npm run test:run

# С покрытием (html-отчёт в coverage/)
npm run test:coverage
```

---

## Матрица инструментов

| Слой | Что рендерится | Данные | Навигация |
|---|---|---|---|
| **Хук** | `renderHook` | MSW | — |
| **Страница** | `render` (полный компонент) | MSW | `vi.mock(useNavigate)` |

---

## Контрольный список при добавлении новой фичи

При добавлении нового хука + страницы создай:

- [ ] `src/hooks/useXxx.test.ts` — хук: успех, ошибка, граничные условия
- [ ] `src/pages/XxxPage.test.tsx` — страница: рендер, валидация, успешный сценарий
- [ ] handler в `src/test/handlers.ts` — для нового API-эндпоинта
- [ ] `mockXxx` объект в `handlers.ts` — если нужны новые типы данных

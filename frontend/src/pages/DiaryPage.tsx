// Главная страница — дневник питания за выбранный день.
// Показывает: итоги КБЖУ, записи сгруппированные по типу приёма пищи,
// кнопку добавить запись.

import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useEntriesByDate } from '@/hooks/useEntries'
import { useMe } from '@/hooks/useUser'
import { DayTotal } from '@/components/DayTotal'
import { EntryItem } from '@/components/EntryItem'
import type { Entry, MealType, Nutrition } from '@/types'

const MEAL_LABELS: Record<MealType, string> = {
  BREAKFAST: 'Завтрак',
  LUNCH:     'Обед',
  DINNER:    'Ужин',
  SNACK:     'Перекус',
}

const MEAL_ORDER: MealType[] = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACK']

function toLocalDate(d: Date): string {
  // Возвращает дату в формате YYYY-MM-DD в локальном часовом поясе
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function sumNutrition(entries: Entry[]): Nutrition {
  return entries.reduce(
    (acc, e) => ({
      calories: acc.calories + e.nutrition.calories,
      protein:  acc.protein  + e.nutrition.protein,
      fat:      acc.fat      + e.nutrition.fat,
      carbs:    acc.carbs    + e.nutrition.carbs,
    }),
    { calories: 0, protein: 0, fat: 0, carbs: 0 }
  )
}

export function DiaryPage() {
  const [date, setDate] = useState(toLocalDate(new Date()))

  const { data: entries = [], isLoading } = useEntriesByDate(date)
  const { data: user } = useMe()

  const total = sumNutrition(entries)

  const byMeal = MEAL_ORDER.reduce<Record<MealType, Entry[]>>(
    (acc, m) => ({ ...acc, [m]: entries.filter(e => e.mealType === m) }),
    {} as Record<MealType, Entry[]>
  )

  return (
    <div className="space-y-4 pb-24">
      {/* Навигация по датам */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => setDate(d => {
            const prev = new Date(d)
            prev.setDate(prev.getDate() - 1)
            return toLocalDate(prev)
          })}
          className="rounded-lg p-2 text-gray-500 hover:bg-gray-100"
          aria-label="Предыдущий день"
        >
          ←
        </button>
        <input
          type="date"
          value={date}
          onChange={e => setDate(e.target.value)}
          className="text-center text-sm font-medium text-gray-800 outline-none"
        />
        <button
          onClick={() => setDate(d => {
            const next = new Date(d)
            next.setDate(next.getDate() + 1)
            return toLocalDate(next)
          })}
          className="rounded-lg p-2 text-gray-500 hover:bg-gray-100"
          aria-label="Следующий день"
        >
          →
        </button>
      </div>

      {/* Итого за день */}
      <DayTotal total={total} goal={user?.dailyGoal ?? null} />

      {/* Записи по приёмам пищи */}
      {isLoading ? (
        <p className="text-center text-sm text-gray-400">Загрузка…</p>
      ) : (
        MEAL_ORDER.map(meal => {
          const mealEntries = byMeal[meal]
          if (mealEntries.length === 0) return null
          return (
            <section key={meal} className="rounded-xl bg-white p-4 shadow-sm">
              <h3 className="mb-2 text-sm font-semibold text-gray-600">{MEAL_LABELS[meal]}</h3>
              <ul className="divide-y divide-gray-50">
                {mealEntries.map(e => <EntryItem key={e.id} entry={e} />)}
              </ul>
            </section>
          )
        })
      )}

      {!isLoading && entries.length === 0 && (
        <p className="text-center text-sm text-gray-400">Записей нет. Добавь первую!</p>
      )}

      {/* Кнопка добавить запись — фиксированная внизу экрана */}
      <Link
        to={`/add?date=${date}`}
        className="fixed bottom-20 right-4 flex h-14 w-14 items-center justify-center
                   rounded-full bg-brand-600 text-white shadow-lg hover:bg-brand-700"
        aria-label="Добавить запись"
      >
        <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
          <path d="M12 5v14M5 12h14" />
        </svg>
      </Link>
    </div>
  )
}

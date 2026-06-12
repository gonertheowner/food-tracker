// Страница добавления записи.
// Шаги: 1) поиск продукта или блюда, 2) ввод граммов и типа приёма пищи.

import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import { NutritionBadge } from '@/components/NutritionBadge'
import { useProductSearch } from '@/hooks/useProducts'
import { useCreateEntry } from '@/hooks/useEntries'
import type { MealType, Product } from '@/types'

const MEAL_OPTIONS: { value: MealType; label: string }[] = [
  { value: 'BREAKFAST', label: 'Завтрак' },
  { value: 'LUNCH',     label: 'Обед' },
  { value: 'DINNER',    label: 'Ужин' },
  { value: 'SNACK',     label: 'Перекус' },
]

export function AddEntryPage() {
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const date = params.get('date') ?? new Date().toISOString().slice(0, 10)

  const [query, setQuery] = useState('')
  const [selected, setSelected] = useState<Product | null>(null)
  const [weight, setWeight] = useState('')
  const [mealType, setMealType] = useState<MealType>('BREAKFAST')

  const { data: searchResult } = useProductSearch(query)
  const { mutate: create, isPending } = useCreateEntry()

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!selected || !weight) return
    create(
      {
        productId: selected.id,
        weightGrams: Number(weight),
        mealType,
        eatenAt: `${date}T12:00:00`,
      },
      { onSuccess: () => navigate(-1) }
    )
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-5 pb-8">
      <h1 className="text-lg font-semibold text-gray-900">Добавить запись</h1>

      {!selected ? (
        <div className="space-y-3">
          <Input
            label="Поиск продукта"
            placeholder="Например: гречка, яйцо…"
            value={query}
            onChange={e => setQuery(e.target.value)}
            autoFocus
          />
          {searchResult && (
            <ul className="divide-y divide-gray-100 rounded-xl border border-gray-200 bg-white">
              {searchResult.content.map(p => (
                <li key={p.id}>
                  <button
                    type="button"
                    onClick={() => setSelected(p)}
                    className="w-full px-4 py-3 text-left hover:bg-gray-50"
                  >
                    <p className="text-sm font-medium text-gray-900">{p.name}</p>
                    {p.brand && <p className="text-xs text-gray-400">{p.brand}</p>}
                    <NutritionBadge nutrition={p.nutrition} className="mt-0.5" />
                  </button>
                </li>
              ))}
              {searchResult.content.length === 0 && (
                <li className="px-4 py-3 text-sm text-gray-400">Ничего не найдено</li>
              )}
            </ul>
          )}
        </div>
      ) : (
        <div className="space-y-4">
          {/* Выбранный продукт */}
          <div className="flex items-start justify-between rounded-xl bg-brand-50 p-4">
            <div>
              <p className="font-medium text-gray-900">{selected.name}</p>
              <NutritionBadge nutrition={selected.nutrition} className="mt-0.5" />
              <p className="text-xs text-gray-400">на 100 г</p>
            </div>
            <button
              type="button"
              onClick={() => setSelected(null)}
              className="text-gray-400 hover:text-gray-600"
            >
              ✕
            </button>
          </div>

          <Input
            label="Количество, г"
            type="number"
            min="1"
            step="1"
            value={weight}
            onChange={e => setWeight(e.target.value)}
            required
            autoFocus
          />

          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700">Приём пищи</label>
            <div className="grid grid-cols-2 gap-2">
              {MEAL_OPTIONS.map(o => (
                <button
                  key={o.value}
                  type="button"
                  onClick={() => setMealType(o.value)}
                  className={[
                    'rounded-lg border py-2 text-sm transition-colors',
                    mealType === o.value
                      ? 'border-brand-600 bg-brand-50 text-brand-700 font-medium'
                      : 'border-gray-200 text-gray-600 hover:border-gray-300',
                  ].join(' ')}
                >
                  {o.label}
                </button>
              ))}
            </div>
          </div>

          <Button type="submit" loading={isPending} className="w-full" size="lg">
            Добавить
          </Button>
        </div>
      )}
    </form>
  )
}

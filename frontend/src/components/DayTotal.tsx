// Итого за день — прогресс-бары КБЖУ относительно Цели.
// Если цели нет — просто показываем числа.

import type { DailyGoal, Nutrition } from '@/types'

interface Props {
  total: Nutrition
  goal: DailyGoal | null
}

interface BarProps {
  label: string
  value: number
  max: number | null
  unit: string
}

function NutrientBar({ label, value, max, unit }: BarProps) {
  const pct = max ? Math.min((value / max) * 100, 100) : null
  const over = max ? value > max : false

  return (
    <div className="flex flex-col gap-1">
      <div className="flex justify-between text-xs text-gray-600">
        <span>{label}</span>
        <span>
          <span className={over ? 'text-red-600 font-semibold' : ''}>{Math.round(value)}</span>
          {max && <span className="text-gray-400"> / {max}&nbsp;{unit}</span>}
          {!max && <span>&nbsp;{unit}</span>}
        </span>
      </div>
      {pct !== null && (
        <div className="h-1.5 rounded-full bg-gray-100">
          <div
            className={`h-1.5 rounded-full transition-all ${over ? 'bg-red-500' : 'bg-brand-500'}`}
            style={{ width: `${pct}%` }}
          />
        </div>
      )}
    </div>
  )
}

export function DayTotal({ total, goal }: Props) {
  return (
    <div className="rounded-xl bg-white p-4 shadow-sm space-y-3">
      <h2 className="text-sm font-semibold text-gray-700">Итого за день</h2>
      <NutrientBar label="Калории" value={total.calories} max={goal?.calories ?? null} unit="ккал" />
      <NutrientBar label="Белки"   value={total.protein}  max={goal?.protein  ?? null} unit="г" />
      <NutrientBar label="Жиры"    value={total.fat}       max={goal?.fat      ?? null} unit="г" />
      <NutrientBar label="Углеводы" value={total.carbs}   max={goal?.carbs    ?? null} unit="г" />
    </div>
  )
}

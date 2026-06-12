// Отображает КБЖУ в одну строку: «250 ккал · Б 20 г · Ж 8 г · У 30 г»
// Используется везде: в карточке продукта, в строке записи, в итогах дня.

import type { Nutrition } from '@/types'

interface Props {
  nutrition: Nutrition
  className?: string
}

export function NutritionBadge({ nutrition, className = '' }: Props) {
  const { calories, protein, fat, carbs } = nutrition
  return (
    <span className={`text-xs text-gray-500 ${className}`}>
      <span className="font-medium text-gray-700">{Math.round(calories)}&nbsp;ккал</span>
      {' · '}Б&nbsp;{protein.toFixed(1)}&nbsp;г
      {' · '}Ж&nbsp;{fat.toFixed(1)}&nbsp;г
      {' · '}У&nbsp;{carbs.toFixed(1)}&nbsp;г
    </span>
  )
}

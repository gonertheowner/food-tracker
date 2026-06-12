// Одна строка записи в дневнике питания.
// Показывает: что съедено, сколько грамм, КБЖУ, кнопку удаления.

import type { Entry } from '@/types'
import { NutritionBadge } from './NutritionBadge'
import { useDeleteEntry } from '@/hooks/useEntries'

interface Props {
  entry: Entry
}

function sourceName(entry: Entry): string {
  return entry.source.kind === 'product'
    ? entry.source.productName
    : entry.source.dishName
}

export function EntryItem({ entry }: Props) {
  const { mutate: remove, isPending } = useDeleteEntry()

  return (
    <li className="flex items-center justify-between gap-3 py-2">
      <div className="min-w-0 flex-1">
        <p className="truncate text-sm font-medium text-gray-900">{sourceName(entry)}</p>
        <div className="flex items-center gap-2">
          <span className="text-xs text-gray-400">{entry.weightGrams}&nbsp;г</span>
          <NutritionBadge nutrition={entry.nutrition} />
        </div>
      </div>
      <button
        onClick={() => remove(entry.id)}
        disabled={isPending}
        aria-label="Удалить запись"
        className="shrink-0 rounded p-1 text-gray-400 hover:bg-red-50 hover:text-red-500"
      >
        {/* Крестик SVG */}
        <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M18 6 6 18M6 6l12 12" />
        </svg>
      </button>
    </li>
  )
}

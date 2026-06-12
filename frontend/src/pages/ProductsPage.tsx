// Страница справочника продуктов.
// Поиск + список с возможностью удалить свои продукты.

import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Input } from '@/components/ui/Input'
import { NutritionBadge } from '@/components/NutritionBadge'
import { useDeleteProduct, useProductSearch } from '@/hooks/useProducts'

export function ProductsPage() {
  const [query, setQuery] = useState('')
  const { data, isLoading } = useProductSearch(query)
  const { mutate: remove, isPending: isDeleting, variables: deletingId } = useDeleteProduct()

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-lg font-semibold text-gray-900">Продукты</h1>
        {/* TODO: Добавить страницу создания продукта */}
        <Link
          to="/products/new"
          className="text-sm font-medium text-brand-600 hover:text-brand-700"
        >
          + Новый
        </Link>
      </div>

      <Input
        placeholder="Поиск…"
        value={query}
        onChange={e => setQuery(e.target.value)}
      />

      {isLoading && <p className="text-center text-sm text-gray-400">Поиск…</p>}

      {data && (
        <ul className="divide-y divide-gray-100 rounded-xl border border-gray-200 bg-white">
          {data.content.map(p => (
            <li key={p.id} className="flex items-center justify-between px-4 py-3">
              <div>
                <p className="text-sm font-medium text-gray-900">{p.name}</p>
                {p.brand && <p className="text-xs text-gray-400">{p.brand}</p>}
                <NutritionBadge nutrition={p.nutrition} className="mt-0.5" />
              </div>
              <button
                type="button"
                aria-label="Удалить"
                disabled={isDeleting && deletingId === p.id}
                onClick={() => remove(p.id)}
                className="ml-3 p-1 text-gray-300 transition-colors hover:text-red-500 disabled:cursor-not-allowed disabled:opacity-50"
              >
                ✕
              </button>
            </li>
          ))}
          {data.content.length === 0 && (
            <li className="px-4 py-3 text-sm text-gray-400">Ничего не найдено</li>
          )}
        </ul>
      )}

      {!data && !isLoading && (
        <p className="text-center text-sm text-gray-400">Начни вводить для поиска</p>
      )}
    </div>
  )
}

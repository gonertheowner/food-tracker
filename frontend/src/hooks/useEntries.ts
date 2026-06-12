// Хук для работы с Записями
//
// Зачем хуки отдельно от компонентов?
// Компонент описывает ЧТО отобразить. Хук описывает КАК получить данные.
// Разделение позволяет переиспользовать логику между страницами.

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { createEntry, deleteEntry, fetchEntriesByDate } from '@/api/entries'
import type { CreateEntryRequest } from '@/api/entries'

// queryKey — уникальный ключ кеша. TanStack Query хранит данные под этим ключом.
// При изменении date — автоматически загружает данные для новой даты.
export function useEntriesByDate(date: string) {
  return useQuery({
    queryKey: ['entries', date],
    queryFn: () => fetchEntriesByDate(date),
  })
}

export function useCreateEntry() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: CreateEntryRequest) => createEntry(body),
    onSuccess: (entry) => {
      // Инвалидируем кеш записей за эту дату — список обновится автоматически
      const date = entry.eatenAt.slice(0, 10)
      queryClient.invalidateQueries({ queryKey: ['entries', date] })
    },
  })
}

export function useDeleteEntry() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => deleteEntry(id),
    onSuccess: () => {
      // Инвалидируем все записи (дата неизвестна после удаления)
      queryClient.invalidateQueries({ queryKey: ['entries'] })
    },
  })
}

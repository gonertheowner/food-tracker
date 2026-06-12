// API для Записей (Entry)
// Каждая функция — один REST-эндпоинт

import { http } from './client'
import type { Entry, MealType } from '@/types'

export interface CreateEntryRequest {
  productId?: number
  dishId?: number
  weightGrams: number
  mealType: MealType
  eatenAt: string          // ISO-8601
}

// Все записи за конкретную дату (YYYY-MM-DD)
export function fetchEntriesByDate(date: string): Promise<Entry[]> {
  return http.get(`/api/entries?date=${date}`)
}

export function createEntry(body: CreateEntryRequest): Promise<Entry> {
  return http.post('/api/entries', body)
}

export function deleteEntry(id: number): Promise<void> {
  return http.delete(`/api/entries/${id}`)
}

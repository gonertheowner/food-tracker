// API для Блюд (Dish)

import { http } from './client'
import type { Dish, Page } from '@/types'

export interface DishItemRequest {
  productId: number
  weightGrams: number
}

export interface CreateDishRequest {
  name: string
  items: DishItemRequest[]
  isPublic: boolean
}

export function searchDishes(query: string, page = 0): Promise<Page<Dish>> {
  return http.get(`/api/dishes?q=${encodeURIComponent(query)}&page=${page}`)
}

export function getDish(id: number): Promise<Dish> {
  return http.get(`/api/dishes/${id}`)
}

export function createDish(body: CreateDishRequest): Promise<Dish> {
  return http.post('/api/dishes', body)
}

export function updateDish(id: number, body: Partial<CreateDishRequest>): Promise<Dish> {
  return http.put(`/api/dishes/${id}`, body)
}

export function deleteDish(id: number): Promise<void> {
  return http.delete(`/api/dishes/${id}`)
}

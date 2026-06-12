// API для Продуктов (Product)

import { http } from './client'
import type { Page, Product } from '@/types'

export interface CreateProductRequest {
  name: string
  brand?: string
  calories: number
  protein: number
  fat: number
  carbs: number
  isPublic: boolean
}

// Поиск продуктов — для строки поиска при добавлении записи
export function searchProducts(query: string, page = 0): Promise<Page<Product>> {
  return http.get(`/api/products?q=${encodeURIComponent(query)}&page=${page}`)
}

export function getProduct(id: number): Promise<Product> {
  return http.get(`/api/products/${id}`)
}

export function createProduct(body: CreateProductRequest): Promise<Product> {
  return http.post('/api/products', body)
}

export function updateProduct(id: number, body: Partial<CreateProductRequest>): Promise<Product> {
  return http.put(`/api/products/${id}`, body)
}

export function deleteProduct(id: number): Promise<void> {
  return http.delete(`/api/products/${id}`)
}

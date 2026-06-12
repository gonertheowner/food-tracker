// ─── Нутриенты ───────────────────────────────────────────────────────────────
// Все значения хранятся в расчёте на 100 г
export interface Nutrition {
  calories: number
  protein: number
  fat: number
  carbs: number
}

// ─── Продукт ─────────────────────────────────────────────────────────────────
export type SourceType = 'MANUAL' | 'OPENFOODFACTS' | 'USDA'

export interface Product {
  id: number
  name: string
  brand: string | null
  sourceType: SourceType
  externalId: string | null
  nutrition: Nutrition   // на 100 г
  isPublic: boolean
  userId: number
}

// ─── Блюдо ───────────────────────────────────────────────────────────────────
export interface DishItem {
  productId: number
  productName: string
  weightGrams: number
}

export interface Dish {
  id: number
  name: string
  items: DishItem[]
  nutrition: Nutrition   // вычисленное суммарное КБЖУ на 100 г
  isPublic: boolean
  userId: number
}

// ─── Запись ──────────────────────────────────────────────────────────────────
export type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'SNACK'

// Что именно съедено — продукт или блюдо (одно из двух, не оба)
export type EntrySource =
  | { kind: 'product'; productId: number; productName: string }
  | { kind: 'dish'; dishId: number; dishName: string }

export interface Entry {
  id: number
  source: EntrySource
  weightGrams: number
  mealType: MealType
  eatenAt: string        // ISO-8601 дата-время
  nutrition: Nutrition   // зафиксированное КБЖУ на момент создания
  userId: number
}

// ─── Цель (Daily goal) ───────────────────────────────────────────────────────
export interface DailyGoal {
  calories: number
  protein: number | null
  fat: number | null
  carbs: number | null
}

// ─── Пользователь ────────────────────────────────────────────────────────────
export interface User {
  id: number
  email: string
  name: string
  dailyGoal: DailyGoal | null
}

// ─── API ответы ──────────────────────────────────────────────────────────────
export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number         // текущая страница (0-indexed)
  size: number
}

export interface ApiError {
  status: number
  message: string
}

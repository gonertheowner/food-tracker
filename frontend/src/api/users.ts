// API для Пользователя и его Цели (Daily goal)

import { http } from './client'
import type { DailyGoal, User } from '@/types'

export function getMe(): Promise<User> {
  return http.get('/api/users/me')
}

export function updateDailyGoal(goal: DailyGoal): Promise<User> {
  return http.put('/api/users/me/goal', goal)
}

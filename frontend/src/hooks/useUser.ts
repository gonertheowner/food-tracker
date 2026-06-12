import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getMe, updateDailyGoal } from '@/api/users'
import type { DailyGoal } from '@/types'

export function useMe() {
  return useQuery({
    queryKey: ['user', 'me'],
    queryFn: getMe,
    staleTime: 5 * 60_000,   // данные пользователя меняются редко — кешируем 5 минут
  })
}

export function useUpdateDailyGoal() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (goal: DailyGoal) => updateDailyGoal(goal),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user', 'me'] })
    },
  })
}

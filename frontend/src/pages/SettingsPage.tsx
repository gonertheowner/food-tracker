// Страница настроек — дневная Цель КБЖУ пользователя.

import { useEffect, useState } from 'react'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import { useMe, useUpdateDailyGoal } from '@/hooks/useUser'

export function SettingsPage() {
  const { data: user } = useMe()
  const { mutate: save, isPending, isSuccess } = useUpdateDailyGoal()

  const [calories, setCalories] = useState('')
  const [protein,  setProtein]  = useState('')
  const [fat,      setFat]      = useState('')
  const [carbs,    setCarbs]    = useState('')

  // Заполняем форму когда данные пользователя загрузились
  useEffect(() => {
    if (!user?.dailyGoal) return
    const g = user.dailyGoal
    setCalories(String(g.calories))
    setProtein(g.protein  != null ? String(g.protein)  : '')
    setFat    (g.fat      != null ? String(g.fat)      : '')
    setCarbs  (g.carbs    != null ? String(g.carbs)    : '')
  }, [user])

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    save({
      calories: Number(calories),
      protein:  protein ? Number(protein) : null,
      fat:      fat     ? Number(fat)     : null,
      carbs:    carbs   ? Number(carbs)   : null,
    })
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      <h1 className="text-lg font-semibold text-gray-900">Настройки</h1>

      <section className="rounded-xl bg-white p-4 shadow-sm space-y-4">
        <h2 className="text-sm font-semibold text-gray-700">Дневная цель</h2>

        <Input
          id="calories"
          label="Калории, ккал *"
          type="number"
          min="500"
          max="10000"
          required
          value={calories}
          onChange={e => setCalories(e.target.value)}
        />
        <Input
          id="protein"
          label="Белки, г (необязательно)"
          type="number"
          min="0"
          value={protein}
          onChange={e => setProtein(e.target.value)}
        />
        <Input
          id="fat"
          label="Жиры, г (необязательно)"
          type="number"
          min="0"
          value={fat}
          onChange={e => setFat(e.target.value)}
        />
        <Input
          id="carbs"
          label="Углеводы, г (необязательно)"
          type="number"
          min="0"
          value={carbs}
          onChange={e => setCarbs(e.target.value)}
        />
      </section>

      <Button type="submit" loading={isPending} className="w-full">
        Сохранить
      </Button>

      {isSuccess && (
        <p className="text-center text-sm text-brand-600">Цель сохранена!</p>
      )}
    </form>
  )
}

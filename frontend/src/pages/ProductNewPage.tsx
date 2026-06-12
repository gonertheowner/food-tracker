import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import { useCreateProduct } from '@/hooks/useProducts'

interface FormState {
  name: string
  brand: string
  calories: string
  protein: string
  fat: string
  carbs: string
  isPublic: boolean
}

interface FormErrors {
  name?: string
  calories?: string
  protein?: string
  fat?: string
  carbs?: string
}

const INITIAL: FormState = {
  name: '',
  brand: '',
  calories: '',
  protein: '',
  fat: '',
  carbs: '',
  isPublic: false,
}

function validate(form: FormState): FormErrors {
  const errors: FormErrors = {}
  if (!form.name.trim()) errors.name = 'Обязательное поле'
  if (!form.calories || isNaN(Number(form.calories)) || Number(form.calories) < 0)
    errors.calories = 'Укажите корректное значение'
  if (!form.protein || isNaN(Number(form.protein)) || Number(form.protein) < 0)
    errors.protein = 'Укажите корректное значение'
  if (!form.fat || isNaN(Number(form.fat)) || Number(form.fat) < 0)
    errors.fat = 'Укажите корректное значение'
  if (!form.carbs || isNaN(Number(form.carbs)) || Number(form.carbs) < 0)
    errors.carbs = 'Укажите корректное значение'
  return errors
}

export function ProductNewPage() {
  const navigate = useNavigate()
  const { mutate: create, isPending, error } = useCreateProduct()

  const [form, setForm] = useState<FormState>(INITIAL)
  const [errors, setErrors] = useState<FormErrors>({})

  function setField(field: keyof FormState) {
    return (e: React.ChangeEvent<HTMLInputElement>) => {
      const value = field === 'isPublic' ? e.target.checked : e.target.value
      setForm(prev => ({ ...prev, [field]: value }))
      if (errors[field as keyof FormErrors]) {
        setErrors(prev => ({ ...prev, [field]: undefined }))
      }
    }
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    const errs = validate(form)
    if (Object.keys(errs).length > 0) {
      setErrors(errs)
      return
    }
    create(
      {
        name: form.name.trim(),
        brand: form.brand.trim() || undefined,
        calories: Number(form.calories),
        protein: Number(form.protein),
        fat: Number(form.fat),
        carbs: Number(form.carbs),
        isPublic: form.isPublic,
      },
      { onSuccess: () => navigate('/products') },
    )
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4 pb-8">
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="text-gray-400 hover:text-gray-600"
          aria-label="Назад"
        >
          ←
        </button>
        <h1 className="text-lg font-semibold text-gray-900">Новый продукт</h1>
      </div>

      <Input
        id="name"
        label="Название *"
        placeholder="Например: Гречка варёная"
        value={form.name}
        onChange={setField('name')}
        error={errors.name}
        autoFocus
      />

      <Input
        id="brand"
        label="Бренд"
        placeholder="Необязательно"
        value={form.brand}
        onChange={setField('brand')}
      />

      <p className="text-sm font-medium text-gray-700">КБЖУ на 100 г *</p>

      <div className="grid grid-cols-2 gap-3">
        <Input
          id="calories"
          label="Калории, ккал"
          type="number"
          min="0"
          step="0.1"
          placeholder="0"
          value={form.calories}
          onChange={setField('calories')}
          error={errors.calories}
        />
        <Input
          id="protein"
          label="Белки, г"
          type="number"
          min="0"
          step="0.1"
          placeholder="0"
          value={form.protein}
          onChange={setField('protein')}
          error={errors.protein}
        />
        <Input
          id="fat"
          label="Жиры, г"
          type="number"
          min="0"
          step="0.1"
          placeholder="0"
          value={form.fat}
          onChange={setField('fat')}
          error={errors.fat}
        />
        <Input
          id="carbs"
          label="Углеводы, г"
          type="number"
          min="0"
          step="0.1"
          placeholder="0"
          value={form.carbs}
          onChange={setField('carbs')}
          error={errors.carbs}
        />
      </div>

      <label className="flex cursor-pointer items-center gap-2">
        <input
          type="checkbox"
          checked={form.isPublic}
          onChange={setField('isPublic')}
          className="h-4 w-4 rounded border-gray-300 text-brand-600 focus:ring-brand-600"
        />
        <span className="text-sm text-gray-700">Публичный продукт</span>
      </label>

      {error && <p className="text-sm text-red-600">{error.message}</p>}

      <Button type="submit" loading={isPending} className="w-full" size="lg">
        Сохранить
      </Button>
    </form>
  )
}

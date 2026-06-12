import { act, renderHook, waitFor } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '@/test/server'
import { mockProduct } from '@/test/handlers'
import { createWrapper } from '@/test/wrapper'
import { useCreateProduct, useDeleteProduct, useProductSearch } from './useProducts'

describe('useProductSearch', () => {
  it('не делает запрос при query короче 2 символов', () => {
    const { result } = renderHook(() => useProductSearch('г'), { wrapper: createWrapper() })
    expect(result.current.isLoading).toBe(false)
    expect(result.current.data).toBeUndefined()
  })

  it('возвращает продукты при query >= 2 символов', async () => {
    const { result } = renderHook(() => useProductSearch('гречка'), { wrapper: createWrapper() })
    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.content).toHaveLength(1)
    expect(result.current.data?.content[0].name).toBe(mockProduct.name)
  })
})

describe('useCreateProduct', () => {
  it('успешно создаёт продукт', async () => {
    const { result } = renderHook(() => useCreateProduct(), { wrapper: createWrapper() })

    await act(() =>
      result.current.mutateAsync({
        name: 'Гречка',
        calories: 110,
        protein: 4.2,
        fat: 1.1,
        carbs: 21.0,
        isPublic: false,
      }),
    )

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
  })

  it('переходит в isError при серверной ошибке', async () => {
    server.use(
      http.post('/api/products', () =>
        HttpResponse.json({ message: 'Server error' }, { status: 500 }),
      ),
    )
    const { result } = renderHook(() => useCreateProduct(), { wrapper: createWrapper() })

    await act(async () => {
      try {
        await result.current.mutateAsync({
          name: 'X',
          calories: 1,
          protein: 0,
          fat: 0,
          carbs: 0,
          isPublic: false,
        })
      } catch {
        // ожидаемая ошибка
      }
    })

    await waitFor(() => expect(result.current.isError).toBe(true))
  })
})

describe('useDeleteProduct', () => {
  it('успешно удаляет продукт', async () => {
    const { result } = renderHook(() => useDeleteProduct(), { wrapper: createWrapper() })

    await act(() => result.current.mutateAsync(1))

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
  })
})

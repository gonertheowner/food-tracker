import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '@/test/server'
import { createWrapper } from '@/test/wrapper'
import { ProductsPage } from './ProductsPage'

function renderPage() {
  render(<ProductsPage />, { wrapper: createWrapper() })
}

describe('ProductsPage', () => {
  it('показывает подсказку при пустом поиске', () => {
    renderPage()
    expect(screen.getByText(/начни вводить для поиска/i)).toBeInTheDocument()
  })

  it('показывает продукт по результатам поиска', async () => {
    const user = userEvent.setup()
    renderPage()

    await user.type(screen.getByPlaceholderText(/поиск/i), 'гречка')

    expect(await screen.findByText('Гречка')).toBeInTheDocument()
  })

  it('показывает «Ничего не найдено» при пустом ответе', async () => {
    server.use(
      http.get('/api/products', () =>
        HttpResponse.json({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 20 }),
      ),
    )
    const user = userEvent.setup()
    renderPage()

    await user.type(screen.getByPlaceholderText(/поиск/i), 'хxx')

    expect(await screen.findByText(/ничего не найдено/i)).toBeInTheDocument()
  })

  it('вызывает DELETE при клике на кнопку удаления', async () => {
    const user = userEvent.setup()
    renderPage()

    await user.type(screen.getByPlaceholderText(/поиск/i), 'гречка')
    await screen.findByText('Гречка')

    let deleteCalled = false
    server.use(
      http.delete('/api/products/1', () => {
        deleteCalled = true
        return new HttpResponse(null, { status: 204 })
      }),
    )

    await user.click(screen.getByRole('button', { name: /удалить/i }))
    await waitFor(() => expect(deleteCalled).toBe(true))
  })
})

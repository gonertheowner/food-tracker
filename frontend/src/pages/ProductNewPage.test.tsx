import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { createWrapper } from '@/test/wrapper'
import { ProductNewPage } from './ProductNewPage'

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom')
  return { ...actual, useNavigate: () => mockNavigate }
})

function renderPage() {
  render(<ProductNewPage />, { wrapper: createWrapper() })
}

describe('ProductNewPage', () => {
  it('показывает поля формы', () => {
    renderPage()
    expect(screen.getByLabelText(/название/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/калории/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/белки/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/жиры/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/углеводы/i)).toBeInTheDocument()
  })

  it('показывает ошибку если название пустое', async () => {
    const user = userEvent.setup()
    renderPage()

    await user.click(screen.getByRole('button', { name: /сохранить/i }))

    expect(await screen.findByText(/обязательное поле/i)).toBeInTheDocument()
  })

  it('показывает ошибки для всех пустых числовых полей', async () => {
    const user = userEvent.setup()
    renderPage()

    await user.type(screen.getByLabelText(/название/i), 'Гречка')
    await user.click(screen.getByRole('button', { name: /сохранить/i }))

    const errors = await screen.findAllByText(/укажите корректное значение/i)
    expect(errors).toHaveLength(4)
  })

  it('создаёт продукт и переходит на /products', async () => {
    const user = userEvent.setup()
    renderPage()

    await user.type(screen.getByLabelText(/название/i), 'Гречка')
    await user.type(screen.getByLabelText(/калории/i), '110')
    await user.type(screen.getByLabelText(/белки/i), '4.2')
    await user.type(screen.getByLabelText(/жиры/i), '1.1')
    await user.type(screen.getByLabelText(/углеводы/i), '21')
    await user.click(screen.getByRole('button', { name: /сохранить/i }))

    await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/products'))
  })
})

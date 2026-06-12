import { http, HttpResponse } from 'msw'
import type { Product } from '@/types'

export const mockProduct: Product = {
  id: 1,
  name: 'Гречка',
  brand: null,
  sourceType: 'MANUAL',
  externalId: null,
  nutrition: { calories: 110, protein: 4.2, fat: 1.1, carbs: 21.0 },
  isPublic: false,
  userId: 42,
}

export const handlers = [
  http.get('/api/products', () =>
    HttpResponse.json({
      content: [mockProduct],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 20,
    }),
  ),

  http.post('/api/products', () =>
    HttpResponse.json({ ...mockProduct, id: 2 }, { status: 201 }),
  ),

  http.delete('/api/products/:id', () => new HttpResponse(null, { status: 204 })),
]

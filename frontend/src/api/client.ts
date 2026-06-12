// Базовый HTTP-клиент. Все методы API импортируют его.
// Добавляет Content-Type, обрабатывает ошибки единообразно.

import type { ApiError } from '@/types'

class HttpError extends Error {
  constructor(public readonly status: number, message: string) {
    super(message)
    this.name = 'HttpError'
  }
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const res = await fetch(path, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...init.headers,
    },
  })

  if (!res.ok) {
    let message = `HTTP ${res.status}`
    try {
      const body = (await res.json()) as ApiError
      message = body.message ?? message
    } catch {
      // сервер вернул не JSON — используем дефолт
    }
    throw new HttpError(res.status, message)
  }

  // 204 No Content — возвращаем undefined
  if (res.status === 204) return undefined as T

  return res.json() as Promise<T>
}

export const http = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'POST', body: JSON.stringify(body) }),
  put: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'PUT', body: JSON.stringify(body) }),
  delete: (path: string) => request<void>(path, { method: 'DELETE' }),
}

export { HttpError }

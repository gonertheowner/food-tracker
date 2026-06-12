// Корневой компонент приложения.
// Здесь: провайдеры (Query, Router), роуты, нижняя навигация.

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'
import { BrowserRouter, Link, NavLink, Route, Routes, useLocation } from 'react-router-dom'
import { DiaryPage }      from './pages/DiaryPage'
import { AddEntryPage }   from './pages/AddEntryPage'
import { ProductsPage }   from './pages/ProductsPage'
import { ProductNewPage } from './pages/ProductNewPage'
import { SettingsPage }   from './pages/SettingsPage'

// QueryClient — глобальный кеш запросов.
// staleTime: 60 сек — данные считаются свежими 1 минуту, не перезапрашиваются.
const queryClient = new QueryClient({
  defaultOptions: {
    queries: { staleTime: 60_000 },
  },
})

function NavBar() {
  const location = useLocation()
  // На странице добавления записи нижнюю панель скрываем (полноэкранная форма)
  if (location.pathname === '/add') return null

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `flex flex-col items-center gap-0.5 text-xs ${isActive ? 'text-brand-600' : 'text-gray-400'}`

  return (
    <nav className="fixed bottom-0 inset-x-0 flex items-center justify-around
                    border-t border-gray-100 bg-white pb-safe px-4 pt-2 shadow-sm">
      <NavLink to="/" end className={linkClass}>
        {/* Дневник */}
        <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75">
          <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" />
          <rect x="9" y="3" width="6" height="4" rx="1" />
          <path d="M9 12h6M9 16h4" />
        </svg>
        <span>Дневник</span>
      </NavLink>

      <NavLink to="/products" className={linkClass}>
        {/* Продукты */}
        <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75">
          <path d="M3 6h18M3 12h18M3 18h18" />
        </svg>
        <span>Продукты</span>
      </NavLink>

      <NavLink to="/settings" className={linkClass}>
        {/* Настройки */}
        <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75">
          <circle cx="12" cy="12" r="3" />
          <path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14" />
        </svg>
        <span>Настройки</span>
      </NavLink>
    </nav>
  )
}

function Layout() {
  return (
    <div className="mx-auto max-w-lg min-h-screen bg-gray-50">
      {/* Шапка */}
      <header className="sticky top-0 z-10 border-b border-gray-100 bg-white px-4 py-3">
        <Link to="/" className="text-base font-bold text-brand-600">Food Tracker</Link>
      </header>

      {/* Контент страницы */}
      <main className="px-4 py-4">
        <Routes>
          <Route path="/"              element={<DiaryPage />} />
          <Route path="/add"           element={<AddEntryPage />} />
          <Route path="/products"      element={<ProductsPage />} />
          <Route path="/products/new"  element={<ProductNewPage />} />
          <Route path="/settings"      element={<SettingsPage />} />
        </Routes>
      </main>

      <NavBar />
    </div>
  )
}

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Layout />
      </BrowserRouter>
      {/* Devtools: панель отладки запросов — только в dev-режиме */}
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  )
}

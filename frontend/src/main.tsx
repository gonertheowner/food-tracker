// Точка входа. Монтирует React в <div id="root"> из index.html.
// Регистрирует Service Worker (PWA).

import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { registerSW } from 'virtual:pwa-register'
import { App } from './App'
import './index.css'

// Service Worker регистрируется автоматически.
// При выходе новой версии SW — страница перезагрузится через 1 секунду.
registerSW({ immediate: true })

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>
)

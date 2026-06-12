import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      // Service Worker стратегия: при обновлении SW автоматически активируется
      // без ожидания закрытия всех вкладок
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg,webp}'],
        // Кешируем всё статичное — приложение работает офлайн
        runtimeCaching: [
          {
            urlPattern: /^\/api\//,
            handler: 'NetworkFirst',
            // API запросы: сначала сеть, при отсутствии — кеш
            options: {
              cacheName: 'api-cache',
              expiration: { maxAgeSeconds: 300 }, // 5 минут
            },
          },
        ],
      },
      manifest: {
        name: 'Food Tracker',
        short_name: 'FoodTracker',
        description: 'Личный трекер питания — учёт КБЖУ по дням',
        theme_color: '#16a34a',       // зелёный — цвет темы (заголовок браузера на Android)
        background_color: '#ffffff',
        display: 'standalone',        // приложение без браузерных кнопок, как нативное
        orientation: 'portrait',
        scope: '/',
        start_url: '/',
        icons: [
          {
            src: '/icons/icon-192.png',
            sizes: '192x192',
            type: 'image/png',
          },
          {
            src: '/icons/icon-512.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'any maskable', // maskable — иконка с отступами для Android adaptive icons
          },
        ],
      },
    }),
  ],
  server: {
    port: 5173,
    proxy: {
      // В режиме разработки проксируем /api на Spring Boot бекенд
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})

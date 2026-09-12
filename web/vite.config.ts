/// <reference types="vitest/config" />
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import { VitePWA } from 'vite-plugin-pwa'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    // Installability for the customer-facing surface (README: "mobile-first,
    // installable customer PWA"). The dashboard shares this Vite app but is
    // not itself meant to be "installed" — the manifest/scope below is
    // written from the customer app's perspective (start_url, name, theme).
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.svg'],
      manifest: {
        name: 'Loyalty Rewards',
        short_name: 'Rewards',
        description: 'Check your points balance and tier, redeem offers.',
        start_url: '/customer',
        scope: '/',
        display: 'standalone',
        background_color: '#fffaf0',
        theme_color: '#f97316',
        icons: [
          {
            src: '/pwa-icon-192.svg',
            sizes: '192x192',
            type: 'image/svg+xml',
          },
          {
            src: '/pwa-icon-512.svg',
            sizes: '512x512',
            type: 'image/svg+xml',
          },
          {
            src: '/pwa-icon-512.svg',
            sizes: '512x512',
            type: 'image/svg+xml',
            purpose: 'maskable',
          },
        ],
      },
      workbox: {
        // Only precache the app shell; there's no API to cache responses
        // from yet (core-api has no REST layer — see root README).
        globPatterns: ['**/*.{js,css,html,svg}'],
      },
      devOptions: {
        enabled: false,
      },
    }),
  ],
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/testSetup.ts',
    // 'forks' (Vitest's default pool) hangs/times out spawning workers in
    // some sandboxed/Windows environments; 'threads' is reliable here.
    pool: 'threads',
  },
})

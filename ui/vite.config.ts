/// <reference types="vitest/config" />
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: { port: 5173, strictPort: true },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/testSetup.ts'],
    // `e2e/` holds Playwright specs, which call Playwright's own `test()`.
    // Without this exclude, `vitest run` collects them and hard-fails with
    // "Playwright Test did not expect test() to be called here".
    exclude: ['**/node_modules/**', '**/dist/**', 'e2e/**'],
  },
})

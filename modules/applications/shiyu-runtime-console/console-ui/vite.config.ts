import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  base: '/console/',
  plugins: [vue()],
  test: {
    environment: 'jsdom',
    restoreMocks: true,
  },
  build: {
    outDir: '../src/main/resources/META-INF/resources/console',
    emptyOutDir: true,
    sourcemap: false,
  },
  server: {
    proxy: {
      '/console/api': 'http://127.0.0.1:9000',
      '/api': 'http://127.0.0.1:9000',
      '/v3/api-docs': 'http://127.0.0.1:9000',
    },
  },
})

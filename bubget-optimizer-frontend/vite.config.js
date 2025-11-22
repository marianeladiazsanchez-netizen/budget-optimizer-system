import { defineConfig } from 'vite'

export default defineConfig({
  
  server: {
    port: 5173,
    host: '0.0.0.0',

    // ⭐ PROXY PARA EVITAR CORS EN DESARROLLO
    proxy: {
      '/api': {
        target:'http://budget-optimizer-backend-dev:8080',
        changeOrigin: true,
        secure: false
      }
    }
  },

  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    emptyOutDir: true
  }
})
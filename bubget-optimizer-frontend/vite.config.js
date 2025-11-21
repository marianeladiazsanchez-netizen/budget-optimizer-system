import { defineConfig } from 'vite'

export default defineConfig({
  root: './src',
  base: './',
  
  server: {
    port: 5173,
    host: true,
    
    // ⭐ PROXY PARA EVITAR CORS EN DESARROLLO
    // Redirige /api/* al backend automáticamente
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        // rewrite: (path) => path // No reescribir, mantener /api
      }
    }
  },
  
  build: {
    outDir: '../dist',
    assetsDir: 'assets',
    emptyOutDir: true
  },
  
  // ⚠️ IMPORTANTE: Axios es global desde CDN
  // No necesitas importarlo como módulo
  optimizeDeps: {
    exclude: ['axios'] // Axios viene del CDN
  }
})
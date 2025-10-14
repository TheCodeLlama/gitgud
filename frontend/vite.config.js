import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite';
import path from 'path';

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    tailwindcss()
  ],
  // Load environment variables from project root (one level up)
  envDir: path.resolve(__dirname, '..'),
  // Server configuration
  server: {
    port: 5173,
    host: true,
  },
})

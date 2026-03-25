import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { githubPortfolioPlugin } from './github-portfolio-plugin'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), githubPortfolioPlugin()],
})

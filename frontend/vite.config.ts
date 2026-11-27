import { defineConfig, loadEnv } from 'vite';
import { svelte } from '@sveltejs/vite-plugin-svelte';

// During `npm run dev`, /api and /schemas are proxied to a running BRAVA backend.
// Python (`uv run brava serve --port 8765`): http://localhost:8765/brapivalidator (default)
// Java   (`mvn jetty:run`):      http://localhost:8080/brapivalidator
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const backend = (env.BRAVA_BACKEND || 'http://localhost:8765/brapivalidator').replace(/\/$/, '');
  const { origin, pathname } = new URL(backend);
  const proxy = {
    target: origin,
    changeOrigin: true,
    rewrite: (path: string) => pathname + path,
  };

  return {
    plugins: [svelte()],
    // Relative asset URLs, so the build works under any context path (e.g. /brapivalidator/).
    base: './',
    server: {
      proxy: { '/api': proxy, '/schemas': proxy },
    },
    build: {
      outDir: 'dist',
      emptyOutDir: true,
    },
  };
});

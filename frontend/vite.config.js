import {defineConfig} from 'vite';
import react from '@vitejs/plugin-react';
import * as path from 'node:path';

export default defineConfig({
    plugins: [
        react(),
        /*  visualizer({
              open: true,
              gzipSize: true,
              brotliSize: true
          })*/
    ],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, 'src'),
        },
    },
    build: {
        chunkSizeWarningLimit: 1600,
        rollupOptions: {
            output: {
                manualChunks(id) {
                    if (id.includes('node_modules/firebase') ||
                        id.includes('node_modules/@firebase')) {
                        return 'firebase-vendor';
                    }

                    if (id.includes('node_modules/xlsx')) {
                        return 'xlsx-vendor';
                    }

                    if (id.includes('node_modules')) {
                        return 'vendor';
                    }
                },
            }
        },
    }
});
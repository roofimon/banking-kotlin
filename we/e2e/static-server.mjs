// Zero-dependency static file server with SPA fallback, used by the E2E
// "static" path. Serving the prebuilt esbuild bundle avoids the ~25s lazy
// dev-server compile that `ng serve` pays on the first navigation of a run.
import { createServer } from 'node:http';
import { readFile, stat } from 'node:fs/promises';
import { join, extname, normalize } from 'node:path';

const ROOT = join(process.cwd(), 'dist', 'we', 'browser');
const PORT = Number(process.env.E2E_STATIC_PORT) || 4200;

const TYPES = {
  '.html': 'text/html',
  '.js': 'text/javascript',
  '.css': 'text/css',
  '.ico': 'image/x-icon',
  '.json': 'application/json',
  '.svg': 'image/svg+xml',
  '.woff2': 'font/woff2',
};

async function sendFile(res, filePath) {
  const body = await readFile(filePath);
  res.writeHead(200, { 'Content-Type': TYPES[extname(filePath)] || 'application/octet-stream' });
  res.end(body);
}

const server = createServer(async (req, res) => {
  try {
    const urlPath = decodeURIComponent((req.url || '/').split('?')[0]);
    const candidate = join(ROOT, normalize(urlPath));
    // Serve the asset if it exists and is a file; otherwise fall back to
    // index.html so Angular's client-side router handles deep routes.
    if (candidate.startsWith(ROOT) && (await stat(candidate).catch(() => null))?.isFile()) {
      await sendFile(res, candidate);
    } else {
      await sendFile(res, join(ROOT, 'index.html'));
    }
  } catch {
    res.writeHead(500);
    res.end('static-server error');
  }
});

server.listen(PORT, () => console.log(`static server serving ${ROOT} on http://localhost:${PORT}`));

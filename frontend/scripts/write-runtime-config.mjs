import { writeFileSync } from 'node:fs';

const apiUrl = (process.env.ATLAS_API_URL || 'http://localhost:8091').trim().replace(/\/+$/, '');
const safeApiUrl = apiUrl || 'http://localhost:8091';

writeFileSync(
  new URL('../public/atlas-config.js', import.meta.url),
  `window.ATLAS_CONFIG = {\n  apiUrl: ${JSON.stringify(safeApiUrl)}\n};\n`
);

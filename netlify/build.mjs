import { cp, mkdir, readFile, rm, writeFile } from 'node:fs/promises';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const publish = resolve(root, 'netlify/public');
const backendUrl = (process.env.BACKEND_URL ?? '').trim().replace(/\/$/, '');
const apiBaseUrl = (process.env.API_BASE_URL ?? (backendUrl ? '/api' : '')).trim().replace(/\/$/, '');
if (backendUrl && !/^https:\/\//i.test(backendUrl)) {
  throw new Error('BACKEND_URL must be an HTTPS URL, for example https://api.example.com');
}
if (apiBaseUrl && !apiBaseUrl.startsWith('/') && !/^https:\/\//i.test(apiBaseUrl)) {
  throw new Error('API_BASE_URL must be an HTTPS URL or a site-relative path such as /api');
}
await rm(publish, { recursive: true, force: true });
await mkdir(resolve(publish, 'assets'), { recursive: true });
await cp(resolve(root, 'netlify/site'), publish, { recursive: true });
await cp(resolve(root, 'src/main/webapp/assets/style.css'), resolve(publish, 'assets/style.css'));
await writeFile(resolve(publish, 'assets/config.js'), `window.ONEWISH_CONFIG = ${JSON.stringify({ apiBaseUrl })};\n`);
const redirects = [];
if (backendUrl) redirects.push(`/api/* ${backendUrl}/api/:splat 200!`);
redirects.push('/* /index.html 200');
await writeFile(resolve(publish, '_redirects'), `${redirects.join('\n')}\n`);
console.log(`Prepared static Onewish site in ${publish}; API base URL ${apiBaseUrl || '(not configured)'}.`);

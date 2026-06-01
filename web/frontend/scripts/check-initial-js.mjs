#!/usr/bin/env node
/**
 * Bundle budget for the INITIAL JavaScript payload (GH-91, NFR-P-2).
 *
 * Unlike a static size-limit glob list, this follows the entry's actual eager
 * dependency graph: it reads dist/index.html and sums the gzipped size of the
 * entry <script> plus every <link rel="modulepreload"> chunk. A newly
 * introduced eager chunk is therefore counted automatically, so the budget
 * cannot silently miss a shared initial chunk (PR #107 review, P2).
 */
import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { gzipSync } from 'node:zlib';

const BUDGET_BYTES = 215 * 1024; // gzipped; calibrated to the measured build + headroom
const DIST = join(dirname(fileURLToPath(import.meta.url)), '..', 'dist');

const html = readFileSync(join(DIST, 'index.html'), 'utf8');

const hrefs = new Set();
for (const m of html.matchAll(/<script\b[^>]*\bsrc="([^"]+\.js)"/g)) hrefs.add(m[1]);
for (const m of html.matchAll(/<link\b[^>]*\brel="modulepreload"[^>]*\bhref="([^"]+\.js)"/g)) {
  hrefs.add(m[1]);
}

if (hrefs.size === 0) {
  console.error('check-initial-js: no initial JS assets found in dist/index.html');
  process.exit(1);
}

const kb = (bytes) => `${(bytes / 1024).toFixed(2)} kB`;
const rows = [...hrefs]
  .map((href) => ({ href, gz: gzipSync(readFileSync(join(DIST, href.replace(/^\//, '')))).length }))
  .sort((a, b) => b.gz - a.gz);

const total = rows.reduce((sum, r) => sum + r.gz, 0);
for (const { href, gz } of rows) console.log(`  ${kb(gz).padStart(10)}  ${href}`);
console.log(`  ${'-'.repeat(10)}`);
console.log(`  ${kb(total).padStart(10)}  initial JS (gzip), budget ${kb(BUDGET_BYTES)}`);

if (total > BUDGET_BYTES) {
  console.error(`\ncheck-initial-js: initial JS ${kb(total)} exceeds budget ${kb(BUDGET_BYTES)}`);
  process.exit(1);
}
console.log('\ncheck-initial-js: within budget');

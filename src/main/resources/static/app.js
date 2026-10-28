// Shared helpers for the read-only pages.
const $ = (s) => document.querySelector(s);
const esc = (v) => String(v ?? '').replace(/[&<>"]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));
const fmt = (t) => (t ? new Date(t).toISOString().replace('T', ' ').slice(0, 19) + 'Z' : '—');
const badge = (s) => `<span class="badge ${s === 'IN_SYNC' ? 'ok' : ''}">${esc(s)}</span>`;

function state(el, title, text, cls = '') {
  el.innerHTML = `<div class="state ${cls}"><h2>${esc(title)}</h2><p>${esc(text)}</p></div>`;
}

/** Fetches JSON and renders loading / 501 / error states into `el`. Resolves to data or null. */
async function load(url, el) {
  state(el, 'Loading…', '');
  let res;
  try { res = await fetch(url); } catch (e) { state(el, 'Could not reach the server', String(e), 'error'); return null; }
  if (res.status === 501) { state(el, 'This API is not implemented yet', `${url} returned 501.`); return null; }
  if (res.status === 404) { state(el, 'Not found', `${url} returned 404.`); return null; }
  if (!res.ok) { state(el, 'Something went wrong', `${url} returned HTTP ${res.status}.`, 'error'); return null; }
  try { return await res.json(); } catch (e) { state(el, 'Unexpected response', 'The response was not valid JSON.', 'error'); return null; }
}

function nav(active) {
  document.body.insertAdjacentHTML('afterbegin',
    `<header><strong>Hub</strong>
      <a href="/" class="${active === 'fleet' ? 'active' : ''}">Fleet</a>
      <a href="/coverage.html" class="${active === 'coverage' ? 'active' : ''}">Policy coverage</a></header>`);
}

const policyCell = (e) => esc(e.policy?.name ?? e.policy?.id ?? '—');

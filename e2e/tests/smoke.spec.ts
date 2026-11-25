import { test, expect } from '@playwright/test';

// Mirrors the contract in avk.yaml (the deployer runs the same checks after a traffic switch). An API has no pages, so there is no
// accessibility scan here; the platform's release run still scans and tests the container.

test('liveness and readiness answer 200', async ({ request }) => {
  expect((await request.get('/healthz')).status()).toBe(200);
  const ready = await request.get('/readyz');
  expect(ready.status()).toBe(200);
  expect(await ready.json()).toEqual({ status: 'ready' });
});

test('ping is public and answers pong', async ({ request }) => {
  const res = await request.get('/api/v1/ping');
  expect(res.status()).toBe(200);
  expect(await res.json()).toEqual({ message: 'pong' });
});

test('every response carries a request id', async ({ request }) => {
  const res = await request.get('/api/v1/ping', { headers: { 'X-Request-Id': 'e2e-request-0001' } });
  expect(res.headers()['x-request-id']).toBe('e2e-request-0001');
});

test('protected paths need a bearer token and never leak internals', async ({ request }) => {
  for (const path of ['/api/v1/me', '/admin', '/definitely-not-here']) {
    const res = await request.get(path, { maxRedirects: 0 });
    expect([401, 403, 404], path).toContain(res.status());
    expect(await res.text()).not.toMatch(/Exception|at com\.|org\.springframework/);
  }
});

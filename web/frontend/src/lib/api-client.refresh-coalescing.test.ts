/**
 * Concurrent refresh coalescing tests (ADR-0013 / ADR-0018, GH-34).
 *
 * Verifies that two parallel requests that both receive 401 trigger exactly one
 * refresh call, not two — preventing the double-refresh that ADR-0013's
 * family-revocation policy would turn into an involuntary logout.
 */
import { afterEach, describe, expect, it, vi } from 'vitest';

import {
  ApiError,
  fetchJson,
  installAuthRefreshInterceptor,
  resetAuthRefreshInterceptor,
} from './api-client';

// Minimal 401 response factory.
const make401 = () => new Response(null, { status: 401 });

describe('fetchJson concurrent refresh coalescing', () => {
  afterEach(() => {
    resetAuthRefreshInterceptor();
    vi.restoreAllMocks();
  });

  it('coalesces parallel 401 responses into a single refresh call', async () => {
    // ARRANGE
    const refresh = vi.fn().mockResolvedValue(undefined);
    const onLogout = vi.fn();
    installAuthRefreshInterceptor({ refresh, onLogout });

    // First two fetch calls (the original concurrent requests) return 401;
    // all subsequent calls (retries after refresh) return 200.
    let callCount = 0;
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => {
        callCount += 1;
        if (callCount <= 2) {
          return new Response(null, { status: 401 });
        }
        return new Response(JSON.stringify({ ok: true }), { status: 200 });
      }),
    );

    // ACT — fire two requests concurrently so both 401s race
    const [r1, r2] = await Promise.all([
      fetchJson<{ ok: boolean }>('/api/v1/me'),
      fetchJson<{ ok: boolean }>('/api/v1/dashboard/summary'),
    ]);

    // ASSERT — exactly one refresh despite two 401s
    expect(refresh).toHaveBeenCalledTimes(1);
    expect(onLogout).not.toHaveBeenCalled();
    expect(r1).toEqual({ ok: true });
    expect(r2).toEqual({ ok: true });
  });

  it('calls onLogout exactly once when concurrent refresh fails', async () => {
    // ARRANGE
    const refresh = vi.fn().mockRejectedValue(new Error('refresh failed'));
    const onLogout = vi.fn();
    installAuthRefreshInterceptor({ refresh, onLogout });

    vi.stubGlobal(
      'fetch',
      vi.fn(async () => new Response(null, { status: 401 })),
    );

    // ACT — both requests get 401, share the same failing refresh
    await expect(
      Promise.all([fetchJson('/api/v1/me'), fetchJson('/api/v1/dashboard/summary')]),
    ).rejects.toBeInstanceOf(ApiError);

    // ASSERT — single refresh call, single onLogout (fired in the promise chain,
    // not per-caller, so there is no double-redirect on concurrent failure)
    expect(refresh).toHaveBeenCalledTimes(1);
    expect(onLogout).toHaveBeenCalledTimes(1);
  });

  it('handles a synchronously-throwing refresh as a standardised 401 ApiError', async () => {
    // refresh() is typed () => Promise<void> but a non-async implementation
    // could throw synchronously before returning a promise.
    const onLogout = vi.fn();
    installAuthRefreshInterceptor({
      refresh: () => {
        throw new Error('synchronous failure');
      },
      onLogout,
    });

    vi.stubGlobal(
      'fetch',
      vi.fn(async () => make401()),
    );

    // ASSERT — caller receives standardised ApiError, not the raw sync error
    await expect(fetchJson('/api/v1/me')).rejects.toBeInstanceOf(ApiError);
    expect(onLogout).toHaveBeenCalledTimes(1);
  });
});

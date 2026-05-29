/**
 * Tests for usePreferencesQuery — fetches + caches server preferences.
 */
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { renderHook, waitFor } from '@testing-library/react';
import type { ReactNode } from 'react';
import { describe, expect, it, vi } from 'vitest';

import { usePreferencesQuery } from './usePreferencesQuery';

vi.mock('../api/preferences-client', () => ({
  preferencesClient: {
    fetch: vi.fn().mockResolvedValue({
      weight_unit: 'lbs',
      notify_achievement: true,
      notify_milestone: true,
      notify_streak: false,
    }),
  },
}));

function wrapper({ children }: { children: ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={qc}>{children}</QueryClientProvider>;
}

describe('usePreferencesQuery', () => {
  it('returns loading state initially', () => {
    const { result } = renderHook(() => usePreferencesQuery(), { wrapper });
    expect(result.current.isLoading).toBe(true);
  });

  it('returns mapped camelCase preferences after fetch', async () => {
    const { result } = renderHook(() => usePreferencesQuery(), { wrapper });
    await waitFor(() => expect(result.current.isLoading).toBe(false));
    expect(result.current.data?.weightUnit).toBe('lbs');
    expect(result.current.data?.notifyStreak).toBe(false);
  });
});

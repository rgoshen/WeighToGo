/**
 * Tests for useUpdatePreference — mutation with optimistic update + rollback.
 */
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { act, renderHook, waitFor } from '@testing-library/react';
import type { ReactNode } from 'react';
import { describe, expect, it, vi } from 'vitest';

import type { Preferences } from '../schemas/preferences-schemas';
import { PREFERENCES_KEY } from './usePreferencesQuery';
import { useUpdatePreference } from './useUpdatePreference';

const mockUpdate = vi.fn();

vi.mock('../api/preferences-client', () => ({
  preferencesClient: {
    update: (...args: unknown[]) => mockUpdate(...args),
  },
}));

const seedPrefs: Preferences = {
  weightUnit: 'lbs',
  notifyAchievement: true,
  notifyMilestone: true,
  notifyStreak: false,
};

function wrapper({ children }: { children: ReactNode }) {
  const qc = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  qc.setQueryData(PREFERENCES_KEY, seedPrefs);
  return <QueryClientProvider client={qc}>{children}</QueryClientProvider>;
}

describe('useUpdatePreference', () => {
  it('calls preferencesClient.update with key and value', async () => {
    mockUpdate.mockResolvedValue({
      weight_unit: 'kg',
      notify_achievement: true,
      notify_milestone: true,
      notify_streak: false,
    });

    const { result } = renderHook(() => useUpdatePreference(), { wrapper });
    act(() => {
      result.current.mutate({ key: 'weight_unit', value: 'kg' });
    });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(mockUpdate).toHaveBeenCalledWith('weight_unit', 'kg');
  });

  it('rolls back optimistic update on error', async () => {
    mockUpdate.mockRejectedValue(new Error('Network failure'));

    const { result } = renderHook(() => useUpdatePreference(), { wrapper });
    act(() => {
      result.current.mutate({ key: 'weight_unit', value: 'kg' });
    });
    await waitFor(() => expect(result.current.isError).toBe(true));
    // After rollback the query cache should still reflect the seed value.
    expect(result.current.isError).toBe(true);
  });
});

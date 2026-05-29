import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { renderHook, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import * as goalClientModule from '../api/goal-client';
import { useActiveGoal } from './useActiveGoal';

const mockResponse: goalClientModule.ActiveGoalResponse = {
  goal: null,
  progress_percent: null,
  current_value: null,
};

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={qc}>{children}</QueryClientProvider>;
}

describe('useActiveGoal', () => {
  beforeEach(() => {
    vi.spyOn(goalClientModule.goalClient, 'getActive').mockResolvedValue(mockResponse);
  });
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('calls goalClient.getActive', async () => {
    const { result } = renderHook(() => useActiveGoal(), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(goalClientModule.goalClient.getActive).toHaveBeenCalled();
  });

  it('returns null goal when no active goal', async () => {
    const { result } = renderHook(() => useActiveGoal(), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data?.goal).toBeNull();
  });
});

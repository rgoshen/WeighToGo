import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { renderHook, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import * as goalClientModule from '../api/goal-client';
import { useAbandonGoal } from './useAbandonGoal';

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={qc}>{children}</QueryClientProvider>;
}

describe('useAbandonGoal', () => {
  beforeEach(() => {
    vi.spyOn(goalClientModule.goalClient, 'abandon').mockResolvedValue(undefined);
  });
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('calls goalClient.abandon on mutate', async () => {
    const { result } = renderHook(() => useAbandonGoal(), { wrapper });
    result.current.mutate(1);
    await waitFor(() => expect(goalClientModule.goalClient.abandon).toHaveBeenCalledWith(1));
  });
});

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { renderHook, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import * as goalClientModule from '../api/goal-client';
import { useSetGoal } from './useSetGoal';

const mockGoal: goalClientModule.GoalRecord = {
  goal_id: 1,
  user_id: 1,
  target_value: 150,
  target_unit: 'lbs',
  start_value: 200,
  goal_type: 'lose',
  target_date: null,
  is_active: true,
  is_achieved: false,
  achieved_at: null,
  created_at: '2026-05-28T00:00:00Z',
  updated_at: '2026-05-28T00:00:00Z',
};

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={qc}>{children}</QueryClientProvider>;
}

describe('useSetGoal', () => {
  beforeEach(() => {
    vi.spyOn(goalClientModule.goalClient, 'create').mockResolvedValue(mockGoal);
  });
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('calls goalClient.create on mutate', async () => {
    const { result } = renderHook(() => useSetGoal(), { wrapper });
    const payload = {
      goal_type: 'lose',
      target_value: 150,
      target_unit: 'lbs',
      start_value: 200,
      target_date: null,
    };
    result.current.mutate(payload);
    await waitFor(() => expect(goalClientModule.goalClient.create).toHaveBeenCalledWith(payload));
  });
});

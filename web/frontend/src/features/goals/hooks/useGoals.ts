import { useQuery } from '@tanstack/react-query';
import { type GoalListResponse, goalClient } from '../api/goal-client';

export const GOALS_LIST_KEY = ['goals', 'list'] as const;

/** Return all goals (active and historical) for the current user. */
export function useGoals() {
  return useQuery<GoalListResponse>({
    queryKey: GOALS_LIST_KEY,
    queryFn: () => goalClient.list(),
  });
}

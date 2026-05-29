import { useQuery } from '@tanstack/react-query';
import { type ActiveGoalResponse, goalClient } from '../api/goal-client';

export const ACTIVE_GOAL_KEY = ['goals', 'active'] as const;

/** Return the active goal with progress for the current user. */
export function useActiveGoal() {
  return useQuery<ActiveGoalResponse>({
    queryKey: ACTIVE_GOAL_KEY,
    queryFn: () => goalClient.getActive(),
  });
}

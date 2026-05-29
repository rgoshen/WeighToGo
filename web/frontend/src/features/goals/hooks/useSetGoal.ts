import { useMutation, useQueryClient } from '@tanstack/react-query';
import { type GoalPayload, goalClient } from '../api/goal-client';
import { DASHBOARD_SUMMARY_KEY } from '../../dashboard/hooks/useDashboardSummary';
import { ACTIVE_GOAL_KEY } from './useActiveGoal';
import { GOALS_LIST_KEY } from './useGoals';

/** Create a new active goal and invalidate related queries on success. */
export function useSetGoal() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: GoalPayload) => goalClient.create(payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ACTIVE_GOAL_KEY });
      void queryClient.invalidateQueries({ queryKey: GOALS_LIST_KEY });
      void queryClient.invalidateQueries({ queryKey: DASHBOARD_SUMMARY_KEY });
    },
  });
}

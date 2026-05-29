import { useMutation, useQueryClient } from '@tanstack/react-query';
import { goalClient } from '../api/goal-client';
import { DASHBOARD_SUMMARY_KEY } from '../../dashboard/hooks/useDashboardSummary';
import { ACTIVE_GOAL_KEY } from './useActiveGoal';
import { GOALS_LIST_KEY } from './useGoals';

/** Abandon the current active goal and invalidate related queries. */
export function useAbandonGoal() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (goalId: number) => goalClient.abandon(goalId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ACTIVE_GOAL_KEY });
      void queryClient.invalidateQueries({ queryKey: GOALS_LIST_KEY });
      void queryClient.invalidateQueries({ queryKey: DASHBOARD_SUMMARY_KEY });
    },
  });
}

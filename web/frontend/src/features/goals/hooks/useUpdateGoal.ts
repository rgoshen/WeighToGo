import { useMutation, useQueryClient } from '@tanstack/react-query';
import { type GoalUpdatePayload, goalClient } from '../api/goal-client';
import { DASHBOARD_SUMMARY_KEY } from '../../dashboard/hooks/useDashboardSummary';
import { ACTIVE_GOAL_KEY } from './useActiveGoal';
import { GOALS_LIST_KEY } from './useGoals';

interface UpdateGoalArgs {
  goalId: number;
  payload: GoalUpdatePayload;
}

/** Update a goal's target value / target date and invalidate related queries. */
export function useUpdateGoal() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ goalId, payload }: UpdateGoalArgs) => goalClient.update(goalId, payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ACTIVE_GOAL_KEY });
      void queryClient.invalidateQueries({ queryKey: GOALS_LIST_KEY });
      void queryClient.invalidateQueries({ queryKey: DASHBOARD_SUMMARY_KEY });
    },
  });
}

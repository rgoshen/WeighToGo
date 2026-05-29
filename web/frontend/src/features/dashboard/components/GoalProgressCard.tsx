import { Card, CardContent, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import type { ActiveGoalResponse } from '../../goals/api/goal-client';
import { GoalProgressBar } from '../../goals/components/GoalProgressBar';

interface GoalProgressCardProps {
  /** Active goal envelope from the dashboard summary, or null when none. */
  activeGoal: ActiveGoalResponse | null;
  /** Whether the dashboard summary query is loading. */
  isLoading: boolean;
  /** Whether the dashboard summary query errored. */
  isError: boolean;
}

/**
 * Dashboard card showing goal progress (FR-D-4).
 *
 * Driven by the dashboard summary (single source of truth), matching the
 * sibling LatestEntryCard / TotalEntriesCard prop pattern. The progress-bar
 * visual is unchanged (DDR-0005).
 */
export function GoalProgressCard({ activeGoal, isLoading, isError }: GoalProgressCardProps) {
  const navigate = useNavigate();

  return (
    <Card>
      <CardContent>
        <Typography variant="subtitle2" color="text.secondary" gutterBottom>
          Goal Progress
        </Typography>
        {isLoading ? (
          <Typography variant="body2" color="text.secondary">
            Loading…
          </Typography>
        ) : isError ? (
          <Typography variant="body2" color="error">
            Failed to load goal progress.
          </Typography>
        ) : (
          <GoalProgressBar
            hasGoal={activeGoal?.goal != null}
            progressPercent={activeGoal?.progress_percent ?? null}
            onSetGoal={() => navigate('/goals')}
          />
        )}
      </CardContent>
    </Card>
  );
}

import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';

import type { ActiveGoalResponse } from '../../goals/api/goal-client';
import { GoalProgressCard } from './GoalProgressCard';

function Wrapper({ children }: { children: React.ReactNode }) {
  return <MemoryRouter>{children}</MemoryRouter>;
}

const goalActive: ActiveGoalResponse = {
  goal: {
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
  },
  progress_percent: 50,
  current_value: 175,
};

describe('GoalProgressCard', () => {
  it('renders the Goal Progress title', () => {
    render(<GoalProgressCard activeGoal={null} isLoading={false} isError={false} />, {
      wrapper: Wrapper,
    });
    expect(screen.getByText(/goal progress/i)).toBeInTheDocument();
  });

  it('shows CTA when no active goal', () => {
    render(<GoalProgressCard activeGoal={null} isLoading={false} isError={false} />, {
      wrapper: Wrapper,
    });
    expect(screen.getByText(/set a goal/i)).toBeInTheDocument();
  });

  it('shows loading text while loading', () => {
    render(<GoalProgressCard activeGoal={null} isLoading={true} isError={false} />, {
      wrapper: Wrapper,
    });
    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });

  it('shows error message on error', () => {
    render(<GoalProgressCard activeGoal={null} isLoading={false} isError={true} />, {
      wrapper: Wrapper,
    });
    expect(screen.getByText(/failed to load goal progress/i)).toBeInTheDocument();
  });

  it('shows progress bar when active goal with progress', () => {
    render(<GoalProgressCard activeGoal={goalActive} isLoading={false} isError={false} />, {
      wrapper: Wrapper,
    });
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
    expect(screen.getByText('50%')).toBeInTheDocument();
  });
});

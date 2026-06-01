import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import type { GoalRecord } from '../api/goal-client';
import { GoalHistoryList } from './GoalHistoryList';

// 200 lbs → 90.718… kg (1 dp = "90.7 kg"); 150 lbs → 68.038… kg (1 dp = "68.0 kg")

const ACHIEVED: GoalRecord = {
  goal_id: 1,
  user_id: 1,
  target_value: 150,
  target_unit: 'lbs',
  start_value: 200,
  goal_type: 'lose',
  target_date: null,
  is_active: false,
  is_achieved: true,
  achieved_at: '2026-02-01T00:00:00Z',
  created_at: '2026-01-01T00:00:00Z',
  updated_at: '2026-02-01T00:00:00Z',
};

const ABANDONED: GoalRecord = {
  ...ACHIEVED,
  goal_id: 2,
  is_achieved: false,
  achieved_at: null,
};

describe('GoalHistoryList', () => {
  it('shows an empty-state message when there are no past goals', () => {
    render(<GoalHistoryList goals={[]} preferredUnit="lbs" />);
    expect(screen.getByText(/no past goals/i)).toBeInTheDocument();
  });

  it('renders an achieved goal with an "Achieved" outcome label', () => {
    render(<GoalHistoryList goals={[ACHIEVED]} preferredUnit="lbs" />);
    expect(screen.getByText(/achieved/i)).toBeInTheDocument();
  });

  it('renders an abandoned goal with an "Abandoned" outcome label', () => {
    render(<GoalHistoryList goals={[ABANDONED]} preferredUnit="lbs" />);
    expect(screen.getByText(/abandoned/i)).toBeInTheDocument();
  });

  it('exposes the history as an accessible list', () => {
    render(<GoalHistoryList goals={[ACHIEVED, ABANDONED]} preferredUnit="lbs" />);
    expect(screen.getByRole('list', { name: /goal history/i })).toBeInTheDocument();
    expect(screen.getAllByRole('listitem')).toHaveLength(2);
  });

  it('converts lbs-stored goal values to kg when preferredUnit is kg', () => {
    render(<GoalHistoryList goals={[ACHIEVED]} preferredUnit="kg" />);
    expect(screen.getByText(/90\.7 kg/)).toBeInTheDocument();
    expect(screen.getByText(/68\.0 kg/)).toBeInTheDocument();
  });

  it('formats lbs-stored goal values in lbs when preferredUnit is lbs', () => {
    render(<GoalHistoryList goals={[ACHIEVED]} preferredUnit="lbs" />);
    expect(screen.getByText(/200\.0 lbs/)).toBeInTheDocument();
    expect(screen.getByText(/150\.0 lbs/)).toBeInTheDocument();
  });
});

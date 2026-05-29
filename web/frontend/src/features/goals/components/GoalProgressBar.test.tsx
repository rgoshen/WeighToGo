import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { GoalProgressBar } from './GoalProgressBar';

describe('GoalProgressBar', () => {
  it('renders CTA when no active goal', () => {
    render(<GoalProgressBar hasGoal={false} progressPercent={null} />);
    expect(screen.getByText(/set a goal/i)).toBeInTheDocument();
  });

  it('renders "No entries yet" when goal exists but no entries', () => {
    render(<GoalProgressBar hasGoal={true} progressPercent={null} />);
    expect(screen.getByText(/no entries yet/i)).toBeInTheDocument();
  });

  it('renders progress bar with percent when progress is known', () => {
    render(<GoalProgressBar hasGoal={true} progressPercent={50} />);
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
    expect(screen.getByText('50%')).toBeInTheDocument();
  });

  it('calls onSetGoal when CTA is clicked', async () => {
    const onSetGoal = vi.fn();
    render(<GoalProgressBar hasGoal={false} progressPercent={null} onSetGoal={onSetGoal} />);
    const cta = screen.getByRole('button', { name: /set a goal/i });
    await userEvent.click(cta);
    expect(onSetGoal).toHaveBeenCalledOnce();
  });
});

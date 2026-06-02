import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import type { ReactNode } from 'react';
import { afterEach, describe, expect, it, vi } from 'vitest';

import * as achClientModule from '../api/achievement-client';
import { AchievementsPage } from './AchievementsPage';

const prefs = { current: 'lbs' as 'lbs' | 'kg' };
vi.mock('../../../contexts/PreferencesContext', () => ({
  usePreferences: () => ({
    preferences: {
      weightUnit: prefs.current,
      notifyAchievement: true,
      notifyMilestone: true,
      notifyStreak: true,
    },
    isLoading: false,
    setPreference: vi.fn(),
  }),
}));

function wrapper({ children }: { children: ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={qc}>{children}</QueryClientProvider>;
}

describe('AchievementsPage', () => {
  afterEach(() => {
    vi.restoreAllMocks();
    prefs.current = 'lbs';
  });

  it('renders the page heading', async () => {
    vi.spyOn(achClientModule.achievementClient, 'list').mockResolvedValue({ items: [] });
    render(<AchievementsPage />, { wrapper });
    await waitFor(() =>
      expect(screen.getByRole('heading', { name: /achievements/i })).toBeInTheDocument(),
    );
  });

  it('shows empty-state message when no achievements', async () => {
    vi.spyOn(achClientModule.achievementClient, 'list').mockResolvedValue({ items: [] });
    render(<AchievementsPage />, { wrapper });
    await waitFor(() => expect(screen.getByText(/no achievements yet/i)).toBeInTheDocument());
  });

  it('renders an earned milestone achievement in lbs when preferred unit is lbs', async () => {
    vi.spyOn(achClientModule.achievementClient, 'list').mockResolvedValue({
      items: [
        {
          achievement_id: 1,
          goal_id: 7,
          achievement_type: 'milestone',
          threshold: 5,
          earned_at: '2026-01-01T00:00:00Z',
        },
      ],
    });
    render(<AchievementsPage />, { wrapper });
    await waitFor(() => expect(screen.getByText(/5\.0 lbs milestone/i)).toBeInTheDocument());
  });

  it('converts milestone threshold to kg when preferred unit is kg', async () => {
    prefs.current = 'kg';
    vi.spyOn(achClientModule.achievementClient, 'list').mockResolvedValue({
      items: [
        {
          achievement_id: 1,
          goal_id: 7,
          achievement_type: 'milestone',
          threshold: 5,
          earned_at: '2026-01-01T00:00:00Z',
        },
      ],
    });
    render(<AchievementsPage />, { wrapper });
    // 5 lbs * 0.45359237 ≈ 2.3 kg (1 dp)
    await waitFor(() => expect(screen.getByText(/2\.3 kg milestone/i)).toBeInTheDocument());
  });

  it('renders a streak achievement with day-count label', async () => {
    vi.spyOn(achClientModule.achievementClient, 'list').mockResolvedValue({
      items: [
        {
          achievement_id: 3,
          goal_id: 7,
          achievement_type: 'streak',
          threshold: 7,
          earned_at: '2026-01-07T00:00:00Z',
        },
      ],
    });
    render(<AchievementsPage />, { wrapper });
    await waitFor(() => expect(screen.getByText(/7-day streak/i)).toBeInTheDocument());
  });

  it('falls back to a plain streak label when threshold is null (no NaN)', async () => {
    vi.spyOn(achClientModule.achievementClient, 'list').mockResolvedValue({
      items: [
        {
          achievement_id: 5,
          goal_id: 7,
          achievement_type: 'streak',
          threshold: null,
          earned_at: '2026-01-07T00:00:00Z',
        },
      ],
    });
    render(<AchievementsPage />, { wrapper });
    await waitFor(() => expect(screen.getByText('Streak')).toBeInTheDocument());
  });

  it('falls back to a plain milestone label when threshold is null (no NaN)', async () => {
    vi.spyOn(achClientModule.achievementClient, 'list').mockResolvedValue({
      items: [
        {
          achievement_id: 6,
          goal_id: 7,
          achievement_type: 'milestone',
          threshold: null,
          earned_at: '2026-01-01T00:00:00Z',
        },
      ],
    });
    render(<AchievementsPage />, { wrapper });
    await waitFor(() => expect(screen.getByText('Milestone')).toBeInTheDocument());
  });

  it('renders a goal_reached achievement with correct label', async () => {
    vi.spyOn(achClientModule.achievementClient, 'list').mockResolvedValue({
      items: [
        {
          achievement_id: 2,
          goal_id: 7,
          achievement_type: 'goal_reached',
          threshold: null,
          earned_at: '2026-01-01T00:00:00Z',
        },
      ],
    });
    render(<AchievementsPage />, { wrapper });
    await waitFor(() => expect(screen.getByText(/goal reached/i)).toBeInTheDocument());
  });
});

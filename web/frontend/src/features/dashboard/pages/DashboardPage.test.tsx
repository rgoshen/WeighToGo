import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';

import { dashboardClient } from '../api/dashboard-client';
import type { DashboardSummaryResponse } from '../api/dashboard-client';
import { DashboardPage } from './DashboardPage';

const emptySummary: DashboardSummaryResponse = {
  latest_entry: null,
  total_entries: 0,
  active_goal: null,
};

const populatedSummary: DashboardSummaryResponse = {
  latest_entry: {
    entry_id: 1,
    weight_value: 175.5,
    weight_unit: 'lbs',
    observation_date: '2026-05-20',
    notes: null,
    created_at: '2026-05-20T12:00:00Z',
    updated_at: '2026-05-20T12:00:00Z',
  },
  total_entries: 1,
  active_goal: null,
};

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return (
    <QueryClientProvider client={qc}>
      <MemoryRouter>{children}</MemoryRouter>
    </QueryClientProvider>
  );
}

describe('DashboardPage', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('shows empty state CTA when total_entries is 0', async () => {
    vi.spyOn(dashboardClient, 'summary').mockResolvedValue(emptySummary);
    render(<DashboardPage />, { wrapper });
    await waitFor(() => expect(screen.getByText(/add your first entry/i)).toBeInTheDocument());
  });

  it('shows the three cards when entries exist', async () => {
    vi.spyOn(dashboardClient, 'summary').mockResolvedValue(populatedSummary);
    render(<DashboardPage />, { wrapper });
    await waitFor(() => expect(screen.getByText(/175/)).toBeInTheDocument());
    expect(screen.getByText(/milestone 3/i)).toBeInTheDocument();
  });

  it('has an accessible heading', async () => {
    vi.spyOn(dashboardClient, 'summary').mockResolvedValue(emptySummary);
    render(<DashboardPage />, { wrapper });
    expect(screen.getByRole('heading', { name: /dashboard/i })).toBeInTheDocument();
  });
});

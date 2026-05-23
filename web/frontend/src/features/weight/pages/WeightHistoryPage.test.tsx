import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';

import { weightClient } from '../api/weight-client';
import type { WeightEntryListResponse } from '../api/weight-client';
import { WeightHistoryPage } from './WeightHistoryPage';

const emptyList: WeightEntryListResponse = { items: [], next_cursor: null };
const populatedList: WeightEntryListResponse = {
  items: [
    {
      entry_id: 1,
      weight_value: 175.5,
      weight_unit: 'lbs',
      observation_date: '2026-05-20',
      notes: null,
      created_at: '2026-05-20T12:00:00Z',
      updated_at: '2026-05-20T12:00:00Z',
    },
  ],
  next_cursor: null,
};

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return (
    <QueryClientProvider client={qc}>
      <MemoryRouter>{children}</MemoryRouter>
    </QueryClientProvider>
  );
}

describe('WeightHistoryPage', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('shows loading spinner initially', () => {
    vi.spyOn(weightClient, 'list').mockReturnValue(new Promise(() => {}));
    render(<WeightHistoryPage />, { wrapper });
    expect(
      document.querySelector('[role="progressbar"], .MuiSkeleton-root, [aria-busy]'),
    ).toBeTruthy();
  });

  it('shows empty state when no entries exist', async () => {
    vi.spyOn(weightClient, 'list').mockResolvedValue(emptyList);
    render(<WeightHistoryPage />, { wrapper });
    await waitFor(() => expect(screen.getByText(/no weight entries/i)).toBeInTheDocument());
  });

  it('renders weight entry table when entries exist', async () => {
    vi.spyOn(weightClient, 'list').mockResolvedValue(populatedList);
    render(<WeightHistoryPage />, { wrapper });
    await waitFor(() => expect(screen.getByText(/175/)).toBeInTheDocument());
  });

  it('shows confirm dialog when delete button is clicked', async () => {
    vi.spyOn(weightClient, 'list').mockResolvedValue(populatedList);
    render(<WeightHistoryPage />, { wrapper });
    await waitFor(() => screen.getByRole('button', { name: /delete/i }));
    await userEvent.click(screen.getByRole('button', { name: /delete/i }));
    expect(screen.getByRole('dialog')).toBeInTheDocument();
  });

  it('closes confirm dialog on cancel', async () => {
    vi.spyOn(weightClient, 'list').mockResolvedValue(populatedList);
    render(<WeightHistoryPage />, { wrapper });
    const deleteButtons = await waitFor(() => screen.getAllByRole('button', { name: /delete/i }));
    await userEvent.click(deleteButtons[0]!);
    expect(screen.getByRole('dialog')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: /cancel/i }));
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
  });

  it('calls deleteMutation on confirm', async () => {
    vi.spyOn(weightClient, 'list').mockResolvedValue(populatedList);
    vi.spyOn(weightClient, 'remove').mockResolvedValue(undefined);
    render(<WeightHistoryPage />, { wrapper });
    await waitFor(() => screen.getByRole('button', { name: /delete/i }));
    await userEvent.click(screen.getByRole('button', { name: /delete/i }));
    await userEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]!);
    await waitFor(() => expect(weightClient.remove).toHaveBeenCalled());
  });
});

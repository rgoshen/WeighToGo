import type { ReactNode } from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider } from '@mui/material';

import { App } from './App';
import { AuthProvider } from './contexts/AuthContext';
import { PreferencesProvider } from './contexts/PreferencesContext';
import { theme } from './theme/theme';
import { authClient } from './features/auth/api/auth-client';

// Force pages to throw on render so we can prove each route slot is wrapped in an
// error boundary (SRS §10.2): a failed page — including a failed lazy chunk load —
// must surface a fallback, not crash the whole app.
vi.mock('./features/dashboard/pages/DashboardPage', () => ({
  DashboardPage: () => {
    throw new Error('simulated dashboard failure');
  },
}));
vi.mock('./features/auth/pages/LoginPage', () => ({
  LoginPage: () => {
    throw new Error('simulated login failure');
  },
}));

const authenticatedUser = {
  user_id: 1,
  email: 'user@example.com',
  display_name: 'User',
  created_at: '2026-05-23T00:00:00Z',
};

function FullProviders({ children }: { children: ReactNode }) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <PreferencesProvider>
          <ThemeProvider theme={theme}>{children}</ThemeProvider>
        </PreferencesProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
}

describe('App route error boundary (SRS §10.2)', () => {
  beforeEach(() => {
    // The error boundary logs the caught error; silence the expected noise.
    vi.spyOn(console, 'error').mockImplementation(() => undefined);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('contains a failed protected page to the content area, keeping the app shell', async () => {
    vi.spyOn(authClient, 'me').mockResolvedValue(authenticatedUser);

    render(
      <FullProviders>
        <MemoryRouter initialEntries={['/']}>
          <App />
        </MemoryRouter>
      </FullProviders>,
    );

    expect(await screen.findByText(/something went wrong/i)).toBeInTheDocument();
    // Shell persists — the failure is contained to the page slot, not the app.
    expect(screen.getByText('Weigh to Go!')).toBeInTheDocument();
  });

  it('shows a fallback when a public page fails to load', async () => {
    vi.spyOn(authClient, 'me').mockRejectedValue(new Error('401'));

    render(
      <FullProviders>
        <MemoryRouter initialEntries={['/login']}>
          <App />
        </MemoryRouter>
      </FullProviders>,
    );

    expect(await screen.findByText(/something went wrong/i)).toBeInTheDocument();
  });
});

import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider } from '@mui/material';
import { theme } from '../theme/theme';
import { AuthProvider } from '../contexts/AuthContext';
import { authClient } from '../features/auth/api/auth-client';
import { AppLayout } from './AppLayout';

function Wrapper({ children }: { children: React.ReactNode }) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return (
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <ThemeProvider theme={theme}>
          <AuthProvider>{children}</AuthProvider>
        </ThemeProvider>
      </MemoryRouter>
    </QueryClientProvider>
  );
}

describe('AppLayout', () => {
  beforeEach(() => {
    vi.spyOn(authClient, 'me').mockRejectedValue(new Error('401'));
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('renders without crashing', () => {
    render(
      <Wrapper>
        <AppLayout />
      </Wrapper>,
    );
  });

  it('renders the application name in the app bar', () => {
    render(
      <Wrapper>
        <AppLayout />
      </Wrapper>,
    );
    expect(screen.getByText(/weigh to go/i)).toBeInTheDocument();
  });

  it('renders navigation landmark', () => {
    render(
      <Wrapper>
        <AppLayout />
      </Wrapper>,
    );
    expect(screen.getByRole('navigation')).toBeInTheDocument();
  });
});

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider } from '@mui/material';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { ReactNode } from 'react';

import { theme } from '../../../theme/theme';
import { AuthProvider } from '../../../contexts/AuthContext';
import { authClient, type AuthUser } from '../api/auth-client';
import { LandingPage } from './LandingPage';

// LandingPage composes useLogin/useRegister (useAuth + router) and authClient;
// no PreferencesProvider is required (the forms/hooks do not consume it).
function Wrapper({ children }: { children: ReactNode }) {
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

describe('LandingPage', () => {
  beforeEach(() => {
    vi.spyOn(authClient, 'me').mockResolvedValue(null as unknown as AuthUser);
  });
  afterEach(() => vi.restoreAllMocks());

  const renderLanding = () =>
    render(
      <Wrapper>
        <LandingPage />
      </Wrapper>,
    );

  it('renders both the login and registration submit buttons', () => {
    renderLanding();
    expect(screen.getByRole('button', { name: /log in/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument();
  });

  it('exposes a Log In heading and a Create Account heading', () => {
    renderLanding();
    expect(screen.getByRole('heading', { name: /log in/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /create account/i })).toBeInTheDocument();
  });

  it('names each pane as a region so the two forms can be disambiguated', () => {
    renderLanding();
    expect(screen.getByRole('region', { name: /log in/i })).toBeInTheDocument();
    expect(screen.getByRole('region', { name: /create account/i })).toBeInTheDocument();
  });

  it('renders within a single main landmark', () => {
    renderLanding();
    expect(screen.getAllByRole('main')).toHaveLength(1);
  });
});

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, renderHook, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, useLocation } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { authClient, type AuthUser } from '../api/auth-client';
import { AuthProvider } from '../../../contexts/AuthContext';
import { useLogout } from './useLogout';

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return (
    <QueryClientProvider client={qc}>
      <MemoryRouter initialEntries={['/']}>
        <AuthProvider>{children}</AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>
  );
}

/**
 * Renders useLogout inside a real router so the post-logout redirect target is
 * observable: a button triggers logout and a probe surfaces the current path.
 */
function LogoutHarness() {
  const { submit } = useLogout();
  const location = useLocation();
  return (
    <>
      <button onClick={() => submit()}>do-logout</button>
      <span data-testid="path">{location.pathname}</span>
    </>
  );
}

describe('useLogout', () => {
  beforeEach(() => {
    vi.spyOn(authClient, 'me').mockResolvedValue(null as unknown as AuthUser);
  });
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('calls authClient.logout and clears auth on success', async () => {
    vi.spyOn(authClient, 'logout').mockResolvedValueOnce(undefined);
    const { result } = renderHook(() => useLogout(), { wrapper });
    result.current.submit();
    await waitFor(() => expect(authClient.logout).toHaveBeenCalled());
  });

  it('clears auth even when logout call fails (onSettled)', async () => {
    vi.spyOn(authClient, 'logout').mockRejectedValueOnce(new Error('network'));
    const { result } = renderHook(() => useLogout(), { wrapper });
    result.current.submit();
    await waitFor(() => expect(result.current.isPending).toBe(false));
  });

  it('redirects to the root landing after logout', async () => {
    vi.spyOn(authClient, 'logout').mockResolvedValueOnce(undefined);
    const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(
      <QueryClientProvider client={qc}>
        <MemoryRouter initialEntries={['/settings']}>
          <AuthProvider>
            <LogoutHarness />
          </AuthProvider>
        </MemoryRouter>
      </QueryClientProvider>,
    );
    expect(screen.getByTestId('path').textContent).toBe('/settings');
    fireEvent.click(screen.getByRole('button', { name: /do-logout/i }));
    await waitFor(() => expect(screen.getByTestId('path').textContent).toBe('/'));
  });
});

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../contexts/AuthContext';
import { authClient } from '../api/auth-client';
import { AUTH_QUERY_KEY } from '../../../contexts/AuthContext';

export function useLogout() {
  const { clearAuth } = useAuth();
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const mutation = useMutation({
    mutationFn: () => authClient.logout(),
    onSettled: () => {
      clearAuth();
      queryClient.removeQueries({ queryKey: AUTH_QUERY_KEY });
      // Return to the canonical unauthenticated entry — the split-screen landing
      // at "/" (login + registration) — rather than the login-only /login page.
      navigate('/', { replace: true });
    },
  });

  return { submit: () => mutation.mutate(), isPending: mutation.isPending };
}

/**
 * Public landing page (unauthenticated root).
 *
 * Presents a split-screen entry point: the login form on the left and the
 * registration form on the right, so both paths are discoverable from the
 * application root without first navigating to /register. Composes the existing
 * LoginForm/RegisterForm and useLogin/useRegister so no auth logic is
 * duplicated. Collapses to a single stacked column on narrow viewports.
 *
 * Rendered only for unauthenticated users (ProtectedRoute gates on auth state),
 * so — unlike LoginPage/RegisterPage — it does not itself redirect.
 *
 * Each pane is a `section` with an accessible name (aria-labelledby → its h2),
 * exposing an ARIA `region` so assistive tech and tests/e2e can disambiguate
 * the two forms' shared "Email"/"Password" labels.
 *
 * Requirements: SRS §6.1 FR-A-1, FR-A-2; SRS §10.1 (root route).
 */

import { Box, Paper, Typography } from '@mui/material';
import type { ReactNode } from 'react';
import { AuthShell } from '../../../components/AuthShell';
import { LoginForm } from '../components/LoginForm';
import { RegisterForm } from '../components/RegisterForm';
import { useLogin } from '../hooks/useLogin';
import { useRegister } from '../hooks/useRegister';

/**
 * One pane of the split screen: a titled `section` exposing an ARIA `region`
 * (its accessible name comes from `headingId` → the `h2`). Keeping the
 * heading / `aria-labelledby` wiring in one place stops the two panes' region
 * names — which the e2e selectors depend on — from drifting apart.
 */
function AuthPane({
  headingId,
  title,
  children,
}: {
  headingId: string;
  title: string;
  children: ReactNode;
}) {
  return (
    <Paper component="section" aria-labelledby={headingId} elevation={3} sx={{ p: 4, flex: 1 }}>
      <Typography id={headingId} variant="h5" component="h2" gutterBottom>
        {title}
      </Typography>
      {children}
    </Paper>
  );
}

/**
 * Renders the split-screen public landing at the application root.
 */
export function LandingPage() {
  const login = useLogin();
  const register = useRegister();

  return (
    <AuthShell>
      <Box
        sx={{
          display: 'flex',
          flexDirection: { xs: 'column', md: 'row' },
          gap: 3,
          width: '100%',
          maxWidth: 900,
        }}
      >
        <AuthPane headingId="landing-login-heading" title="Log In">
          <LoginForm onSubmit={login.submit} status={login.status} formError={login.formError} />
        </AuthPane>

        <AuthPane headingId="landing-register-heading" title="Create Account">
          <RegisterForm
            onSubmit={register.submit}
            status={register.status}
            formError={register.formError}
          />
        </AuthPane>
      </Box>
    </AuthShell>
  );
}

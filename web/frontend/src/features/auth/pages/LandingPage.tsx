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
import { LoginForm } from '../components/LoginForm';
import { RegisterForm } from '../components/RegisterForm';
import { useLogin } from '../hooks/useLogin';
import { useRegister } from '../hooks/useRegister';

/**
 * Renders the split-screen public landing at the application root.
 */
export function LandingPage() {
  const login = useLogin();
  const register = useRegister();

  return (
    <Box
      component="main"
      sx={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: 'background.default',
        px: 2,
        py: 4,
      }}
    >
      <Typography
        variant="h4"
        component="h1"
        sx={{ mb: 3, color: 'primary.main', fontWeight: 700 }}
      >
        Weigh to Go!
      </Typography>

      <Box
        sx={{
          display: 'flex',
          flexDirection: { xs: 'column', md: 'row' },
          gap: 3,
          width: '100%',
          maxWidth: 900,
        }}
      >
        <Paper
          component="section"
          aria-labelledby="landing-login-heading"
          elevation={3}
          sx={{ p: 4, flex: 1 }}
        >
          <Typography id="landing-login-heading" variant="h5" component="h2" gutterBottom>
            Log In
          </Typography>
          <LoginForm onSubmit={login.submit} status={login.status} formError={login.formError} />
        </Paper>

        <Paper
          component="section"
          aria-labelledby="landing-register-heading"
          elevation={3}
          sx={{ p: 4, flex: 1 }}
        >
          <Typography id="landing-register-heading" variant="h5" component="h2" gutterBottom>
            Create Account
          </Typography>
          <RegisterForm
            onSubmit={register.submit}
            status={register.status}
            formError={register.formError}
          />
        </Paper>
      </Box>
    </Box>
  );
}

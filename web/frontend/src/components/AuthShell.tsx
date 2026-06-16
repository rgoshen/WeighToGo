/**
 * Shared shell for unauthenticated screens.
 *
 * Provides the full-height, centered `<main>` landmark and the application
 * brand heading above its children, so the app name, brand color, and the
 * single main landmark live in one place for both the centered-card
 * `AuthLayout` (login / register) and the split-screen `LandingPage`. A rebrand
 * touches this file alone rather than drifting `/login` from `/`.
 */

import { Box, Typography } from '@mui/material';
import type { ReactNode } from 'react';

interface AuthShellProps {
  children: ReactNode;
}

/**
 * Renders the centered full-height main region with brand heading.
 */
export function AuthShell({ children }: AuthShellProps) {
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
      }}
    >
      <Typography
        variant="h4"
        component="h1"
        sx={{ mb: 3, color: 'primary.main', fontWeight: 700 }}
      >
        Weigh to Go!
      </Typography>
      {children}
    </Box>
  );
}

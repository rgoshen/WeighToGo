/**
 * Layout wrapper for unauthenticated screens (login, register).
 *
 * Centres a card on the viewport with the application name as branding above
 * the card. There is no navigation — auth screens are intentionally isolated.
 *
 * SRS §10.2 specifies the AuthLayout behaviour.
 */

import { Container, Paper } from '@mui/material';
import type { ReactNode } from 'react';
import { AuthShell } from './AuthShell';

interface AuthLayoutProps {
  children: ReactNode;
}

/**
 * Renders the centered-card auth shell around the provided children. The
 * full-height main region and brand heading come from the shared AuthShell.
 */
export function AuthLayout({ children }: AuthLayoutProps) {
  return (
    <AuthShell>
      {/* Centred card containing the auth form */}
      <Container maxWidth="xs">
        <Paper elevation={3} sx={{ p: 4 }}>
          {children}
        </Paper>
      </Container>
    </AuthShell>
  );
}

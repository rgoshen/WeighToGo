import { Box, CircularProgress, Typography } from '@mui/material';

interface LoadingSplashProps {
  /**
   * Minimum height of the loading region. Defaults to the full viewport
   * (`100vh`) for top-level / route-initial loads; pass a smaller value (e.g.
   * `40vh`) when the splash fills a content area inside a persistent shell.
   */
  minHeight?: string;
}

export function LoadingSplash({ minHeight = '100vh' }: LoadingSplashProps) {
  return (
    <Box
      role="status"
      aria-live="polite"
      style={{ minHeight }}
      sx={{
        display: 'flex',
        flexDirection: 'column',
        gap: 2,
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <CircularProgress aria-hidden="true" />
      <Typography variant="body1">Loading…</Typography>
    </Box>
  );
}

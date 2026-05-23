import { Box, CircularProgress, Typography } from '@mui/material';

export function LoadingSplash() {
  return (
    <Box
      role="status"
      aria-live="polite"
      sx={{
        display: 'flex',
        flexDirection: 'column',
        gap: 2,
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
      }}
    >
      <CircularProgress aria-hidden="true" />
      <Typography variant="body1">Loading…</Typography>
    </Box>
  );
}

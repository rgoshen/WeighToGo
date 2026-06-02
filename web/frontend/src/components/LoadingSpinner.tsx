/**
 * Reusable loading indicator.
 *
 * Centres a MUI CircularProgress with a default accessible label. Pages that
 * need a context-specific announcement can supply the optional `label` prop.
 */

import { Box, CircularProgress } from '@mui/material';

interface LoadingSpinnerProps {
  /** Accessible label announced by screen readers. Defaults to "Loading". */
  label?: string;
  /** MUI CircularProgress size in px. Defaults to the MUI default (40). */
  size?: number;
  /** Vertical padding (MUI spacing units). Defaults to 8. */
  py?: number;
}

/**
 * Vertically and horizontally centred circular progress indicator.
 */
export function LoadingSpinner({ label = 'Loading', size, py = 8 }: LoadingSpinnerProps) {
  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        py,
      }}
    >
      <CircularProgress aria-label={label} {...(size !== undefined ? { size } : {})} />
    </Box>
  );
}

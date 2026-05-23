/**
 * Weight history page (FR-W-2, FR-W-4).
 *
 * Displays the user's full weight log with delete functionality.
 * Uses TanStack Query for server state, ConfirmDeleteDialog for
 * safe deletes, and WeightEntryTable for the data display.
 */

import { Button, Box, Typography } from '@mui/material';
import { useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import { EmptyState } from '../../../components/EmptyState';
import { LoadingSpinner } from '../../../components/LoadingSpinner';
import { ConfirmDeleteDialog } from '../components/ConfirmDeleteDialog';
import { WeightEntryTable } from '../components/WeightEntryTable';
import { useDeleteWeightEntry } from '../hooks/useDeleteWeightEntry';
import { useWeightEntries } from '../hooks/useWeightEntries';

/**
 * Weight log history page rendered at /weight.
 */
export function WeightHistoryPage() {
  const { data, isLoading } = useWeightEntries();
  const deleteMutation = useDeleteWeightEntry();
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const handleDeleteRequest = (entryId: number) => setDeleteId(entryId);
  const handleDeleteConfirm = () => {
    if (deleteId !== null) {
      deleteMutation.mutate(deleteId, { onSettled: () => setDeleteId(null) });
    }
  };
  const handleDeleteCancel = () => setDeleteId(null);

  if (isLoading) {
    return <LoadingSpinner />;
  }

  const entries = data?.items ?? [];

  return (
    <Box component="main">
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Weight Log
        </Typography>
        <Button component={RouterLink} to="/weight/new" variant="contained">
          Log entry
        </Button>
      </Box>

      {entries.length === 0 ? (
        <EmptyState
          title="No weight entries yet"
          description="Start tracking your progress by logging your first weight entry."
          action={
            <Button component={RouterLink} to="/weight/new" variant="contained">
              Log your first entry
            </Button>
          }
        />
      ) : (
        <WeightEntryTable entries={entries} onDelete={handleDeleteRequest} />
      )}

      <ConfirmDeleteDialog
        open={deleteId !== null}
        onConfirm={handleDeleteConfirm}
        onCancel={handleDeleteCancel}
      />
    </Box>
  );
}

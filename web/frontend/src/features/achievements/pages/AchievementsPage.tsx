/**
 * Achievements history page (FR-Ach-4).
 * Replaces AchievementsPlaceholderPage from Milestone 2.
 */

import {
  Box,
  CircularProgress,
  Divider,
  List,
  ListItem,
  ListItemText,
  Paper,
  Typography,
} from '@mui/material';

import { usePreferences } from '../../../contexts/PreferencesContext';
import { formatWeightInPreferredUnit } from '../../../lib/format';
import type { WeightUnit } from '../../../lib/unit-conversion';
import { useAchievements } from '../hooks/useAchievements';
import {
  MILESTONE_THRESHOLD_UNIT,
  parseThreshold,
  type AchievementRecord,
} from '../schemas/achievement';

function achievementLabel(ach: AchievementRecord, preferredUnit: WeightUnit): string {
  if (ach.achievement_type === 'goal_reached') return 'Goal Reached';
  // parseThreshold strips trailing decimal zeros from the Pydantic-serialised
  // Numeric(6,2) string (e.g. "5.00" → 5) and guards against a null/NaN value.
  const value = parseThreshold(ach.threshold);
  if (ach.achievement_type === 'streak') {
    return value === null ? 'Streak' : `${value}-day Streak`;
  }
  // Milestone thresholds are stored in pounds (MILESTONE_THRESHOLD_UNIT);
  // convert to preferred unit for display (FR-P-1).
  return value === null
    ? 'Milestone'
    : `${formatWeightInPreferredUnit(value, MILESTONE_THRESHOLD_UNIT, preferredUnit)} Milestone`;
}

function achievementDate(ach: AchievementRecord): string {
  return new Date(ach.earned_at).toLocaleDateString();
}

export function AchievementsPage() {
  const { data, isLoading } = useAchievements();
  const preferredUnit = usePreferences().preferences.weightUnit;

  return (
    <Box component="main" sx={{ maxWidth: 600, mx: 'auto', py: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Achievements
      </Typography>

      {isLoading && <CircularProgress aria-label="Loading achievements" />}

      {!isLoading && data?.items.length === 0 && (
        <Paper elevation={0} sx={{ p: 4, textAlign: 'center' }}>
          <Typography color="text.secondary">
            No achievements yet. Log weight entries to start earning milestones!
          </Typography>
        </Paper>
      )}

      {!isLoading && data && data.items.length > 0 && (
        <Paper elevation={1}>
          <List disablePadding>
            {data.items.map((ach, idx) => (
              <Box key={ach.achievement_id}>
                {idx > 0 && <Divider />}
                <ListItem>
                  <ListItemText
                    primary={achievementLabel(ach, preferredUnit)}
                    secondary={achievementDate(ach)}
                  />
                </ListItem>
              </Box>
            ))}
          </List>
        </Paper>
      )}
    </Box>
  );
}

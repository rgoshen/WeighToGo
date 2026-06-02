import { z } from 'zod';
import { formatWeightInPreferredUnit } from '../../../lib/format';
import type { WeightUnit } from '../../../lib/unit-conversion';

/**
 * Milestone thresholds are detected and stored in pounds — the weight-entry
 * create handler normalizes to lbs before detection (FR-Ach-2 basis).
 * Convert from this unit for display only.
 */
export const MILESTONE_THRESHOLD_UNIT = 'lbs' satisfies WeightUnit;

/**
 * Format a milestone threshold (canonical pounds) as a display weight in the
 * user's preferred unit. Returns `null` for a null threshold so each call site
 * can own its own fallback copy ("Milestone" vs "Milestone reached!").
 */
export function formatMilestoneWeight(
  threshold: number | null,
  preferredUnit: WeightUnit,
): string | null {
  if (threshold === null) return null;
  return formatWeightInPreferredUnit(threshold, MILESTONE_THRESHOLD_UNIT, preferredUnit);
}

export const achievementSchema = z.object({
  achievement_id: z.number(),
  goal_id: z.number(),
  achievement_type: z.enum(['goal_reached', 'milestone', 'streak']),
  threshold: z.union([z.string(), z.number()]).nullable(),
  earned_at: z.string(),
});

export const achievementListSchema = z.object({
  items: z.array(achievementSchema),
});

export type AchievementRecord = z.infer<typeof achievementSchema>;
export type AchievementListResponse = z.infer<typeof achievementListSchema>;

/**
 * Parse an achievement threshold to a number for display, or `null` when it is
 * absent or malformed. The schema allows a null threshold (only `goal_reached`
 * legitimately uses it); guarding here keeps a bad milestone/streak row from
 * rendering "NaN" to the user.
 */
export function parseThreshold(threshold: string | number | null): number | null {
  if (threshold === null) return null;
  const value = typeof threshold === 'number' ? threshold : parseFloat(threshold);
  return Number.isNaN(value) ? null : value;
}

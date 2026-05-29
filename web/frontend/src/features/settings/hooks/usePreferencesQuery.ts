/**
 * TanStack Query hook for fetching user preferences (ADR-0014: Query = server state).
 */

import { useQuery } from '@tanstack/react-query';

import { preferencesClient } from '../api/preferences-client';
import { apiToPreferences, type Preferences } from '../schemas/preferences-schemas';

export const PREFERENCES_KEY = ['preferences'] as const;

/** Fetch and cache the authenticated user's preferences. Returns mapped camelCase data. */
export function usePreferencesQuery() {
  return useQuery<Preferences>({
    queryKey: PREFERENCES_KEY,
    queryFn: async () => {
      const raw = await preferencesClient.fetch();
      return apiToPreferences(raw);
    },
  });
}

/**
 * TanStack Query mutation hook for updating a single preference.
 *
 * Optimistic update: immediately reflects the new value in the Query cache.
 * On error: rolls back to the snapshot taken before the mutation started.
 */

import { useMutation, useQueryClient } from '@tanstack/react-query';

import { preferencesClient } from '../api/preferences-client';
import { apiToPreferences, type Preferences } from '../schemas/preferences-schemas';
import { PREFERENCES_KEY } from './usePreferencesQuery';

interface UpdatePreferenceVariables {
  key: keyof Preferences extends infer K
    ? K extends string
      ? 'weight_unit' | 'notify_achievement' | 'notify_milestone' | 'notify_streak'
      : never
    : never;
  value: boolean | string;
}

/** Mutate one preference. Optimistic update with rollback on error. */
export function useUpdatePreference() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ key, value }: UpdatePreferenceVariables) => preferencesClient.update(key, value),

    onMutate: async ({ key, value }: UpdatePreferenceVariables) => {
      await queryClient.cancelQueries({ queryKey: PREFERENCES_KEY });
      const snapshot = queryClient.getQueryData<Preferences>(PREFERENCES_KEY);

      // Build the optimistic camelCase key from the snake_case API key.
      const camelKey = key.replace(/_([a-z])/g, (_, c: string) =>
        c.toUpperCase(),
      ) as keyof Preferences;
      queryClient.setQueryData<Preferences>(PREFERENCES_KEY, (prev) =>
        prev ? { ...prev, [camelKey]: value } : prev,
      );

      return { snapshot };
    },

    onError: (_err, _vars, context) => {
      if (context?.snapshot) {
        queryClient.setQueryData(PREFERENCES_KEY, context.snapshot);
      }
    },

    onSuccess: (raw) => {
      queryClient.setQueryData(PREFERENCES_KEY, apiToPreferences(raw));
    },
  });
}

import { z } from 'zod';

export const goalFormSchema = z
  .object({
    goal_type: z.enum(['lose', 'gain'] as const, { message: 'Select lose or gain.' }),
    target_value: z.coerce
      .number()
      .positive('Target weight must be greater than 0.')
      .max(1500, 'Target weight must be 1500 or less.'),
    target_unit: z.enum(['lbs', 'kg'] as const, { message: 'Select lbs or kg.' }),
    start_value: z.coerce
      .number()
      .positive('Starting weight must be greater than 0.')
      .max(1500, 'Starting weight must be 1500 or less.'),
    target_date: z.string().nullable().optional(),
  })
  .superRefine((data, ctx) => {
    if (data.target_value === data.start_value) {
      ctx.addIssue({
        code: 'custom',
        message: 'Target weight must differ from starting weight.',
        path: ['target_value'],
      });
    } else if (data.goal_type === 'lose' && data.target_value >= data.start_value) {
      ctx.addIssue({
        code: 'custom',
        message: 'For a lose goal, target must be less than starting weight.',
        path: ['target_value'],
      });
    } else if (data.goal_type === 'gain' && data.target_value <= data.start_value) {
      ctx.addIssue({
        code: 'custom',
        message: 'For a gain goal, target must be greater than starting weight.',
        path: ['target_value'],
      });
    }
  });

/**
 * Pre-transform field type for React Hook Form's `TFieldValues` generic.
 * Differs from {@link GoalFormValues} because `target_value` and `start_value`
 * use `z.coerce.number()`, so the form's raw input type and validated output
 * type are not identical.
 */
export type GoalFormInput = z.input<typeof goalFormSchema>;
export type GoalFormValues = z.infer<typeof goalFormSchema>;

export const goalUpdateSchema = z.object({
  target_value: z.coerce
    .number()
    .positive('Target weight must be greater than 0.')
    .max(1500, 'Target weight must be 1500 or less.'),
  target_date: z.string().nullable().optional(),
});

export type GoalUpdateValues = z.infer<typeof goalUpdateSchema>;

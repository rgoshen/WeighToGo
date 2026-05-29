import { describe, expect, it } from 'vitest';
import { goalFormSchema } from './goal-schemas';

const BASE = {
  goal_type: 'lose' as const,
  target_value: 150,
  target_unit: 'lbs' as const,
  start_value: 200,
  target_date: null,
};

describe('goalFormSchema', () => {
  it('accepts a valid lose goal', () => {
    expect(goalFormSchema.safeParse(BASE).success).toBe(true);
  });

  it('accepts a valid gain goal', () => {
    const gain = { ...BASE, goal_type: 'gain' as const, target_value: 220 };
    expect(goalFormSchema.safeParse(gain).success).toBe(true);
  });

  it('rejects zero target_value', () => {
    expect(goalFormSchema.safeParse({ ...BASE, target_value: 0 }).success).toBe(false);
  });

  it('rejects target_value over 1500', () => {
    expect(goalFormSchema.safeParse({ ...BASE, target_value: 1501 }).success).toBe(false);
  });

  it('rejects lose goal where target >= start', () => {
    expect(goalFormSchema.safeParse({ ...BASE, target_value: 200 }).success).toBe(false);
    expect(goalFormSchema.safeParse({ ...BASE, target_value: 210 }).success).toBe(false);
  });

  it('rejects gain goal where target <= start', () => {
    const gain = { ...BASE, goal_type: 'gain' as const };
    expect(goalFormSchema.safeParse(gain).success).toBe(false);
  });

  it('rejects target_value equal to start_value', () => {
    expect(goalFormSchema.safeParse({ ...BASE, target_value: 200 }).success).toBe(false);
  });

  it('rejects invalid goal_type', () => {
    expect(goalFormSchema.safeParse({ ...BASE, goal_type: 'maintain' }).success).toBe(false);
  });

  it('rejects invalid target_unit', () => {
    expect(goalFormSchema.safeParse({ ...BASE, target_unit: 'stones' }).success).toBe(false);
  });
});

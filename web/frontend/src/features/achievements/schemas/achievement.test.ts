import { describe, expect, it } from 'vitest';

import { MILESTONE_THRESHOLD_UNIT, parseThreshold } from './achievement';

describe('MILESTONE_THRESHOLD_UNIT', () => {
  it('is the canonical pounds unit used by the backend weight-entry create handler', () => {
    expect(MILESTONE_THRESHOLD_UNIT).toBe('lbs');
  });
});

describe('parseThreshold', () => {
  it('parses a numeric string and strips trailing zeros', () => {
    expect(parseThreshold('5.00')).toBe(5);
  });

  it('returns a numeric threshold unchanged', () => {
    expect(parseThreshold(7)).toBe(7);
  });

  it('returns null for a null threshold', () => {
    expect(parseThreshold(null)).toBeNull();
  });

  it('returns null for a non-numeric string so NaN never reaches the UI', () => {
    expect(parseThreshold('not-a-number')).toBeNull();
  });
});

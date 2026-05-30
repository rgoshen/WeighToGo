import { describe, expect, it } from 'vitest';

import { convertWeight } from './unit-conversion';

describe('convertWeight', () => {
  it('converts 1 lb to kilograms using the NIST factor', () => {
    expect(convertWeight(1, 'lbs', 'kg')).toBeCloseTo(0.45359237, 8);
  });
});

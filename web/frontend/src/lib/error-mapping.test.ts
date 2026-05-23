import { describe, expect, it } from 'vitest';

import { mapApiError, mapValidationErrors } from './error-mapping';

describe('mapApiError', () => {
  it('returns a user-friendly string for a 401 status', () => {
    const message = mapApiError(401);
    expect(typeof message).toBe('string');
    expect(message.length).toBeGreaterThan(0);
  });

  it('returns a user-friendly string for a 409 status', () => {
    const message = mapApiError(409);
    expect(typeof message).toBe('string');
    expect(message.length).toBeGreaterThan(0);
  });

  it('returns a user-friendly string for a 422 status', () => {
    const message = mapApiError(422);
    expect(typeof message).toBe('string');
    expect(message.length).toBeGreaterThan(0);
  });

  it('returns a user-friendly string for a 500 status', () => {
    const message = mapApiError(500);
    expect(typeof message).toBe('string');
    expect(message.length).toBeGreaterThan(0);
  });

  it('returns different messages for different status codes', () => {
    expect(mapApiError(401)).not.toBe(mapApiError(500));
  });

  it('returns a fallback string for an unknown status code', () => {
    const message = mapApiError(999);
    expect(typeof message).toBe('string');
    expect(message.length).toBeGreaterThan(0);
  });
});

describe('mapValidationErrors', () => {
  it('returns empty object for empty input', () => {
    expect(mapValidationErrors([])).toEqual({});
  });

  it('maps a single field error', () => {
    expect(
      mapValidationErrors([{ field: 'email', code: 'value_error', message: 'bad email' }]),
    ).toEqual({ email: 'bad email' });
  });

  it('keeps the first message when a field repeats', () => {
    const errors = [
      { field: 'password', code: 'value_error', message: 'too short' },
      { field: 'password', code: 'value_error', message: 'missing symbol' },
    ];
    expect(mapValidationErrors(errors)).toEqual({ password: 'too short' });
  });

  it('handles nested field paths via dot notation', () => {
    const errors = [{ field: 'profile.displayName', code: 'value_error', message: 'required' }];
    expect(mapValidationErrors(errors)).toEqual({ 'profile.displayName': 'required' });
  });
});

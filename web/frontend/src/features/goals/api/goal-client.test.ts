import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import * as apiClient from '../../../lib/api-client';
import { goalClient } from './goal-client';

describe('goalClient', () => {
  let fetchJsonSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    fetchJsonSpy = vi.spyOn(apiClient, 'fetchJson').mockResolvedValue({} as never);
  });
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('getActive calls GET /api/v1/goals/active', async () => {
    await goalClient.getActive();
    expect(fetchJsonSpy).toHaveBeenCalledWith(
      '/api/v1/goals/active',
      expect.objectContaining({ method: 'GET' }),
    );
  });

  it('list calls GET /api/v1/goals', async () => {
    await goalClient.list();
    expect(fetchJsonSpy).toHaveBeenCalledWith(
      '/api/v1/goals',
      expect.objectContaining({ method: 'GET' }),
    );
  });

  it('create calls POST /api/v1/goals', async () => {
    const body = {
      goal_type: 'lose',
      target_value: 150,
      target_unit: 'lbs',
      start_value: 200,
      target_date: null,
    };
    await goalClient.create(body);
    expect(fetchJsonSpy).toHaveBeenCalledWith(
      '/api/v1/goals',
      expect.objectContaining({ method: 'POST', body }),
    );
  });

  it('update calls PUT /api/v1/goals/:id', async () => {
    const body = { target_value: 145, target_date: null };
    await goalClient.update(1, body);
    expect(fetchJsonSpy).toHaveBeenCalledWith(
      '/api/v1/goals/1',
      expect.objectContaining({ method: 'PUT', body }),
    );
  });

  it('abandon calls DELETE /api/v1/goals/:id', async () => {
    await goalClient.abandon(1);
    expect(fetchJsonSpy).toHaveBeenCalledWith(
      '/api/v1/goals/1',
      expect.objectContaining({ method: 'DELETE' }),
    );
  });
});

import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { notifyManager } from '@tanstack/react-query';
import { afterEach, beforeAll } from 'vitest';

// Make TanStack Query v5 notify observers synchronously so that synchronous
// act(() => {...}) calls flush cache updates immediately in tests.
beforeAll(() => {
  notifyManager.setScheduler((cb) => cb());
});

// Ensure the DOM is cleaned between every test so stale rendered trees do
// not interfere with subsequent test assertions.
afterEach(() => {
  cleanup();
});

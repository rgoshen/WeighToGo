import { Suspense } from 'react';
import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { lazyNamed } from './lazy-named';

describe('lazyNamed', () => {
  it('renders the named export of a dynamically imported module', async () => {
    // ARRANGE: a module exposing a component under a named (non-default) export,
    // mirroring how the app's page modules are structured (no default export).
    const Loaded = lazyNamed(
      () => Promise.resolve({ Widget: () => <div>widget loaded</div> }),
      'Widget',
    );

    // ACT
    render(
      <Suspense fallback={<span>loading…</span>}>
        <Loaded />
      </Suspense>,
    );

    // ASSERT
    expect(await screen.findByText('widget loaded')).toBeInTheDocument();
  });
});

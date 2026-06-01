import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { LoadingSplash } from './LoadingSplash';

describe('LoadingSplash', () => {
  it('renders an accessible loading indicator', () => {
    render(<LoadingSplash />);
    expect(screen.getByRole('status')).toBeInTheDocument();
    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });

  it('applies a custom minHeight so it can fill a content area instead of the viewport', () => {
    render(<LoadingSplash minHeight="40vh" />);
    expect(screen.getByRole('status')).toHaveStyle({ minHeight: '40vh' });
  });
});

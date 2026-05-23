import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { LoadingSplash } from './LoadingSplash';

describe('LoadingSplash', () => {
  it('renders an accessible loading indicator', () => {
    render(<LoadingSplash />);
    expect(screen.getByRole('status')).toBeInTheDocument();
    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });
});

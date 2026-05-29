/**
 * Tests for SettingsPage — renders controls and calls setPreference.
 */
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { SettingsPage } from './SettingsPage';

const mockSetPreference = vi.fn();

vi.mock('../../../contexts/PreferencesContext', () => ({
  usePreferences: () => ({
    preferences: {
      weightUnit: 'lbs',
      notifyAchievement: true,
      notifyMilestone: true,
      notifyStreak: false,
    },
    isLoading: false,
    setPreference: mockSetPreference,
  }),
}));

describe('SettingsPage', () => {
  it('renders the Settings heading', () => {
    render(<SettingsPage />);
    expect(screen.getByRole('heading', { name: /settings/i })).toBeInTheDocument();
  });

  it('renders the unit preference radio group', () => {
    render(<SettingsPage />);
    expect(screen.getByRole('group', { name: /weight unit/i })).toBeInTheDocument();
  });

  it('renders the notification toggles', () => {
    render(<SettingsPage />);
    expect(screen.getByLabelText(/achievement alerts toggle/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/milestone alerts toggle/i)).toBeInTheDocument();
  });

  it('calls setPreference when unit changes', async () => {
    const user = userEvent.setup();
    render(<SettingsPage />);
    const kgRadio = screen.getByRole('radio', { name: 'kg' });
    await user.click(kgRadio);
    expect(mockSetPreference).toHaveBeenCalledWith('weight_unit', 'kg');
  });

  it('calls setPreference when achievement toggle changes', async () => {
    const user = userEvent.setup();
    render(<SettingsPage />);
    const toggle = screen.getByLabelText(/achievement alerts toggle/i);
    await user.click(toggle);
    expect(mockSetPreference).toHaveBeenCalledWith('notify_achievement', false);
  });

  it('shows save feedback after preference change', async () => {
    const user = userEvent.setup();
    render(<SettingsPage />);
    const kgRadio = screen.getByRole('radio', { name: 'kg' });
    await user.click(kgRadio);
    expect(screen.getByText('Preferences saved')).toBeInTheDocument();
  });

  it('resets save feedback timer on rapid consecutive changes', async () => {
    const user = userEvent.setup();
    render(<SettingsPage />);
    const kgRadio = screen.getByRole('radio', { name: 'kg' });
    // Click twice rapidly to trigger the clearTimeout branch in showSaved.
    await user.click(kgRadio);
    await user.click(screen.getByLabelText(/achievement alerts toggle/i));
    expect(screen.getByText('Preferences saved')).toBeInTheDocument();
  });
});

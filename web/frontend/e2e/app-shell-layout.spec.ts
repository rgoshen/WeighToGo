import { expect, test, type Page } from '@playwright/test';

/**
 * Layout-geometry regression guards for the application shell (issue #130).
 *
 * On desktop viewports (md breakpoint, >=900px) the permanent Drawer must reserve
 * horizontal space so the main content region starts at or beyond the drawer's right
 * edge; the defect this guards against painted the fixed drawer over the left 240px of
 * the content, hiding the page heading. On mobile the drawer is a closed overlay, so the
 * content stays full-width. jsdom has no layout engine, so these invariants can only be
 * verified in a real browser.
 */

const DRAWER_WIDTH = 240;

/** Register a unique account; registration lands the user on the dashboard, inside the shell. */
async function registerFreshUser(page: Page, prefix: string): Promise<void> {
  // Date.now() alone collides when the same registration runs twice within a millisecond
  // (e.g. under --repeat-each); add random entropy so the email is always unique.
  const email = `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}@example.com`;
  const password = 'Aa1!aaaaaaaa';
  await page.goto('/register');
  await page.getByLabel(/display name/i).fill('Shell Layout User');
  await page.getByLabel(/email/i).fill(email);
  await page.getByLabel(/^password$/i).fill(password);
  await page.getByLabel(/confirm password/i).fill(password);
  await page.getByRole('button', { name: /create account/i }).click();
  await expect(page).toHaveURL('/', { timeout: 10_000 });
}

test.describe('application shell layout (desktop)', () => {
  // 1366x768 is the desktop size the M4 quality review measured the defect at.
  test.use({ viewport: { width: 1366, height: 768 } });

  test('main content region clears the permanent sidebar', async ({ page }) => {
    await registerFreshUser(page, 'shell-desktop');

    // Wait for the lazy dashboard chunk to mount so its nested <main> is present and the
    // layout has settled, then measure — proving the locator is unambiguous, not race-bound.
    await expect(page.getByRole('heading', { level: 1, name: 'Dashboard' })).toBeVisible();

    // Every authenticated page renders its own <main> nested inside the shell's <main>, so
    // getByRole('main') is ambiguous under Playwright strict mode; .first() targets the
    // shell's outer main — the flex item that must clear the drawer. The permanent drawer is
    // DRAWER_WIDTH wide, so the shell main's left edge must sit at or beyond it.
    const main = page.getByRole('main').first();
    await expect(main).toBeVisible();
    const box = await main.boundingBox();
    expect(box).not.toBeNull();
    expect(box!.x).toBeGreaterThanOrEqual(DRAWER_WIDTH);
  });
});

test.describe('application shell layout (mobile)', () => {
  // 390x844 is the mobile size the review confirmed correct (temporary overlay drawer).
  test.use({ viewport: { width: 390, height: 844 } });

  test('main content spans the full width with the temporary drawer closed', async ({ page }) => {
    await registerFreshUser(page, 'shell-mobile');

    // Wait for the lazy dashboard chunk to mount so its nested <main> is present, then measure.
    await expect(page.getByRole('heading', { level: 1, name: 'Dashboard' })).toBeVisible();

    // The temporary drawer is a closed overlay and reserves no space, so the shell's main
    // region stays flush to the left edge and spans the full viewport. .first() targets the
    // shell's outer main (each page nests its own <main> inside it). Compare against the
    // actual viewport (less a scrollbar allowance) rather than a hard-coded width.
    const main = page.getByRole('main').first();
    await expect(main).toBeVisible();
    const box = await main.boundingBox();
    expect(box).not.toBeNull();
    expect(box!.x).toBeLessThan(1);
    expect(box!.width).toBeGreaterThanOrEqual(page.viewportSize()!.width - 16);
  });
});

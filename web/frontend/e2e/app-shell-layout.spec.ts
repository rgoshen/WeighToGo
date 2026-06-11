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
  const email = `${prefix}-${Date.now()}@example.com`;
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

    // The permanent drawer is DRAWER_WIDTH wide; the main region's left edge must sit at
    // or beyond it, never under it.
    const main = page.getByRole('main');
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

    // The temporary drawer is a closed overlay and reserves no space, so the main region
    // stays flush to the left edge and spans (nearly) the full viewport.
    const main = page.getByRole('main');
    await expect(main).toBeVisible();
    const box = await main.boundingBox();
    expect(box).not.toBeNull();
    expect(box!.x).toBeLessThan(1);
    expect(box!.width).toBeGreaterThan(360);
  });
});

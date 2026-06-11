import { expect, test } from '@playwright/test';

/**
 * Layout-geometry regression guard for the application shell (issue #130).
 *
 * On desktop viewports (md breakpoint, >=900px) the permanent Drawer must
 * reserve horizontal space so the main content region starts at or beyond the
 * drawer's right edge. The defect this guards against painted the fixed drawer
 * over the left 240px of the content, hiding the page heading. jsdom has no
 * layout engine, so this invariant can only be verified in a real browser.
 */

const DRAWER_WIDTH = 240;

// 1366x768 is the desktop size the M4 quality review measured the defect at.
test.use({ viewport: { width: 1366, height: 768 } });

test.describe('application shell layout (desktop)', () => {
  test('main content region clears the permanent sidebar', async ({ page }) => {
    const unique = Date.now();
    const email = `shell-layout-${unique}@example.com`;
    const password = 'Aa1!aaaaaaaa';

    // Registering lands the authenticated user on the dashboard, inside the shell.
    await page.goto('/register');
    await page.getByLabel(/display name/i).fill('Shell Layout User');
    await page.getByLabel(/email/i).fill(email);
    await page.getByLabel(/^password$/i).fill(password);
    await page.getByLabel(/confirm password/i).fill(password);
    await page.getByRole('button', { name: /create account/i }).click();
    await expect(page).toHaveURL('/', { timeout: 10_000 });

    // The permanent drawer is DRAWER_WIDTH wide; the main region's left edge must
    // sit at or beyond it, never under it.
    const main = page.getByRole('main');
    await expect(main).toBeVisible();
    const box = await main.boundingBox();
    expect(box).not.toBeNull();
    expect(box!.x).toBeGreaterThanOrEqual(DRAWER_WIDTH);
  });
});

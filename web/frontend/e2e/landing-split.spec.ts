import { expect, test } from '@playwright/test';

// The root landing shows two forms whose fields share "Email"/"Password"
// labels, so every field locator is scoped to its named region ("Log In" /
// "Create Account") to stay unambiguous under Playwright strict mode.
test.describe.serial('root landing auth', () => {
  const unique = Date.now();
  const email = `landing-${unique}@example.com`;
  const password = 'Aa1!aaaaaaaa';

  test('logged-out root shows split-screen login and registration', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveURL('/');
    await expect(
      page.getByRole('region', { name: /log in/i }).getByRole('button', { name: /log in/i }),
    ).toBeVisible();
    await expect(
      page
        .getByRole('region', { name: /create account/i })
        .getByRole('button', { name: /create account/i }),
    ).toBeVisible();
  });

  test('new user can register from the root landing', async ({ page }) => {
    await page.goto('/');
    const pane = page.getByRole('region', { name: /create account/i });
    await pane.getByLabel(/display name/i).fill('Landing User');
    await pane.getByLabel(/email/i).fill(email);
    await pane.getByLabel(/^password$/i).fill(password);
    await pane.getByLabel(/confirm password/i).fill(password);
    await pane.getByRole('button', { name: /create account/i }).click();
    await expect(page).toHaveURL('/', { timeout: 10_000 });
    await expect(page.getByRole('button', { name: /account menu/i })).toBeVisible();
    // Log out → returns to the root landing.
    await page.getByRole('button', { name: /account menu/i }).click();
    await page.getByRole('menuitem', { name: /log out/i }).click();
    await expect(page).toHaveURL('/');
  });

  test('returning user can log in from the root landing', async ({ page }) => {
    await page.goto('/');
    const pane = page.getByRole('region', { name: /log in/i });
    await pane.getByLabel(/email/i).fill(email);
    await pane.getByLabel(/^password$/i).fill(password);
    await pane.getByRole('button', { name: /log in/i }).click();
    await expect(page).toHaveURL('/', { timeout: 10_000 });
    await expect(page.getByRole('button', { name: /account menu/i })).toBeVisible();
  });
});

import AxeBuilder from '@axe-core/playwright';
import { expect, test } from '@playwright/test';

// Logged-out `/` resolves to the public split-screen LandingPage — the primary
// unauthenticated entry, whose named regions and single <main> DDR-0010 frames
// as an accessibility contract — so it is axe-scanned alongside /login and /register.
for (const path of ['/login', '/register', '/']) {
  test(`a11y: ${path} has zero detectable axe violations`, async ({ page }) => {
    await page.goto(path);
    const results = await new AxeBuilder({ page })
      .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa'])
      .analyze();
    expect(results.violations).toEqual([]);
  });
}

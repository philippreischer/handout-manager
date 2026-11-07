import { test, expect } from '@playwright/test'

test('redirects a visitor without a session to the login form', async ({ page }) => {
  // Stubbed so the test needs no backend: a rejected refresh is exactly what
  // the app sees on a first visit without a refresh cookie.
  await page.route('**/api/auth/refresh', (route) => route.fulfill({ status: 401 }))

  await page.goto('/home')

  await expect(page).toHaveURL(/\/login$/)
  await expect(page.getByRole('heading', { name: 'Sign in' })).toBeVisible()
})

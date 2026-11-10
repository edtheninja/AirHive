import { test, expect, type Page, type Browser } from "@playwright/test";

const ADMIN_USERNAME = "admin";
const ADMIN_PASSWORD = "AirHiveAdmin123!";

async function createPassenger(browser: Browser, username: string) {
  const context = await browser.newContext({
    baseURL: "http://localhost:3000",
  });

  const page = await context.newPage();

  await page.goto("/signup");

  await expect(page.locator("#confirm-password")).toBeVisible();

  await page.locator("#username").fill(username);
  await page.locator("#password").fill("PassengerTest123!");
  await page.locator("#confirm-password").fill("PassengerTest123!");

  await page.getByRole("button", { name: "Create account" }).click();

  await expect(page).toHaveURL(/\/$/);

  const session = await page.evaluate(() => {
    const value = localStorage.getItem("airhive_auth");
    return value ? JSON.parse(value) : null;
  });

  expect(session?.role).toBe("PASSENGER");

  await context.close();

  return session;
}

async function loginAsAdmin(page: Page) {
  await page.goto("/login");

  await page.getByLabel("Username").fill(ADMIN_USERNAME);
  await page.getByLabel("Password").fill(ADMIN_PASSWORD);
  await page.getByRole("button", { name: "Sign in" }).click();

  await expect(page).toHaveURL(/\/$/);
}

test.describe("Admin user management", () => {
  test("admin can view users, update a role, and retain the change after reload", async ({
    page,
    browser,
  }) => {
    const suffix = `${Date.now()}`;
    const targetUsername = `admin_role_e2e_${suffix}`;
    const passengerUsername = `passenger_access_e2e_${suffix}`;

    // Create two independent passenger accounts.
    await createPassenger(browser, targetUsername);
    const passengerSession = await createPassenger(browser, passengerUsername);

    // Log in using the main page as the seeded admin.
    await loginAsAdmin(page);

    await page.goto("/users");

    await expect(page.getByRole("heading", { name: /users/i })).toBeVisible();

    // Verify the newly registered user appears.
    await expect(page.getByText(targetUsername)).toBeVisible();

    // Change the target user's role.
    const roleSelect = page.getByRole("combobox", {
      name: `Role for ${targetUsername}`,
    });

    await roleSelect.click();
    await page.getByRole("option", { name: "OPERATOR" }).click();

    await page
      .getByRole("button", {
        name: `Save role for ${targetUsername}`,
      })
      .click();

    // Verify the role change.
    const userRow = page.locator("tr").filter({
      hasText: targetUsername,
    });

    await expect(userRow).toContainText("OPERATOR");

    // Verify persistence after reload.
    await page.reload();

    const reloadedRow = page.locator("tr").filter({
      hasText: targetUsername,
    });

    await expect(reloadedRow).toContainText("OPERATOR");

    // Restore a separate PASSENGER session.
    await page.evaluate((session) => {
      localStorage.setItem("airhive_auth", JSON.stringify(session));
    }, passengerSession);

    // Passenger must not access the admin page.
    await page.goto("/users");
    await expect(page).toHaveURL(/\/$/);

    // Passenger must also be denied by the backend API.
    const apiStatus = await page.evaluate(async () => {
      const session = JSON.parse(localStorage.getItem("airhive_auth") ?? "{}");

      const response = await fetch("http://localhost:8080/api/users", {
        headers: {
          Authorization: `Bearer ${session.token}`,
        },
      });

      return response.status;
    });

    expect(apiStatus).toBe(403);
  });
});

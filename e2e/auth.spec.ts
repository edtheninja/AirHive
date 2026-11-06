import { test, expect } from "@playwright/test";

const ADMIN_USERNAME = "admin";
const ADMIN_PASSWORD = "AirHiveAdmin123!";

async function loginAsAdmin(page: import("@playwright/test").Page) {
  await page.goto("/login");

  await page.getByLabel("Username").fill(ADMIN_USERNAME);
  await page.getByLabel("Password").fill(ADMIN_PASSWORD);
  await page.getByRole("button", { name: "Sign in" }).click();

  await expect(page).toHaveURL(/\/$/);
}

test.describe("AirHive authentication", () => {
  test("redirects unauthenticated users from protected routes", async ({ page }) => {
    page.on("console", (message) => {
      console.log("[BROWSER]", message.text());
    });

    page.on("pageerror", (error) => {
      console.log("[PAGE ERROR]", error.message);
    });

    await page.goto("/passengers");

    console.log(
      "[LOCAL STORAGE SESSION]",
      await page.evaluate(() => localStorage.getItem("airhive_auth")),
    );

    await expect(page).toHaveURL(/\/login$/);
    await expect(page.getByText("Sign in to AirHive")).toBeVisible();
  });

  test("rejects invalid credentials", async ({ page }) => {
    await page.goto("/login");

    await page.getByLabel("Username").fill("invalid-user");
    await page.getByLabel("Password").fill("invalid-password");
    await page.getByRole("button", { name: "Sign in" }).click();

    await expect(page.getByRole("alert")).toBeVisible();
    await expect(page).toHaveURL(/\/login$/);
  });

  test("allows the seeded admin to sign in", async ({ page }) => {
    page.on("request", (request) => {
      if (request.method() === "POST") {
        console.log("[LOGIN REQUEST]", {
          method: request.method(),
          url: request.url(),
        });
      }
    });

    page.on("response", async (response) => {
      if (response.request().method() !== "POST") return;

      const contentType = response.headers()["content-type"] ?? "";

      console.log("[POST RESPONSE]", {
        status: response.status(),
        url: response.url(),
        contentType,
      });

      if (contentType.includes("application/json")) {
        try {
          const body = await response.json();

          console.log("[POST RESPONSE BODY]", {
            message: body.message,
            error: body.error,
            keys: Object.keys(body),
          });
        } catch {
          console.log("[POST RESPONSE BODY] Could not parse JSON");
        }
      }
    });

    page.on("requestfailed", (request) => {
      if (request.method() === "POST") {
        console.log("[POST REQUEST FAILED]", {
          url: request.url(),
          error: request.failure()?.errorText,
        });
      }
    });

    await loginAsAdmin(page);

    await expect(page.getByRole("heading", { name: "Operations Dashboard" })).toBeVisible();

    await expect
      .poll(() => page.evaluate(() => localStorage.getItem("airhive_auth")))
      .not.toBeNull();
  });

  test("preserves the admin session after a page reload", async ({ page }) => {
    await loginAsAdmin(page);

    await page.reload();

    await expect(page.getByRole("heading", { name: "Operations Dashboard" })).toBeVisible();

    await expect(page).not.toHaveURL(/\/login$/);
  });
});

test.describe("Passenger manifest", () => {
  test("admin can access the passenger manifest", async ({ page }) => {
    await loginAsAdmin(page);

    await page.goto("/passengers");

    await expect(page.getByRole("heading", { name: "Passengers" })).toBeVisible();

    await expect(page.getByText("PX-9000")).toBeVisible();
    await expect(page.getByText("Aarav Sharma")).toBeVisible();
  });
});

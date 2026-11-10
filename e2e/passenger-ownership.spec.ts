import { test, expect, type Page } from "@playwright/test";

const PASSENGER_A = {
  username: "passenger_e2e_01",
  password: "PassengerTest123!",
};

const PASSENGER_B = {
  username: "passenger_e2e_02",
  password: "PassengerTest456!",
};

async function login(page: Page, username: string, password: string) {
  await page.goto("/login");

  await page.getByLabel("Username").fill(username);
  await page.getByLabel("Password").fill(password);
  await page.getByRole("button", { name: "Sign in" }).click();

  await expect(page).toHaveURL(/\/$/);
  await expect(
    page.getByRole("heading", {
      name: new RegExp(`Welcome,?\\s*${username}`),
    }),
  ).toBeVisible();
}

async function createBooking(page: Page, passengerName: string): Promise<string> {
  await expect(page.getByText("Book a flight", { exact: true })).toBeVisible();

  const flightButtons = page.locator("button[aria-pressed]");
  await expect(flightButtons.first()).toBeVisible({ timeout: 15000 });

  await flightButtons.first().click();
  await expect(flightButtons.first()).toHaveAttribute("aria-pressed", "true");

  await page.getByLabel("Passenger name").fill(passengerName);

  const responsePromise = page.waitForResponse(
    (response) =>
      response.url().includes("/api/bookings") && response.request().method() === "POST",
  );

  await page.getByRole("button", { name: "Confirm Economy booking" }).click();

  const response = await responsePromise;
  expect(response.status()).toBe(201);

  const booking = await response.json();
  expect(booking.pnr).toBeTruthy();

  await expect(page.getByText("Booking confirmed!", { exact: true })).toBeVisible();

  return booking.pnr as string;
}

async function getBookingHistory(page: Page) {
  const responsePromise = page.waitForResponse(
    (response) => response.url().includes("/api/bookings") && response.request().method() === "GET",
  );

  await page.goto("/bookings");

  const response = await responsePromise;
  expect(response.ok()).toBeTruthy();

  await expect(page.getByRole("heading", { name: "My bookings" })).toBeVisible();

  return response.json() as Promise<Array<{ pnr: string }>>;
}

test.describe("Passenger booking ownership isolation", () => {
  test("passengers can only see their own bookings", async ({ browser }) => {
    const contextA = await browser.newContext({
      baseURL: "http://localhost:3000",
    });
    const contextB = await browser.newContext({
      baseURL: "http://localhost:3000",
    });

    try {
      const pageA = await contextA.newPage();
      const pageB = await contextB.newPage();

      // Passenger A signs in and creates a booking.
      await login(pageA, PASSENGER_A.username, PASSENGER_A.password);
      const pnrA = await createBooking(pageA, "Ownership Test Passenger A");

      // Passenger B signs in independently and creates a booking.
      await login(pageB, PASSENGER_B.username, PASSENGER_B.password);
      const pnrB = await createBooking(pageB, "Ownership Test Passenger B");

      expect(pnrA).not.toBe(pnrB);

      // Passenger B must see their own booking, but not Passenger A's.
      const bookingsB = await getBookingHistory(pageB);
      const pnrsB = bookingsB.map((booking) => booking.pnr);

      expect(pnrsB).toContain(pnrB);
      expect(pnrsB).not.toContain(pnrA);

      await expect(pageB.locator("article").filter({ hasText: pnrB })).toBeVisible();

      await expect(pageB.locator("article").filter({ hasText: pnrA })).toHaveCount(0);

      // Passenger A must see their own booking, but not Passenger B's.
      const bookingsA = await getBookingHistory(pageA);
      const pnrsA = bookingsA.map((booking) => booking.pnr);

      expect(pnrsA).toContain(pnrA);
      expect(pnrsA).not.toContain(pnrB);

      await expect(pageA.locator("article").filter({ hasText: pnrA })).toBeVisible();

      await expect(pageA.locator("article").filter({ hasText: pnrB })).toHaveCount(0);

      console.log("[PASSENGER OWNERSHIP ISOLATION]", {
        passengerA: pnrA,
        passengerB: pnrB,
        result: "PASS",
      });
    } finally {
      await contextA.close();
      await contextB.close();
    }
  });
});

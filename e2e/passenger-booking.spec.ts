import { test, expect } from "@playwright/test";

const PASSENGER_USERNAME = "passenger_e2e_01";
const PASSENGER_PASSWORD = "PassengerTest123!";

test.describe("Passenger flight booking", () => {
  test("passenger can book a scheduled flight and receive a PNR", async ({ page }) => {
    await page.goto("/login");

    await page.getByLabel("Username").fill(PASSENGER_USERNAME);
    await page.getByLabel("Password").fill(PASSENGER_PASSWORD);
    await page.getByRole("button", { name: "Sign in" }).click();

    await expect(page).toHaveURL(/\/$/);

    await expect(
      page.getByRole("heading", {
        name: new RegExp(`Welcome,?\\s*${PASSENGER_USERNAME}`),
      }),
    ).toBeVisible();

    const bookingHeading = page.getByText("Book a flight", { exact: true });

    await expect(bookingHeading).toBeVisible();

    const flightButtons = page.locator("button[aria-pressed]");

    await expect(flightButtons.first()).toBeVisible();

    await expect(flightButtons.first()).toBeVisible({
      timeout: 15000,
    });

    // Select the first available scheduled flight.
    await flightButtons.first().click();

    await expect(flightButtons.first()).toHaveAttribute("aria-pressed", "true");

    await page.getByLabel("Passenger name").fill("E2E Passenger Test");

    // Observe the booking API response.
    const bookingResponsePromise = page.waitForResponse(
      (response) =>
        response.url().includes("/api/bookings") && response.request().method() === "POST",
    );

    await page.getByRole("button", { name: "Confirm Economy booking" }).click();

    const bookingResponse = await bookingResponsePromise;

    expect(
      bookingResponse.status(),
      "Booking API should accept the passenger booking",
    ).toBeGreaterThanOrEqual(200);

    expect(bookingResponse.status()).toBeLessThan(300);

    await expect(page.getByText("Booking confirmed!", { exact: true })).toBeVisible();

    const confirmation = page.getByRole("status");

    await expect(confirmation).toContainText("PNR:");

    const pnrText = await confirmation.innerText();
    const pnrMatch = pnrText.match(/PNR:\s*([A-Z0-9]+)/);

    expect(pnrMatch, "Booking confirmation should contain a PNR").not.toBeNull();

    const pnr = pnrMatch![1];

    expect(pnr.length).toBeGreaterThan(0);

    // Verify the booking confirmation contains the generated PNR.
    await expect(confirmation).toContainText(pnr);
    console.log("[PASSENGER BOOKING E2E]", {
      pnr,
      apiStatus: bookingResponse.status(),
    });
  });
});

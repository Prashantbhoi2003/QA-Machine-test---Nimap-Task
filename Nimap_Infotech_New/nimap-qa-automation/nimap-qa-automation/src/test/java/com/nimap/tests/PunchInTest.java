package com.nimap.tests;

import com.nimap.base.BaseTest;
import com.nimap.pages.DashboardPage;
import com.nimap.pages.LoginPage;
import com.nimap.utils.ConfigReader;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Task 2: Verify the Toast/Popup message after PunchIn.
 *
 * DISABLED - see README / submission notes for full investigation:
 * No clickable "Punch In" control exists anywhere in the web UI for this
 * test account. Confirmed by checking:
 *   - Dashboard (only a read-only "0 / 1 Punched In" stat card, no button
 *     or click handler - inspected via DevTools, it's a plain <p>/<h6>
 *     display element)
 *   - Attendance page ("Allow Punch In/Out" settings - Web toggle was
 *     found OFF by default; toggled it ON and re-checked Dashboard and
 *     Attendance again - still no button appeared)
 *   - Network requests filtered by "punch" - 0 matching requests
 * Conclusion: Punch In (Web) is likely a mobile-app-only feature, or
 * requires a different user role/permission than this test account has.
 * This is a genuine finding, not a locator/automation defect.
 */
public class PunchInTest extends BaseTest {

    @Test(enabled = false, description = "Disabled: no Punch In control exists in the web UI for this test account - see class Javadoc / README for investigation notes.")
    public void validateToastMessageAfterPunchIn() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(
                System.getProperty("validUsername", "your_valid_username"),
                System.getProperty("validPassword", "your_valid_password"));

        DashboardPage dashboardPage = new DashboardPage(driver);
        dashboardPage.clickPunchIn();

        String actualToastMessage = dashboardPage.getToastMessage();

        // TODO: replace this with the exact text you observe on the real
        // site once you run this against https://test.fieldforceconnect.com/
        String expectedToastMessage = "Punched In Successfully";

        Assert.assertEquals(actualToastMessage.trim(), expectedToastMessage,
                "Toast message after PunchIn did not match the expected text.");
    }
}
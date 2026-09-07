package com.nimap.tests;

import com.nimap.base.BaseTest;
import com.nimap.pages.LoginPage;
import com.nimap.utils.ExcelUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Task 1: Automate Login Journey using parameterization and validate it.
 *
 * Data is pulled from src/test/resources/testdata/TestData.xlsx -> LoginData
 * sheet, so every row (valid creds, invalid creds, blank username, blank
 * password) runs as its own TestNG test iteration.
 */
public class LoginTest extends BaseTest {

    @DataProvider(name = "loginData")
    public Object[][] loginData() {
        return ExcelUtils.readSheet(
                "src/test/resources/testdata/TestData.xlsx", "LoginData");
    }

    @Test(dataProvider = "loginData")
    public void validateLogin(String username, String password, String expectedResult) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(username, password);

        if (expectedResult.equalsIgnoreCase("Success")) {
            // After a successful login the app should navigate away from
            // the login page onto the dashboard.
            Assert.assertFalse(loginPage.isLoginPageDisplayed(),
                    "Expected login to succeed and redirect to dashboard, but login page is still displayed.");
        } else {
            // For every negative case the login page must still be shown,
            // i.e. the invalid attempt was correctly rejected.
            Assert.assertTrue(loginPage.isLoginPageDisplayed(),
                    "Expected login to fail and stay on login page, but user got past it.");
        }
    }
}

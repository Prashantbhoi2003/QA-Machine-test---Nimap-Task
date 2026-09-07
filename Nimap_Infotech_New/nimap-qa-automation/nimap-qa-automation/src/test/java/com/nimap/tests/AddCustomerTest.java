package com.nimap.tests;

import com.nimap.base.BaseTest;
import com.nimap.pages.AddCustomerPage;
import com.nimap.pages.LoginPage;
import com.nimap.utils.ExcelUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Task 3: Add Customer using parameterization and validate it.
 *
 * Validation strategy: the real site did not show a distinct toast/popup
 * after Save, so success is verified by confirming the entered customer
 * name now appears in the results table (for the "Success" rows only -
 * negative rows just assert the form did not silently create a record with
 * invalid data, which for this app means checking the table does NOT gain
 * a row with that exact name when the name field itself was left blank/invalid).
 */
public class AddCustomerTest extends BaseTest {

    // TestNG always runs the superclass's @BeforeMethod (BaseTest.setUp,
    // which launches the browser) before this subclass's own @BeforeMethod,
    // so the driver is already initialised by the time this runs.
    @BeforeMethod
    public void loginFirst() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(
                System.getProperty("validUsername", "your_valid_username"),
                System.getProperty("validPassword", "your_valid_password"));
    }

    @DataProvider(name = "customerData")
    public Object[][] customerData() {
        return ExcelUtils.readSheet(
                "src/test/resources/testdata/TestData.xlsx", "AddCustomerData");
    }

    @Test(dataProvider = "customerData")
    public void validateAddCustomer(String name, String email, String phone, String expectedResult) {
        AddCustomerPage addCustomerPage = new AddCustomerPage(driver);
        addCustomerPage.navigateToCustomerModule();
        addCustomerPage.clickAddCustomer();
        addCustomerPage.addCustomer(name, email, phone);

        if (expectedResult.equalsIgnoreCase("Success")) {
            Assert.assertTrue(addCustomerPage.isCustomerListedInTable(name),
                    "Expected newly added customer '" + name + "' to appear in the table but it did not.");
        } else {
            // For invalid/blank-name rows, the required-field validation on
            // the form should block the record from ever reaching the table.
            Assert.assertFalse(addCustomerPage.isCustomerListedInTable(name),
                    "Expected invalid data to be rejected, but a row was still created.");
        }
    }
}
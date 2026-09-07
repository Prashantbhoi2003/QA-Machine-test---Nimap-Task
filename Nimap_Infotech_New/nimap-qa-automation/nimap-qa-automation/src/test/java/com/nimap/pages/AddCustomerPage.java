package com.nimap.pages;

import com.nimap.utils.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the "Add Customer" journey.
 * Real flow confirmed on the live site:
 *   Sidebar "My Customers" parent link (href="/", just expands the
 *   submenu via JS - does NOT navigate away) -> click child "My Customer"
 *   link (href="/customers") -> "Manage" button -> "New Customer" menu
 *   option -> modal form opens -> fill -> Save
 *
 * Navigating by clicking through the UI (rather than driver.get() to the
 * URL directly) is important here - a hard page load can lose the SPA's
 * in-memory auth/session state and bounce back to the login page.
 */
public class AddCustomerPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By customerMenuParent = By.xpath(ConfigReader.get("addcustomer.menu.locator"));
    private final By customerMenuChild = By.xpath(ConfigReader.get("addcustomer.submenu.locator"));
    private final By manageButton = By.xpath(ConfigReader.get("addcustomer.manage.locator"));
    private final By addButton = By.xpath(ConfigReader.get("addcustomer.addbutton.locator"));
    private final By nameField = By.name(ConfigReader.get("addcustomer.name.locator"));
    private final By emailField = By.name(ConfigReader.get("addcustomer.email.locator"));
    private final By phoneField = By.name(ConfigReader.get("addcustomer.phone.locator"));
    private final By saveButton = By.xpath(ConfigReader.get("addcustomer.save.locator"));
    private final By resultsTable = By.xpath(ConfigReader.get("addcustomer.success.locator"));

    public AddCustomerPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("explicit.wait")));
    }

    /**
     * Two clicks are required: the parent "My Customers" link only expands
     * the submenu (its href is "/" and is intercepted by the SPA router),
     * then the child "My Customer" link actually navigates to /customers.
     */
    public void navigateToCustomerModule() {
        wait.until(ExpectedConditions.elementToBeClickable(customerMenuParent)).click();
        wait.until(ExpectedConditions.elementToBeClickable(customerMenuChild)).click();
        wait.until(ExpectedConditions.elementToBeClickable(manageButton));
    }

    public void clickAddCustomer() {
        // A transient toast/notification (e.g. "Login Successful") can
        // briefly overlap the Manage button and block a normal click, so a
        // JS click is used here - it doesn't require the element to be
        // physically unobstructed like a native Selenium click does.
        WebElement manage = wait.until(ExpectedConditions.presenceOfElementLocated(manageButton));
        jsClick(manage);
        WebElement addOption = wait.until(ExpectedConditions.elementToBeClickable(addButton));
        jsClick(addOption);
    }

    private void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    /**
     * Full "Add Customer" flow - parameterized so it can be run once per
     * row of test data. Only "name" is a required field on the real form;
     * email/phone are passed through as-is (may be blank for negative cases).
     */
    public void addCustomer(String name, String email, String phone) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(nameField)).sendKeys(name);
        if (email != null && !email.isEmpty()) {
            driver.findElement(emailField).sendKeys(email);
        }
        if (phone != null && !phone.isEmpty()) {
            driver.findElement(phoneField).sendKeys(phone);
        }
        driver.findElement(saveButton).click();
        // Give the modal a moment to close after Save before the caller
        // checks the table - not strictly required now that
        // isCustomerListedInTable() polls, but avoids interacting with the
        // modal while it's still animating shut.
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(saveButton));
        } catch (org.openqa.selenium.TimeoutException ignored) {
            // Some flows may not remove the Save button from the DOM the
            // same way - isCustomerListedInTable()'s own polling wait below
            // still catches the row appearing, so this is not fatal.
        }
    }

    /**
     * The real site did not show a reliable distinct toast/popup after Save,
     * so success is instead verified by checking that the customer's name
     * now appears in the results table below the form.
     *
     * IMPORTANT: after clicking Save, the SPA re-fetches/re-renders the table
     * asynchronously, so the new row does not appear instantly. Checking the
     * table's text a single time right away is a race condition - the check
     * can run before the re-render finishes and wrongly report "not found".
     * wait.until(...) below polls repeatedly (every ~500ms, per Selenium's
     * default FluentWait behaviour) until the name shows up or the explicit
     * wait timeout is reached, instead of checking only once.
     */
    public boolean isCustomerListedInTable(String name) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(resultsTable));
            wait.until(d -> d.findElement(resultsTable).getText().contains(name));
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            // Name never showed up within the wait window - genuinely not found.
            return false;
        }
    }
}
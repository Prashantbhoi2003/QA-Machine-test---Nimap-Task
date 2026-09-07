package com.nimap.pages;

import com.nimap.utils.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the Login screen.
 * Locators are pulled from config.properties so they can be corrected
 * against the real DOM without touching Java code.
 */
public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By usernameField = locatorFor("login.username.locator");
    private final By passwordField = locatorFor("login.password.locator");
    private final By loginButton = locatorFor("login.submit.locator");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("explicit.wait")));
    }

    /**
     * Small helper: if the value in config.properties looks like an XPath
     * (starts with "//" or "(") treat it as one, otherwise treat it as a
     * "name" attribute. Update config.properties, not this method.
     */
    private By locatorFor(String key) {
        String value = ConfigReader.get(key);
        if (value.startsWith("//") || value.startsWith("(")) {
            return By.xpath(value);
        }
        return By.name(value);
    }

    public void enterUsername(String username) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameField));
        el.clear();
        el.sendKeys(username);
    }

    public void enterPassword(String password) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField));
        el.clear();
        el.sendKeys(password);
    }

    public void clickLogin() {
        // If username/password is blank or otherwise fails client-side
        // validation, the real site disables the Sign In button entirely
        // (confirmed on the live site: greyed-out button + inline error
        // like "Password cannot exceed 12 characters"). In that case the
        // button never becomes clickable - which is the CORRECT behaviour
        // for a negative test case, not an automation failure. A short,
        // dedicated wait (much shorter than the full explicit wait) is used
        // here so a blocked/disabled button is detected quickly instead of
        // burning the full 30s timeout on every negative-data row.
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(loginButton))
                    .click();
        } catch (org.openqa.selenium.TimeoutException e) {
            // Button stayed disabled - client-side validation blocked the
            // submit, which is the expected outcome for invalid/blank input.
            // Nothing more to do here; isLoginPageDisplayed() will correctly
            // report that we're still on the login page.
        }
    }

    /**
     * Full login flow in one call - this is what gets parameterized
     * from the DataProvider (different username/password combinations).
     */
    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
        // On a SUCCESSFUL login the SPA navigates away from this page
        // asynchronously, so checking isLoginPageDisplayed() immediately
        // after the click is a race condition (the check can run before
        // the navigation finishes and wrongly report "still on login
        // page"). This wait gives a real successful login time to
        // complete the redirect. For a FAILED login (wrong/blank
        // credentials) the username field simply never disappears, this
        // wait times out harmlessly, and isLoginPageDisplayed() below
        // correctly reports the login page is still shown.
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(usernameField));
        } catch (org.openqa.selenium.TimeoutException ignored) {
            // Still on the login page - expected for negative cases.
        }
    }

    public boolean isLoginPageDisplayed() {
        return driver.findElements(usernameField).size() > 0;
    }
}
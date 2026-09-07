package com.nimap.pages;

import com.nimap.utils.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the Dashboard - covers the PunchIn action and
 * reading the toast/popup message that appears afterwards.
 */
public class DashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By punchInButton = By.xpath(ConfigReader.get("punchin.button.locator"));
    private final By toastMessage = By.xpath(ConfigReader.get("toast.message.locator"));

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("explicit.wait")));
    }

    public void clickPunchIn() {
        wait.until(ExpectedConditions.elementToBeClickable(punchInButton)).click();
    }

    /**
     * Waits for the toast/popup to appear after PunchIn and returns its text
     * so the test can assert on the exact message shown.
     */
    public String getToastMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(toastMessage)).getText();
    }
}

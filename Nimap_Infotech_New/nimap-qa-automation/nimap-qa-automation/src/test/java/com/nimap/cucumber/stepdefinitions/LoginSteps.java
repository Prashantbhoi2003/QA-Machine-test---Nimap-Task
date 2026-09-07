package com.nimap.cucumber.stepdefinitions;

import com.nimap.pages.LoginPage;
import com.nimap.utils.ConfigReader;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;

/**
 * Step definitions backing src/test/resources/features/login.feature.
 * This is the Cucumber/BDD alternative to LoginTest.java (TestNG version) -
 * both implement the same "Automate Login Journey" requirement, use
 * whichever the interviewer prefers to see.
 */
public class LoginSteps {

    private WebDriver driver;
    private LoginPage loginPage;

    @Before
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        driver = new ChromeDriver(options);
        driver.get(ConfigReader.get("base.url"));
        loginPage = new LoginPage(driver);
    }

    @Given("the user is on the login page")
    public void the_user_is_on_the_login_page() {
        Assert.assertTrue(loginPage.isLoginPageDisplayed());
    }

    @When("the user enters username {string} and password {string}")
    public void the_user_enters_username_and_password(String username, String password) {
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
    }

    @And("clicks the login button")
    public void clicks_the_login_button() {
        loginPage.clickLogin();
    }

    @Then("the login result should be {string}")
    public void the_login_result_should_be(String expectedResult) {
        if (expectedResult.equalsIgnoreCase("Success")) {
            Assert.assertFalse(loginPage.isLoginPageDisplayed());
        } else {
            Assert.assertTrue(loginPage.isLoginPageDisplayed());
        }
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

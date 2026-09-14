package com.campusgate.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the main Dashboard / home page reached after login.
 */
public class DashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By WELCOME_HEADING    = By.cssSelector("h1.page-title");
    private static final By LOGOUT_BUTTON      = By.id("sidebar-logout-btn");
    private static final By NAV_SCAN           = By.cssSelector("a[href='/scan']");
    private static final By TOKEN_INPUT        = By.id("scan-token");
    private static final By SCAN_SUBMIT        = By.id("scan-submit");
    private static final By SCAN_RESULT_STATUS = By.cssSelector(".scan-result-status");
    private static final By SCAN_RESULT_MSG    = By.cssSelector(".scan-result-message");

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public DashboardPage waitForLoad() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(WELCOME_HEADING));
        return this;
    }

    public boolean isLoaded() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(WELCOME_HEADING)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getWelcomeText() {
        return driver.findElement(WELCOME_HEADING).getText();
    }

    public DashboardPage goToScan() {
        wait.until(ExpectedConditions.elementToBeClickable(NAV_SCAN)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(TOKEN_INPUT));
        return this;
    }

    public DashboardPage submitScan(String token) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(TOKEN_INPUT));
        input.clear();
        input.sendKeys(token);
        wait.until(ExpectedConditions.elementToBeClickable(SCAN_SUBMIT)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(SCAN_RESULT_STATUS));
        return this;
    }

    public String getScanResultStatus() {
        return driver.findElement(SCAN_RESULT_STATUS).getText();
    }

    public String getScanResultMessage() {
        return driver.findElement(SCAN_RESULT_MSG).getText();
    }

    public LoginPage logout() {
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON)).click();
        return new LoginPage(driver);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}

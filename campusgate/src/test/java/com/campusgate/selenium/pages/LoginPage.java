package com.campusgate.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the Login page.
 */
public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By EMAIL_INPUT    = By.id("login-email");
    private static final By PASSWORD_INPUT = By.id("login-password");
    private static final By LOGIN_BUTTON   = By.id("login-submit");
    private static final By ERROR_MESSAGE  = By.cssSelector(".form-error");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public LoginPage open(String baseUrl) {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT));
        return this;
    }

    public LoginPage enterEmail(String email) {
        WebElement field = wait.until(ExpectedConditions.elementToBeClickable(EMAIL_INPUT));
        field.clear();
        field.sendKeys(email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        WebElement field = driver.findElement(PASSWORD_INPUT);
        field.clear();
        field.sendKeys(password);
        return this;
    }

    public DashboardPage submit() {
        driver.findElement(LOGIN_BUTTON).click();
        return new DashboardPage(driver);
    }

    public LoginPage submitExpectingError() {
        driver.findElement(LOGIN_BUTTON).click();
        return this;
    }

    public String getErrorMessage() {
        try {
            WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE));
            return error.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public DashboardPage loginAs(String baseUrl, String email, String password) {
        open(baseUrl);
        enterEmail(email);
        enterPassword(password);
        return submit();
    }
}

package com.campusgate.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the Register page (/register)
 */
public class RegisterPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Form field locators
    private static final By NAME_INPUT           = By.id("register-name");
    private static final By EMAIL_INPUT          = By.id("register-email");
    private static final By PASSWORD_INPUT       = By.id("register-password");
    private static final By STUDENT_NUM_INPUT    = By.id("register-student-number");
    private static final By PHONE_INPUT          = By.id("register-phone");

    private static final By SUBMIT_BUTTON = By.cssSelector("button[type='submit']");

    private static final By ROLE_CARDS = By.cssSelector(".role-card");

    private static final By ANY_ERROR = By.cssSelector(".form-error");

    public RegisterPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public RegisterPage open(String baseUrl) {
        driver.get(baseUrl + "/register");
        wait.until(ExpectedConditions.visibilityOfElementLocated(NAME_INPUT));
        return this;
    }

    public RegisterPage selectRole(String roleLabel) {
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(ROLE_CARDS))
            .stream()
            .filter(el -> el.getText().trim().contains(roleLabel))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Role card not found: " + roleLabel))
            .click();
        return this;
    }

    public RegisterPage enterName(String name) {
        WebElement field = wait.until(ExpectedConditions.elementToBeClickable(NAME_INPUT));
        field.clear();
        field.sendKeys(name);
        return this;
    }

    public RegisterPage enterEmail(String email) {
        WebElement field = driver.findElement(EMAIL_INPUT);
        field.clear();
        field.sendKeys(email);
        return this;
    }

    public RegisterPage enterPassword(String password) {
        WebElement field = driver.findElement(PASSWORD_INPUT);
        field.clear();
        field.sendKeys(password);
        return this;
    }

    public RegisterPage enterStudentNumber(String studentNumber) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(STUDENT_NUM_INPUT));
        field.clear();
        field.sendKeys(studentNumber);
        return this;
    }

    public RegisterPage enterPhone(String phone) {
        WebElement field = driver.findElement(PHONE_INPUT);
        field.clear();
        field.sendKeys(phone);
        return this;
    }

    private void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", element);
    }

    public DashboardPage submit() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON));
        jsClick(btn);
        // Wait for SPA redirect to dashboard after auto-login
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        return new DashboardPage(driver);
    }

    public RegisterPage submitExpectingError() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON));
        jsClick(btn);
        return this;
    }

    public String getFirstErrorMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(ANY_ERROR)).getText();
        } catch (Exception e) {
            return "";
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}

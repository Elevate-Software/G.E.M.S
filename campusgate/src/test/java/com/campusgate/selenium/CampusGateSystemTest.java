package com.campusgate.selenium;

import com.campusgate.selenium.pages.DashboardPage;
import com.campusgate.selenium.pages.LoginPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System-level tests for the CampusGate application using Selenium WebDriver.
 */
class CampusGateSystemTest {

    private static final String BASE_URL  = System.getProperty("app.baseUrl", "http://localhost:5173");
    private static final String VALID_EMAIL    = System.getProperty("app.email",    "admin@campusgate.com");
    private static final String VALID_PASSWORD = System.getProperty("app.password", "Admin@123");

    private WebDriver driver;
    private LoginPage loginPage;

    @BeforeAll
    static void setupDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void openBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1280,800");
        driver    = new ChromeDriver(options);
        loginPage = new LoginPage(driver);
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    // Happy-path login
    @Test
    @DisplayName("Valid credentials redirect to dashboard")
    void login_withValidCredentials_shouldLoadDashboard() {
        DashboardPage dashboard = loginPage.loginAs(BASE_URL, VALID_EMAIL, VALID_PASSWORD);

        assertTrue(dashboard.isLoaded(),
                "Dashboard should be visible after successful login");
        // URL should no longer contain /login
        assertFalse(dashboard.getCurrentUrl().contains("/login"),
                "URL should have moved away from the login page");
    }

    // Wrong password
    @Test
    @DisplayName("Wrong password shows error and stays on login page")
    void login_withWrongPassword_shouldShowError() {
        loginPage.open(BASE_URL)
                 .enterEmail(VALID_EMAIL)
                 .enterPassword("wrong-password-xyz")
                 .submitExpectingError();

        // Either an error element is present, or the URL is still /login
        String currentUrl = driver.getCurrentUrl();
        String error      = loginPage.getErrorMessage();

        assertTrue(currentUrl.contains("/login") || !error.isBlank(),
                "Either the URL stays on /login or an error message is displayed");
    }

    // Empty form submission
    @Test
    @DisplayName("Submitting empty form does not navigate away from login")
    void login_withEmptyFields_shouldNotProceed() {
        loginPage.open(BASE_URL)
                 .submitExpectingError();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/login"),
                "URL should remain on the login page when the form is empty");
    }

    // Logout flow
    @Test
    @DisplayName("Logged-in user can log out and is redirected to login")
    void logout_shouldRedirectToLogin() {
        DashboardPage dashboard = loginPage.loginAs(BASE_URL, VALID_EMAIL, VALID_PASSWORD);
        assertTrue(dashboard.isLoaded(), "User must be logged in before testing logout");

        dashboard.logout();

        String url = driver.getCurrentUrl();
        assertTrue(url.contains("/login"),
                "After logout the application should redirect to the login page");
    }
}

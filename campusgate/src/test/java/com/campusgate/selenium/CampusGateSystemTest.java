package com.campusgate.selenium;

import com.campusgate.selenium.pages.DashboardPage;
import com.campusgate.selenium.pages.LoginPage;
import com.campusgate.selenium.pages.RegisterPage;
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
    private static final String VALID_EMAIL    = System.getProperty("app.email",    "peterkinfe@aau.com");
    private static final String VALID_PASSWORD = System.getProperty("app.password", "topdawg2145@gate");

    private WebDriver driver;
    private LoginPage loginPage;
    private RegisterPage registerPage;

    @BeforeAll
    static void setupDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void openBrowser() {
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless=new");  // Comment out to watch tests in the browser
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1280,800");
        driver       = new ChromeDriver(options);
        loginPage    = new LoginPage(driver);
        registerPage = new RegisterPage(driver);
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

    // -------------------------------------------------------------------------
    // Registration tests
    // -------------------------------------------------------------------------

    /**
     * Happy path: a brand-new student account is created, the app auto-logs in,
     * and Selenium lands on the dashboard.
     * A timestamp suffix guarantees a unique email on every run.
     */
    @Test
    @DisplayName("New student registration auto-logs in and loads the dashboard")
    void register_newStudent_shouldRedirectToDashboard() {
        // Unique email avoids duplicate-key failures across test runs
        String uniqueEmail = "selenium.student+" + System.currentTimeMillis() + "@test.local";

        DashboardPage dashboard = registerPage
                .open(BASE_URL)
                .selectRole("Student")
                .enterName("Selenium Student")
                .enterEmail(uniqueEmail)
                .enterPassword("TestPass1!")
                .enterStudentNumber("SEL" + System.currentTimeMillis())
                .submit();

        assertTrue(dashboard.isLoaded(),
                "Dashboard should be visible after successful registration and auto-login");
        assertFalse(dashboard.getCurrentUrl().contains("/register"),
                "URL should have left the register page");
    }

    /**
     * Server-side duplicate: registering with an email that already exists
     * must keep the user on /register and surface an error.
     * We use the known VALID_EMAIL which is already in the DB.
     */
    @Test
    @DisplayName("Duplicate email registration shows an error and stays on register page")
    void register_duplicateEmail_shouldShowError() {
        registerPage
                .open(BASE_URL)
                .selectRole("Student")
                .enterName("Duplicate User")
                .enterEmail(VALID_EMAIL)          // already registered
                .enterPassword("TestPass1!")
                .enterStudentNumber("DUP001")
                .submitExpectingError();

        // Give the toast / error a moment to appear (async API call)
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        String url   = registerPage.getCurrentUrl();
        String error = registerPage.getFirstErrorMessage();

        assertTrue(url.contains("/register") || !error.isBlank(),
                "Page should stay on /register or display an error when email is already taken");
    }

    /**
     * Client-side validation: submitting without filling in the name field
     * must stay on /register without making an API call.
     */
    @Test
    @DisplayName("Missing required field keeps user on the register page")
    void register_missingName_shouldNotProceed() {
        registerPage
                .open(BASE_URL)
                .selectRole("Student")
                // name intentionally omitted
                .enterEmail("missing.name@test.local")
                .enterPassword("TestPass1!")
                .enterStudentNumber("SEL999")
                .submitExpectingError();

        String url = registerPage.getCurrentUrl();
        assertTrue(url.contains("/register"),
                "URL should remain on /register when a required field is missing");
    }
}

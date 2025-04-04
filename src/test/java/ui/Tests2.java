package ui;

import configs.TestPropertiesConfig;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Tests2 {

    WebDriver driver;
    TestPropertiesConfig config = ConfigFactory.create(TestPropertiesConfig.class, System.getProperties());
    String baseUrl = config.getBaseUrl();
    String testPrompt = config.getTestPrompt();
    String username = config.getUsername();
    String password = config.getPassword();


    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        driver.get(baseUrl);
        driver.manage().window().maximize();
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    @Test
    @DisplayName("Successful login")
    void loginTest() {
        driver.get(baseUrl + "login-form.html");
        driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.cssSelector("button")).click();

        assertEquals("Login successful", driver.findElement(By.className("alert-success")).getText());
        assertEquals(baseUrl + "login-sucess.html?username=user&password=user", driver.getCurrentUrl());
    }

    @Test
    @DisplayName("2 - Infinite scroll")
    void infiniteScrollTests() {
        driver.get(baseUrl + "infinite-scroll.html");
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        List<WebElement> paragraphs = driver.findElements(By.xpath("//p[@class='lead']"));

        assertEquals(20, paragraphs.size(), "Ошибка на первом этапе");
        assertTrue(paragraphs.getFirst().getText().contains("Lorem ipsum dolor sit amet"), "Ошибка на первом этапе");

        new Actions(driver)
                .scrollToElement(paragraphs.get(19))
                .perform();
        new Actions(driver)
                .scrollByAmount(0, 100)
                .perform();

        List<WebElement> paragraphsNew = driver.findElements(By.xpath("//p[@class='lead']"));
        assertEquals(40, paragraphsNew.size(), "Ошибка на втором этапе");
        assertTrue(paragraphsNew.get(39).getText().contains("Magnis feugiat natoque proin"), "Ошибка на втором этапе");
    }

    @Test
    @DisplayName("2 - Shadow Dom")
    void shadowDomTests() {
        driver.get(baseUrl + "shadow-dom.html");
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        WebElement content = driver.findElement(By.id("content"));
        SearchContext shadowRoot = content.getShadowRoot();
        WebElement shadowText = shadowRoot.findElement(By.cssSelector("p"));
        assertEquals("Hello Shadow DOM", shadowText.getText());
    }

    @Test
    @DisplayName("2 - Cookies")
    void cookiesTests() {
        driver.get(baseUrl + "cookies.html");
        WebElement button = driver.findElement(By.cssSelector("button"));
        WebElement cookiesList = driver.findElement(By.xpath("//p[@id='cookies-list']"));

        Cookie cookie = driver.manage().getCookieNamed("username");
        String cookieValue = cookie.getValue();
        String cookiePath = cookie.getPath();
        assertAll(
                () -> assertEquals("John Doe", cookieValue),
                () -> assertTrue(cookiePath.contains("/")));

        driver.manage().addCookie(new Cookie("Lol", testPrompt));
        assertTrue(cookiesList.getText().isEmpty());
        new Actions(driver).click(button).perform();
        assertFalse(cookiesList.getText().isEmpty());
        assertTrue(cookiesList.getText().contains(testPrompt));
        System.out.println("Проверенный промпт: " + testPrompt);  //просто для себя
    }

    @Test
    @DisplayName("2 - iFrames")
    void iFramesTests() {
        driver.get(baseUrl + "iframes.html");
        assertThrows(NoSuchElementException.class, () -> driver.findElement(By.className("lead")));
        WebElement iframe = driver.findElement(By.id("my-iframe"));
        driver.switchTo().frame(iframe);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        assertTrue(driver.findElement(By.xpath("//p")).getText().contains("dolor sit amet consectetur"));
    }

    @Test
    @DisplayName("2 - Alerts")
    void alertsTests() {
        driver.get(baseUrl + "dialog-boxes.html");
        WebElement launchAlert = driver.findElement(By.id("my-alert"));
        WebElement launchConfirm = driver.findElement(By.id("my-confirm"));
        WebElement launchPrompt = driver.findElement(By.id("my-prompt"));
        WebElement launchModal = driver.findElement(By.id("my-modal"));

        launchAlert.click();
        Alert alert1 = driver.switchTo().alert();
        assertEquals("Hello world!", alert1.getText());
        alert1.accept();

        launchConfirm.click();
        Alert alert2 = driver.switchTo().alert();
        assertTrue(alert2.getText().contains("correct"));
        alert2.dismiss();
        assertEquals("You chose: false", driver.findElement(By.xpath("//p[@id='confirm-text']")).getText());

        launchPrompt.click();
        String testText = "Lorem ipsum";
        Alert alert3 = driver.switchTo().alert();
        alert3.sendKeys(testText);
        alert3.accept();
        assertEquals("You typed: " + testText, driver.findElement(By.id("prompt-text")).getText());

        launchModal.click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.cssSelector("button.btn-primary"))));
        assertEquals("This is the modal body", driver.findElement(By.xpath("//div[@class='modal-body']")).getText());
        driver.findElement(By.cssSelector("button.btn-primary")).click();
        assertTrue(driver.findElement(By.xpath("//p[@id='modal-text']")).getText().contains("Save changes"));
    }
}
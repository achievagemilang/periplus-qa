package com.openway.periplus;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class PeriplusShoppingCartTest {
    private WebDriver driver;
    private WebDriverWait wait;

    private String BASE_URL;
    private String EMAIL;
    private String PASSWORD;
    private final String BOOK_NAME = "The Effective Engineer"; 

    @BeforeClass
    public void setup() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, 10);
        
        loadConfig();
    }

    public void loadConfig() {
        Properties prop = new Properties();
        try (FileInputStream input = new FileInputStream("config.properties")) {
            prop.load(input);
            BASE_URL = prop.getProperty("BASE_URL");
            EMAIL = prop.getProperty("EMAIL");
            PASSWORD = prop.getProperty("PASSWORD");

            if (BASE_URL == null || EMAIL == null || PASSWORD == null) {
                throw new RuntimeException("Check your config.properties file. Missing properties.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    @Test(priority = 1)
    public void testLogin() {
        driver.get(BASE_URL);

        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("nav-signin-text")));
        loginLink.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login")));
        WebElement emailInput = driver.findElement(By.name("email"));
        WebElement passwordInput = driver.findElement(By.id("ps"));

        emailInput.sendKeys(EMAIL);
        passwordInput.sendKeys(PASSWORD);
        
        WebElement submitBtn = driver.findElement(By.cssSelector("input[type='submit']"));
        submitBtn.click();

        WebElement personalInfoDiv = wait.until(ExpectedConditions.visibilityOfElementLocated(
        By.cssSelector("div.row.row-account")));
        Assert.assertTrue(personalInfoDiv.isDisplayed(), "Not redirected to Account Page");

    }

    // Note: Periplus cannot add another book to the cart if the previous book is not removed.
    // It seems "Add to Cart" button actually means "Update Cart" according to the quantity we've assigned.
    @Test(priority = 2, dependsOnMethods = "testLogin")
    public void testSearchAndAddToCart() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.preloader")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cart_total")));

        WebElement cartTotalElem = driver.findElement(By.id("cart_total"));
        int initialTotal = Integer.parseInt(cartTotalElem.getText().trim());
        System.out.println("Initial cart_total: " + initialTotal);

        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("filter_name"))); 
        searchBox.sendKeys(BOOK_NAME);
        searchBox.submit();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.single-product")));

        List<WebElement> products = driver.findElements(By.cssSelector("div.single-product"));
        for (WebElement product : products) {
            WebElement titleLink = product.findElement(By.cssSelector("h3 > a"));
            String title = titleLink.getText().trim();
            if (title.contains(BOOK_NAME)) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", titleLink);
                break;
            }
        }
        
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.preloader")));
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.btn.btn-add-to-cart[onclick=\"willAddtoCart('33727997')\"]")
            ));
        addToCartButton.click();

        // Short delay for the UI to update
        try {
            Thread.sleep(3000);  
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
            
        cartTotalElem = driver.findElement(By.id("cart_total"));
        int currentTotal = Integer.parseInt(cartTotalElem.getText().trim());
        System.out.println("Current cart_total: " + currentTotal);

        boolean isUpdated = false;
        if (currentTotal == initialTotal + 1) {
            isUpdated = true;
        }
        Assert.assertTrue(isUpdated, "Book not added to the shopping cart");
    }

    @Test(priority = 3, dependsOnMethods = "testSearchAndAddToCart")
    public void testVerifyCartContents() {
        By notificationModal = By.id("Notification-Modal");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(notificationModal));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.preloader")));

        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("show-your-cart")));
        cartLink.click();

        wait.until(ExpectedConditions.urlToBe("https://www.periplus.com/checkout/cart"));
        Assert.assertEquals(driver.getCurrentUrl(), "https://www.periplus.com/checkout/cart", "URL is incorrect after clicking cart");

        WebElement productNameLink = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("p.product-name.limit-lines > a[href*='the-effective-engineer']")
        ));

        Assert.assertTrue(productNameLink.isDisplayed(), "Expected product is not displayed in the cart");
    }

    @AfterClass
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

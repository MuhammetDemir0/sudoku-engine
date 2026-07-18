package com.sudokuengine.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("null")
class FrontendBrowserTest {

    private static final int[][] PUZZLE = {
            { 5, 3, 0, 0, 7, 0, 0, 0, 0 },
            { 6, 0, 0, 1, 9, 5, 0, 0, 0 },
            { 0, 9, 8, 0, 0, 0, 0, 6, 0 },
            { 8, 0, 0, 0, 6, 0, 0, 0, 3 },
            { 4, 0, 0, 8, 0, 3, 0, 0, 1 },
            { 7, 0, 0, 0, 2, 0, 0, 0, 6 },
            { 0, 6, 0, 0, 0, 0, 2, 8, 0 },
            { 0, 0, 0, 4, 1, 9, 0, 0, 5 },
            { 0, 0, 0, 0, 8, 0, 0, 7, 9 }
    };

    private static final int[][] SOLUTION = {
            { 5, 3, 4, 6, 7, 8, 9, 1, 2 },
            { 6, 7, 2, 1, 9, 5, 3, 4, 8 },
            { 1, 9, 8, 3, 4, 2, 5, 6, 7 },
            { 8, 5, 9, 7, 6, 1, 4, 2, 3 },
            { 4, 2, 6, 8, 5, 3, 7, 9, 1 },
            { 7, 1, 3, 9, 2, 4, 8, 5, 6 },
            { 9, 6, 1, 5, 3, 7, 2, 8, 4 },
            { 2, 8, 7, 4, 1, 9, 6, 3, 5 },
            { 3, 4, 5, 2, 8, 6, 1, 7, 9 }
    };

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        driver = createHeadlessDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void homePageOpensHeadlessly() {
        openHomePage();

        assertEquals("Sudoku Engine", driver.getTitle());
        assertEquals("Ready", text(By.id("statusText")));
        assertEquals(81, driver.findElements(By.cssSelector("#board .cell")).size());
        assertTrue(displayed(By.id("generateBtn")));
        assertTrue(displayed(By.id("difficulty")));
    }

    @Test
    void userCanStartNewGameChooseDifficultyAndRequestHint() throws Exception {
        openHomePageWithMockApi();
        new Select(driver.findElement(By.id("difficulty"))).selectByValue("EXPERT");

        driver.findElement(By.id("generateBtn")).click();
        waitForMessage("EXPERT puzzle ready.");

        assertEquals("5", cell(0, 0).getDomProperty("value"));
        assertEquals("true", cell(0, 0).getDomProperty("readOnly"));
        assertEquals("", cell(0, 2).getDomProperty("value"));
        assertEquals("EXPERT", lastFetchBodyValue("difficulty"));

        driver.findElement(By.id("hintBtn")).click();
        wait.until(webDriver -> "1".equals(text(By.id("hintCount"))));

        WebElement hintedCell = cell(4, 4);
        assertEquals("5", hintedCell.getDomProperty("value"));
        assertTrue(hintedCell.getDomAttribute("class").contains("hint"));
        assertTrue(text(By.id("messageText")).contains("Hint 1 used."));
    }

    @Test
    void userCanEnterCellValueAndResetPuzzle() throws Exception {
        openHomePageWithMockApi();
        driver.findElement(By.id("generateBtn")).click();
        waitForMessage("MEDIUM puzzle ready.");

        WebElement editableCell = cell(0, 2);
        editableCell.click();
        editableCell.sendKeys("4");
        wait.until(webDriver -> "4".equals(cell(0, 2).getDomProperty("value")));

        driver.findElement(By.id("resetBtn")).click();
        waitForMessage("Puzzle reset.");

        assertEquals("", cell(0, 2).getDomProperty("value"));
        assertEquals("5", cell(0, 0).getDomProperty("value"));
    }

    @Test
    void completedPuzzleShowsSuccessMessageAndDisablesHints() throws Exception {
        openHomePageWithMockApi();
        driver.findElement(By.id("generateBtn")).click();
        waitForMessage("MEDIUM puzzle ready.");

        fillSolutionValues();

        waitForMessage("Completed board verified by the server.");
        assertTrue(Boolean.parseBoolean(driver.findElement(By.id("hintBtn")).getDomProperty("disabled")));
    }

    private WebDriver createHeadlessDriver() {
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage",
                    "--window-size=1280,900");
            return new ChromeDriver(options);
        } catch (WebDriverException chromeFailure) {
            try {
                EdgeOptions options = new EdgeOptions();
                options.addArguments("--headless=new", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage",
                        "--window-size=1280,900");
                return new EdgeDriver(options);
            } catch (WebDriverException edgeFailure) {
                Assumptions.assumeTrue(false,
                        "No headless Chrome or Edge browser is available for Selenium tests.");
                throw edgeFailure;
            }
        }
    }

    private void openHomePage() {
        driver.get("http://localhost:" + port + "/");
        wait.until(webDriver -> displayed(By.id("board")));
    }

    private void openHomePageWithMockApi() throws JsonProcessingException {
        openHomePage();
        installMockApi();
    }

    private void installMockApi() throws JsonProcessingException {
        String puzzleJson = objectMapper.writeValueAsString(PUZZLE);
        String script = """
                window.__sudokuFetchCalls = [];
                window.fetch = async (url, options = {}) => {
                    const path = new URL(url, window.location.origin).pathname;
                    const body = options.body ? JSON.parse(options.body) : {};
                    window.__sudokuFetchCalls.push({ path, body });
                    const json = payload => Promise.resolve({
                        ok: true,
                        status: 200,
                        json: async () => payload
                    });

                    if (path.endsWith("/generate")) {
                        return json({ difficulty: body.difficulty, puzzle: __PUZZLE__ });
                    }
                    if (path.endsWith("/hint")) {
                        return json({
                            row: 4,
                            col: 4,
                            value: 5,
                            reason: "Cell (4,4) can safely be 5 based on the solved board."
                        });
                    }
                    if (path.endsWith("/validate")) {
                        return json({ valid: true, violations: [] });
                    }
                    return json({ solved: true, board: __PUZZLE__, metrics: {}, steps: [] });
                };
                """.replace("__PUZZLE__", puzzleJson);
        ((JavascriptExecutor) driver).executeScript(script);
    }

    private void fillSolutionValues() {
        for (int row = 0; row < SOLUTION.length; row++) {
            for (int col = 0; col < SOLUTION[row].length; col++) {
                if (PUZZLE[row][col] != 0) {
                    continue;
                }
                WebElement editableCell = cell(row, col);
                editableCell.click();
                editableCell.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                editableCell.sendKeys(String.valueOf(SOLUTION[row][col]));
            }
        }
    }

    private WebElement cell(int row, int col) {
        return driver.findElement(By.cssSelector("#board .cell[data-row='" + row + "'][data-col='" + col + "']"));
    }

    private void waitForMessage(String message) {
        wait.until(webDriver -> message.equals(text(By.id("messageText"))));
    }

    private String text(By by) {
        return driver.findElement(by).getText();
    }

    private boolean displayed(By by) {
        return driver.findElement(by).isDisplayed();
    }

    private String lastFetchBodyValue(String key) {
        Object value = ((JavascriptExecutor) driver).executeScript("""
                const calls = window.__sudokuFetchCalls || [];
                const last = calls[calls.length - 1] || { body: {} };
                return last.body[arguments[0]];
                """, key);
        return String.valueOf(value);
    }
}

package maestro.drivers

import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory

/**
 * Shared utility functions for web drivers (WebDriver and CdpWebDriver)
 */
object WebDriverUtils {
    private val LOGGER = LoggerFactory.getLogger(WebDriverUtils::class.java)

    /**
     * Switch to a browser tab by index.
     *
     * @param driver The Selenium WebDriver instance
     * @param index The 0-based index of the tab to switch to
     * @return The new set of window handles after switching
     * @throws IllegalArgumentException if the index is out of bounds
     */
    fun switchTab(driver: WebDriver, index: Int): Set<String> {
        val handles = driver.windowHandles.toList()

        if (index < 0 || index >= handles.size) {
            throw IllegalArgumentException("Tab index $index is out of bounds. Available tabs: ${handles.size}")
        }

        val targetHandle = handles[index]
        LOGGER.info("Switching to tab at index $index (handle: $targetHandle)")
        driver.switchTo().window(targetHandle)

        return driver.windowHandles
    }

    /**
     * Get the number of open browser tabs.
     *
     * @param driver The Selenium WebDriver instance
     * @return The number of open tabs
     */
    fun getTabCount(driver: WebDriver): Int {
        return driver.windowHandles.size
    }
}

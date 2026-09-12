package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.CreditCard
import com.example.ui.components.CreditCardVisual
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleCard = CreditCard(
        id = 1L,
        name = "Like U",
        bank = "Santander",
        cutoffDay = 12,
        paymentDueDay = 2,
        creditLimit = 65000.0,
        primaryColorHex = 0xFFEC0000,
        secondaryColorHex = 0xFF990000,
        last4Digits = "4123",
        network = "Mastercard"
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        CreditCardVisual(card = sampleCard)
      }
    }

    composeTestRule.waitForIdle()

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

package dev.def.lab.droid.crypto

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class CryptoScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun text(resourceId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resourceId)

    @Test
    fun exerciseScreenShowsTheMessageSection() {
        composeRule
            .onNodeWithText(text(R.string.section_message))
            .assertIsDisplayed()
    }

    @Test
    fun comparisonScreenOpensFromTheExerciseScreen() {
        composeRule
            .onNodeWithText(text(R.string.action_open_comparison))
            .performClick()

        composeRule
            .onNodeWithText(text(R.string.section_fixed_key))
            .assertIsDisplayed()
    }
}

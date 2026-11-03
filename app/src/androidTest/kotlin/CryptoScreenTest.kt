package defdev.labdroid.crypto

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
        composeRule.onNodeWithText(text(R.string.section_message)).assertIsDisplayed()
    }

    @Test
    fun resourceCaseReportsNotImplemented() {
        composeRule.onNodeWithText("2").performClick()
        composeRule.onNodeWithText(text(R.string.flow_resource_title)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.action_encrypt)).performClick()
        composeRule.onNodeWithText(text(R.string.operation_not_implemented)).assertIsDisplayed()
    }
}

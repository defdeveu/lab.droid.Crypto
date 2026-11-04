package defdev.labdroid.crypto

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
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
    fun resourceCaseEncryptsAndDecrypts() {
        composeRule.onNodeWithText("2").performClick()
        composeRule.onNodeWithText(text(R.string.flow_resource_title)).assertIsDisplayed()
        composeRule.onNode(hasText(text(R.string.section_message)) and hasSetTextAction())
            .performTextInput("Demo message")
        composeRule.onNodeWithText(text(R.string.action_encrypt)).performScrollTo().performClick()
        composeRule.onNodeWithText("0cb5e0ea8906cb6efc0867ec35590629")
            .performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.action_decrypt)).performScrollTo().performClick()
        composeRule.onNode(hasText("Demo message") and !hasSetTextAction())
            .performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("0123456789abcdef0123456789abcde2")
            .performScrollTo().assertIsDisplayed()
    }

    @Test
    fun httpsCaseRequiresAMessage() {
        composeRule.onNodeWithText("3").performClick()
        composeRule.onNodeWithText(text(R.string.flow_https_title)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.action_encrypt)).performClick()
        composeRule.onNodeWithText(text(R.string.error_empty_input)).assertIsDisplayed()
    }

    @Test
    fun keystoreCaseEncryptsAndDecrypts() {
        composeRule.onNodeWithText("4").performClick()
        composeRule.onNodeWithText(text(R.string.flow_keystore_title)).assertIsDisplayed()
        composeRule.onNode(hasText(text(R.string.section_message)) and hasSetTextAction())
            .performTextInput("Demo message")
        composeRule.onNodeWithText(text(R.string.action_encrypt)).performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onNode(hasText(text(R.string.section_ciphertext)) and hasSetTextAction())
                .fetchSemanticsNode().config[SemanticsProperties.EditableText].text.length == 80
        }
        composeRule.onNodeWithText(text(R.string.action_decrypt)).performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(hasText("Demo message") and !hasSetTextAction())
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNode(hasText("Demo message") and !hasSetTextAction())
            .performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.key_non_exportable), substring = true)
            .performScrollTo().assertIsDisplayed()
    }
}

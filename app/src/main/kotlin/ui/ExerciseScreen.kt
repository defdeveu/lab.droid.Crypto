package defdev.labdroid.crypto.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import defdev.labdroid.crypto.CryptoViewModel
import defdev.labdroid.crypto.R
import defdev.labdroid.crypto.error.CryptoError

@Composable
fun ExerciseScreen(viewModel: CryptoViewModel, onOpenComparison: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = { LabHeader(title = stringResource(R.string.app_name)) }) { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets).imePadding().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = stringResource(R.string.exercise_title), style = MaterialTheme.typography.titleLargeEmphasized)
                    Text(
                        text = stringResource(R.string.exercise_supporting),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                MessageField(
                    label = stringResource(R.string.section_message),
                    value = state.plaintext,
                    onValueChange = viewModel::onPlaintextChange,
                    enabled = !state.isRunning,
                )
                PrimaryAction(
                    label = stringResource(if (state.isRunning) R.string.operation_working else R.string.action_encrypt),
                    onClick = viewModel::encrypt,
                    isRunning = state.isRunning,
                    showShield = true,
                )
                ResultGroup {
                    OperationFeedback(isRunning = state.isRunning, error = state.error)
                    MessageField(
                        label = stringResource(R.string.section_ciphertext),
                        value = state.ciphertext,
                        onValueChange = viewModel::onCiphertextChange,
                        enabled = !state.isRunning,
                        placeholder = stringResource(R.string.placeholder_ciphertext),
                    )
                    DecryptAction(onClick = viewModel::decrypt, enabled = !state.isRunning)
                    OutputSection(
                        title = stringResource(R.string.section_plaintext),
                        text = state.decrypted,
                        placeholder = stringResource(R.string.placeholder_plaintext),
                    )
                }
                TextButton(onClick = onOpenComparison) {
                    Text(text = stringResource(R.string.action_open_comparison))
                }
            }
        }
    }
}

@StringRes
internal fun CryptoError.messageRes(): Int = when (this) {
    CryptoError.EMPTY_INPUT -> R.string.error_empty_input
    CryptoError.INVALID_HEX -> R.string.error_invalid_hex
    CryptoError.KEY_UNAVAILABLE -> R.string.error_key_unavailable
    CryptoError.OPERATION_FAILED -> R.string.error_operation_failed
}

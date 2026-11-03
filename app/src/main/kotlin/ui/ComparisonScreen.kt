package defdev.labdroid.crypto.ui

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import defdev.labdroid.crypto.ComparisonViewModel
import defdev.labdroid.crypto.R

@Composable
fun ComparisonScreen(viewModel: ComparisonViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { LabHeader(title = stringResource(R.string.comparison_title), onBack = onBack) },
    ) { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets).imePadding().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Text(
                    text = stringResource(R.string.comparison_supporting),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                MessageField(
                    label = stringResource(R.string.section_message),
                    value = state.plaintext,
                    onValueChange = viewModel::onPlaintextChange,
                    enabled = !state.isRunning,
                )
                PrimaryAction(
                    label = stringResource(if (state.isRunning) R.string.operation_working else R.string.action_compare),
                    onClick = viewModel::compare,
                    isRunning = state.isRunning,
                )
                ResultGroup {
                    OperationFeedback(isRunning = state.isRunning, error = state.error)
                    OutputSection(
                        title = stringResource(R.string.section_fixed_key),
                        text = state.fixedKeyCiphertext,
                        placeholder = stringResource(R.string.placeholder_fixed_key),
                    )
                    MessageField(
                        label = stringResource(R.string.section_device_key),
                        value = state.deviceKeyCiphertext,
                        onValueChange = viewModel::onDeviceKeyCiphertextChange,
                        enabled = !state.isRunning,
                        placeholder = stringResource(R.string.placeholder_device_key),
                    )
                    DecryptAction(onClick = viewModel::decryptDeviceKeyCiphertext, enabled = !state.isRunning)
                    OutputSection(
                        title = stringResource(R.string.section_plaintext),
                        text = state.deviceKeyPlaintext,
                        placeholder = stringResource(R.string.placeholder_plaintext),
                    )
                }
            }
        }
    }
}

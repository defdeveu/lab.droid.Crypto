package dev.def.lab.droid.crypto.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.def.lab.droid.crypto.CryptoViewModel
import dev.def.lab.droid.crypto.R
import dev.def.lab.droid.crypto.error.CryptoError

@Composable
fun ExerciseScreen(
    viewModel: CryptoViewModel,
    onOpenComparison: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(R.string.app_name)) }) },
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.exercise_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.exercise_supporting),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                SectionCard(title = stringResource(R.string.section_message)) {
                    OutlinedTextField(
                        value = state.plaintext,
                        onValueChange = viewModel::onPlaintextChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isRunning,
                    )
                }

                PrimaryAction(
                    label = stringResource(R.string.action_encrypt),
                    onClick = viewModel::encrypt,
                    enabled = !state.isRunning,
                )

                SectionCard(title = stringResource(R.string.section_ciphertext)) {
                    OutlinedTextField(
                        value = state.ciphertext,
                        onValueChange = viewModel::onCiphertextChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isRunning,
                    )
                    OutputPane(
                        text = state.ciphertext,
                        placeholder = stringResource(R.string.placeholder_ciphertext),
                    )
                }

                PrimaryAction(
                    label = stringResource(R.string.action_decrypt),
                    onClick = viewModel::decrypt,
                    enabled = !state.isRunning,
                )

                SectionCard(title = stringResource(R.string.section_plaintext)) {
                    OutputPane(
                        text = state.decrypted,
                        placeholder = stringResource(R.string.placeholder_plaintext),
                    )
                }

                state.error?.let { error ->
                    Text(
                        text = stringResource(error.messageRes()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
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

package dev.def.lab.droid.crypto.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.def.lab.droid.crypto.ComparisonViewModel
import dev.def.lab.droid.crypto.R

@Composable
fun ComparisonScreen(
    viewModel: ComparisonViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.comparison_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
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
                Text(
                    text = stringResource(R.string.comparison_supporting),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                SectionCard(title = stringResource(R.string.section_message)) {
                    OutlinedTextField(
                        value = state.plaintext,
                        onValueChange = viewModel::onPlaintextChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isRunning,
                    )
                }

                PrimaryAction(
                    label = stringResource(R.string.action_compare),
                    onClick = viewModel::compare,
                    enabled = !state.isRunning,
                )

                SectionCard(title = stringResource(R.string.section_fixed_key)) {
                    OutputPane(
                        text = state.fixedKeyCiphertext,
                        placeholder = stringResource(R.string.placeholder_fixed_key),
                    )
                }

                SectionCard(title = stringResource(R.string.section_device_key)) {
                    OutlinedTextField(
                        value = state.deviceKeyCiphertext,
                        onValueChange = viewModel::onDeviceKeyCiphertextChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isRunning,
                    )
                }

                PrimaryAction(
                    label = stringResource(R.string.action_decrypt),
                    onClick = viewModel::decryptDeviceKeyCiphertext,
                    enabled = !state.isRunning,
                )

                SectionCard(title = stringResource(R.string.section_plaintext)) {
                    OutputPane(
                        text = state.deviceKeyPlaintext,
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
            }
        }
    }
}

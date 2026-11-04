package defdev.labdroid.crypto.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import defdev.labdroid.crypto.CryptoOperation
import defdev.labdroid.crypto.CryptoViewModel
import defdev.labdroid.crypto.R
import defdev.labdroid.crypto.error.CryptoError
import defdev.labdroid.crypto.crypto.KeySecurity
import defdev.labdroid.crypto.flows.FlowId

@Composable
fun CryptoScreen(
    selectedFlow: FlowId,
    onSelectFlow: (FlowId) -> Unit,
    viewModel: CryptoViewModel,
) {
    Scaffold(topBar = { LabHeader(title = stringResource(R.string.app_name)) }) { insets ->
        Row(modifier = Modifier
            .fillMaxSize()
            .padding(insets)) {
            WideNavigationRail(
                modifier = Modifier.fillMaxHeight().width(76.8.dp),
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = WideNavigationRailDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                FlowId.entries.forEachIndexed { index, flow ->
                    val description = stringResource(
                        R.string.flow_navigation,
                        index + 1,
                        stringResource(flow.titleRes())
                    )
                    WideNavigationRailItem(
                        selected = selectedFlow == flow,
                        onClick = { onSelectFlow(flow) },
                        icon = {
                            Text(
                                text = stringResource(R.string.flow_number, index + 1),
                                style = MaterialTheme.typography.titleMediumEmphasized,
                                fontSize = 20.sp,
                            )
                        },
                        label = null,
                        railExpanded = false,
                        modifier = Modifier.semantics { contentDescription = description },
                    )
                }
            }
            key(selectedFlow) {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .imePadding()
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
                                text = stringResource(selectedFlow.titleRes()),
                                style = MaterialTheme.typography.titleLargeEmphasized,
                            )
                            Text(
                                text = stringResource(selectedFlow.descriptionRes()),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (selectedFlow == FlowId.BIOMETRIC && state.operation == null && state.error != null) {
                                OperationFeedback(isRunning = false, error = state.error)
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            MessageField(
                                label = stringResource(R.string.section_message),
                                value = state.plaintext,
                                onValueChange = viewModel::onPlaintextChange,
                                enabled = !state.isRunning,
                                minLines = 2,
                            )
                            PrimaryAction(
                                label = stringResource(
                                    if (state.isRunning && state.operation == CryptoOperation.ENCRYPT) {
                                        R.string.operation_encrypting
                                    } else R.string.action_encrypt,
                                ),
                                onClick = viewModel::encrypt,
                                isRunning = state.isRunning,
                                showShield = true,
                            )
                            if (state.operation == CryptoOperation.ENCRYPT) {
                                OperationFeedback(isRunning = state.isRunning, error = state.error)
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            MessageField(
                                label = stringResource(R.string.section_ciphertext),
                                value = state.ciphertext,
                                onValueChange = viewModel::onCiphertextChange,
                                enabled = !state.isRunning,
                                placeholder = stringResource(R.string.placeholder_ciphertext),
                            )
                            PrimaryAction(
                                label = stringResource(
                                    if (state.isRunning && state.operation == CryptoOperation.DECRYPT) {
                                        R.string.operation_decrypting
                                    } else R.string.action_decrypt,
                                ),
                                onClick = viewModel::decrypt,
                                isRunning = state.isRunning,
                            )
                            if (state.operation == CryptoOperation.DECRYPT) {
                                OperationFeedback(isRunning = state.isRunning, error = state.error)
                            }
                        }
                        OutputField(
                            title = stringResource(R.string.section_plaintext),
                            text = state.decrypted,
                            placeholder = stringResource(R.string.placeholder_plaintext),
                        )
                        OutputField(
                            title = stringResource(R.string.section_key),
                            text = when (selectedFlow) {
                                FlowId.HARDCODED, FlowId.RESOURCE -> state.keyValue
                                FlowId.HTTPS -> stringResource(R.string.key_not_fetched)
                                FlowId.KEYSTORE, FlowId.BIOMETRIC -> protectedKeyText(state.keySecurity, state.error)
                            },
                            placeholder = stringResource(R.string.key_not_available),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun protectedKeyText(security: KeySecurity?, error: CryptoError?): String {
    if (error == CryptoError.KEY_UNAVAILABLE) {
        return stringResource(R.string.error_key_unavailable)
    }
    if (security == null) {
        if (error == CryptoError.BIOMETRIC_NOT_ENROLLED || error == CryptoError.BIOMETRIC_UNAVAILABLE) {
            return stringResource(R.string.key_not_available)
        }
        return stringResource(R.string.key_non_exportable)
    }
    val level = when (security) {
        KeySecurity.SOFTWARE -> R.string.key_security_software
        KeySecurity.TEE -> R.string.key_security_tee
        KeySecurity.STRONGBOX -> R.string.key_security_strongbox
        KeySecurity.HARDWARE -> R.string.key_security_hardware
        KeySecurity.UNKNOWN -> R.string.key_security_unknown
    }
    val insideHardware = when (security) {
        KeySecurity.SOFTWARE -> R.string.value_no
        KeySecurity.TEE, KeySecurity.STRONGBOX, KeySecurity.HARDWARE -> R.string.value_yes
        KeySecurity.UNKNOWN -> R.string.key_security_unknown
    }
    return stringResource(R.string.key_protected_details, stringResource(level), stringResource(insideHardware))
}

@StringRes
private fun FlowId.titleRes(): Int = when (this) {
    FlowId.HARDCODED -> R.string.flow_hardcoded_title
    FlowId.RESOURCE -> R.string.flow_resource_title
    FlowId.HTTPS -> R.string.flow_https_title
    FlowId.KEYSTORE -> R.string.flow_keystore_title
    FlowId.BIOMETRIC -> R.string.flow_biometric_title
}

@StringRes
private fun FlowId.descriptionRes(): Int = when (this) {
    FlowId.HARDCODED -> R.string.flow_hardcoded_description
    FlowId.RESOURCE -> R.string.flow_resource_description
    FlowId.HTTPS -> R.string.flow_https_description
    FlowId.KEYSTORE -> R.string.flow_keystore_description
    FlowId.BIOMETRIC -> R.string.flow_biometric_description
}

@StringRes
internal fun CryptoError.messageRes(): Int = when (this) {
    CryptoError.EMPTY_INPUT -> R.string.error_empty_input
    CryptoError.INVALID_HEX -> R.string.error_invalid_hex
    CryptoError.KEY_UNAVAILABLE -> R.string.error_key_unavailable
    CryptoError.OPERATION_FAILED -> R.string.error_operation_failed
    CryptoError.NOT_IMPLEMENTED -> R.string.operation_not_implemented
    CryptoError.BIOMETRIC_NOT_ENROLLED -> R.string.error_biometric_not_enrolled
    CryptoError.BIOMETRIC_UNAVAILABLE -> R.string.error_biometric_unavailable
    CryptoError.BIOMETRIC_LOCKED_OUT -> R.string.error_biometric_locked_out
    CryptoError.AUTHENTICATION_CANCELLED -> R.string.authentication_cancelled
    CryptoError.AUTHENTICATION_FAILED -> R.string.error_authentication_failed
}

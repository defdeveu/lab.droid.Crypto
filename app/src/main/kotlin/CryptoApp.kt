package defdev.labs.droid.crypto

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import defdev.labs.droid.crypto.flows.FlowId
import defdev.labs.droid.crypto.ui.CryptoScreen
import defdev.labs.droid.crypto.ui.BiometricPromptHost

@Composable
fun CryptoApp(container: AppContainer) {
    BiometricPromptHost(container.biometricAuthorizer)
    var selectedId by rememberSaveable { mutableStateOf(FlowId.HARDCODED.value) }
    val selectedFlow = FlowId.entries.first { it.value == selectedId }
    val viewModel: CryptoViewModel = viewModel(
        key = selectedFlow.value,
        factory = container.viewModelFactory(selectedFlow),
    )
    CryptoScreen(
        selectedFlow = selectedFlow,
        onSelectFlow = { selectedId = it.value },
        viewModel = viewModel,
    )
}

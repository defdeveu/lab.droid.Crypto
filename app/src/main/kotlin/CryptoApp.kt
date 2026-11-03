package defdev.labdroid.crypto

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import defdev.labdroid.crypto.flows.FlowId
import defdev.labdroid.crypto.ui.CryptoScreen

@Composable
fun CryptoApp(container: AppContainer) {
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

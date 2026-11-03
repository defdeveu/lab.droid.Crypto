package defdev.labdroid.crypto

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import defdev.labdroid.crypto.crypto.CryptoService
import defdev.labdroid.crypto.flows.FlowId
import defdev.labdroid.crypto.flows.hardcoded.HardcodedFlow

class AppContainer {
    private val flows: Map<FlowId, CryptoService> = mapOf(
        FlowId.HARDCODED to HardcodedFlow(),
    )

    fun viewModelFactory(flow: FlowId): ViewModelProvider.Factory = viewModelFactory {
        initializer { CryptoViewModel(flows[flow]) }
    }
}

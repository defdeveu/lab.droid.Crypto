package defdev.labdroid.crypto

import android.content.res.Resources
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import defdev.labdroid.crypto.crypto.CryptoService
import defdev.labdroid.crypto.flows.FlowId
import defdev.labdroid.crypto.flows.hardcoded.HardcodedFlow
import defdev.labdroid.crypto.flows.resource.ResourceFlow

class AppContainer(resources: Resources) {
    private val flows: Map<FlowId, CryptoService> = mapOf(
        FlowId.HARDCODED to HardcodedFlow(),
        FlowId.RESOURCE to ResourceFlow(resources),
    )

    fun viewModelFactory(flow: FlowId): ViewModelProvider.Factory = viewModelFactory {
        initializer { CryptoViewModel(flows[flow]) }
    }
}

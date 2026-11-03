package defdev.labdroid.crypto

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import defdev.labdroid.crypto.crypto.CryptoService
import defdev.labdroid.crypto.flows.FlowId
import defdev.labdroid.crypto.flows.hardcoded.HardcodedFlow
import defdev.labdroid.crypto.flows.keystore.KeystoreFlow
import defdev.labdroid.crypto.flows.resource.ResourceFlow
import kotlinx.coroutines.CoroutineScope

class AppContainer(context: Context, scope: CoroutineScope) {
    private val flows: Map<FlowId, CryptoService> = mapOf(
        FlowId.HARDCODED to HardcodedFlow(),
        FlowId.RESOURCE to ResourceFlow(context.resources),
        FlowId.KEYSTORE to KeystoreFlow(context, scope),
    )

    fun viewModelFactory(flow: FlowId): ViewModelProvider.Factory = viewModelFactory {
        initializer { CryptoViewModel(flows[flow]) }
    }
}

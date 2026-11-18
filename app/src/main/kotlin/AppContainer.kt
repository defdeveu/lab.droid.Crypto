package defdev.labs.droid.crypto

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import defdev.labs.droid.crypto.crypto.CryptoService
import defdev.labs.droid.crypto.flows.FlowId
import defdev.labs.droid.crypto.flows.biometric.BiometricAuthorizer
import defdev.labs.droid.crypto.flows.biometric.BiometricFlow
import defdev.labs.droid.crypto.flows.hardcoded.HardcodedFlow
import defdev.labs.droid.crypto.flows.https.HttpsFlow
import defdev.labs.droid.crypto.flows.keystore.KeystoreFlow
import defdev.labs.droid.crypto.flows.resource.ResourceFlow
import defdev.labs.droid.crypto.error.CryptoError
import kotlinx.coroutines.CoroutineScope

class AppContainer(context: Context, scope: CoroutineScope) {
    val biometricAuthorizer = BiometricAuthorizer()

    private val httpsEndpoint = HttpsFlow.parseEndpoint(BuildConfig.LAB_ENDPOINT)

    private val flows: Map<FlowId, CryptoService?> = mapOf(
        FlowId.HARDCODED to HardcodedFlow(),
        FlowId.RESOURCE to ResourceFlow(context.resources),
        FlowId.HTTPS to httpsEndpoint?.let { HttpsFlow(it) },
        FlowId.KEYSTORE to KeystoreFlow(context, scope),
        FlowId.BIOMETRIC to BiometricFlow(context, scope, biometricAuthorizer),
    )

    fun viewModelFactory(flow: FlowId): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            CryptoViewModel(
                flows[flow],
                configurationError = if (flow == FlowId.HTTPS && httpsEndpoint == null) CryptoError.INVALID_ENDPOINT else null,
            )
        }
    }
}

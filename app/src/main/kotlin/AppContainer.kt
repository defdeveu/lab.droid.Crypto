package defdev.labdroid.crypto

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import defdev.labdroid.crypto.crypto.CryptoService
import defdev.labdroid.crypto.flows.FlowId
import defdev.labdroid.crypto.flows.biometric.BiometricAuthorizer
import defdev.labdroid.crypto.flows.biometric.BiometricFlow
import defdev.labdroid.crypto.flows.hardcoded.HardcodedFlow
import defdev.labdroid.crypto.flows.https.HttpsFlow
import defdev.labdroid.crypto.flows.keystore.KeystoreFlow
import defdev.labdroid.crypto.flows.resource.ResourceFlow
import defdev.labdroid.crypto.error.CryptoError
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

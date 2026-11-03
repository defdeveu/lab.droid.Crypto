package defdev.labdroid.crypto

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import defdev.labdroid.crypto.crypto.CryptoService
import defdev.labdroid.crypto.flows.hardcoded.HardcodedFlow
import defdev.labdroid.crypto.flows.keystore.KeystoreFlow
import defdev.labdroid.crypto.flows.keystore.KeystoreKeySource
import defdev.labdroid.crypto.crypto.SecretKeySource

class AppContainer {
    private val keySource: SecretKeySource = KeystoreKeySource()
    private val ecbCrypto: CryptoService = HardcodedFlow()
    private val gcmCrypto: CryptoService = KeystoreFlow(keySource)

    val cryptoViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { CryptoViewModel(ecbCrypto) }
    }

    val comparisonViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { ComparisonViewModel(ecbCrypto, gcmCrypto) }
    }
}

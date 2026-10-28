package dev.def.lab.droid.crypto

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.def.lab.droid.crypto.service.CryptoService
import dev.def.lab.droid.crypto.service.EcbCryptoService
import dev.def.lab.droid.crypto.service.GcmCryptoService
import dev.def.lab.droid.crypto.service.KeystoreKeySource
import dev.def.lab.droid.crypto.service.SecretKeySource

class AppContainer {
    private val keySource: SecretKeySource = KeystoreKeySource()
    private val ecbCrypto: CryptoService = EcbCryptoService()
    private val gcmCrypto: CryptoService = GcmCryptoService(keySource)

    val cryptoViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { CryptoViewModel(ecbCrypto) }
    }

    val comparisonViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { ComparisonViewModel(ecbCrypto, gcmCrypto) }
    }
}

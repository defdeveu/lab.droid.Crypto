package defdev.labdroid.crypto.flows.keystore

import android.content.Context
import defdev.labdroid.crypto.crypto.AesGcmCipher
import defdev.labdroid.crypto.crypto.CryptoService
import defdev.labdroid.crypto.crypto.KeySecurity
import defdev.labdroid.crypto.error.CryptoFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

class KeystoreFlow(context: Context, scope: CoroutineScope) : CryptoService {
    private val keyStore = KeystoreKey(context)
    private val startup = scope.async {
        try {
            keyStore.initialize()
            Result.success(Unit)
        } catch (failure: CryptoFailure) {
            Result.failure(failure)
        }
    }

    override suspend fun keySecurity(): KeySecurity {
        startup.await().getOrThrow()
        return keyStore.security()
    }

    override suspend fun encrypt(plaintext: String): String {
        startup.await().getOrThrow()
        return AesGcmCipher.encrypt(plaintext, keyStore.loadExisting())
    }

    override suspend fun decrypt(ciphertext: String): String {
        startup.await().getOrThrow()
        return AesGcmCipher.decrypt(ciphertext, keyStore.loadExisting())
    }
}

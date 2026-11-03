package defdev.labdroid.crypto.flows.biometric

import android.content.Context
import defdev.labdroid.crypto.crypto.AesGcmCipher
import defdev.labdroid.crypto.crypto.CryptoService
import defdev.labdroid.crypto.crypto.KeySecurity
import defdev.labdroid.crypto.error.CryptoFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

class BiometricFlow(
    context: Context,
    scope: CoroutineScope,
    private val authorizer: BiometricAuthorizer,
) : CryptoService {
    private val keyStore = BiometricKey(context)
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
        val cipher = AesGcmCipher.prepareEncryption(keyStore.loadExisting())
        val authorized = authorizer.authenticate(cipher, BiometricAction.ENCRYPT)
        return AesGcmCipher.finishEncryption(plaintext, authorized)
    }

    override suspend fun decrypt(ciphertext: String): String {
        startup.await().getOrThrow()
        val operation = AesGcmCipher.prepareDecryption(ciphertext, keyStore.loadExisting())
        val authorized = authorizer.authenticate(operation.cipher, BiometricAction.DECRYPT)
        return AesGcmCipher.finishDecryption(operation, authorized)
    }
}

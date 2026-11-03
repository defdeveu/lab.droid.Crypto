package defdev.labdroid.crypto.flows.biometric

import defdev.labdroid.crypto.error.CryptoError
import defdev.labdroid.crypto.error.CryptoFailure
import java.util.concurrent.atomic.AtomicBoolean
import javax.crypto.Cipher
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class BiometricAction {
    ENCRYPT,
    DECRYPT,
}

class BiometricAuthorizer {
    class Request internal constructor(val cipher: Cipher, val action: BiometricAction) {
        internal val result = CompletableDeferred<Cipher>()
        private val launched = AtomicBoolean(false)

        fun startOnce(): Boolean = launched.compareAndSet(false, true)
    }

    private val pending = MutableStateFlow<Request?>(null)
    val requests = pending.asStateFlow()

    suspend fun authenticate(cipher: Cipher, action: BiometricAction): Cipher {
        val request = Request(cipher, action)
        if (!pending.compareAndSet(null, request)) {
            throw CryptoFailure(CryptoError.AUTHENTICATION_FAILED)
        }
        return try {
            request.result.await()
        } finally {
            pending.compareAndSet(request, null)
            request.result.cancel()
        }
    }

    fun succeed(request: Request, cipher: Cipher?) {
        if (pending.value !== request) return
        if (cipher === request.cipher) {
            request.result.complete(request.cipher)
        } else {
            request.result.completeExceptionally(CryptoFailure(CryptoError.AUTHENTICATION_FAILED))
        }
    }

    fun fail(request: Request, reason: CryptoError) {
        if (pending.value === request) {
            request.result.completeExceptionally(CryptoFailure(reason))
        }
    }
}

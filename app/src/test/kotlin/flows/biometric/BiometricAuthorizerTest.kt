package defdev.labdroid.crypto.flows.biometric

import defdev.labdroid.crypto.error.CryptoError
import defdev.labdroid.crypto.error.CryptoFailure
import javax.crypto.Cipher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BiometricAuthorizerTest {
    private fun cipher(): Cipher = Cipher.getInstance("AES/GCM/NoPadding")

    @Test
    fun requestWaitsForItsResult() = runTest {
        val authorizer = BiometricAuthorizer()
        val cipher = cipher()
        val result = async { authorizer.authenticate(cipher, BiometricAction.ENCRYPT) }
        runCurrent()
        assertFalse(result.isCompleted)
        val request = requireNotNull(authorizer.requests.value)
        assertEquals(BiometricAction.ENCRYPT, request.action)

        authorizer.succeed(request, cipher)

        assertSame(cipher, result.await())
        assertNull(authorizer.requests.value)
    }

    @Test
    fun promptStartsOnceForEachRequest() = runTest {
        val authorizer = BiometricAuthorizer()
        val cipher = cipher()
        val result = async { authorizer.authenticate(cipher, BiometricAction.DECRYPT) }
        runCurrent()
        val request = requireNotNull(authorizer.requests.value)
        assertTrue(request.startOnce())
        assertFalse(request.startOnce())
        authorizer.succeed(request, cipher)
        result.await()
    }

    @Test
    fun cancellationReturnsATypedOutcome() = runTest {
        val authorizer = BiometricAuthorizer()
        val result = async {
            failure { authorizer.authenticate(cipher(), BiometricAction.ENCRYPT) }
        }
        runCurrent()
        authorizer.fail(requireNotNull(authorizer.requests.value), CryptoError.AUTHENTICATION_CANCELLED)

        assertEquals(CryptoError.AUTHENTICATION_CANCELLED, result.await())
        assertNull(authorizer.requests.value)
    }

    @Test
    fun resultMustBelongToThePendingRequest() = runTest {
        val authorizer = BiometricAuthorizer()
        val result = async {
            failure { authorizer.authenticate(cipher(), BiometricAction.ENCRYPT) }
        }
        runCurrent()
        authorizer.succeed(requireNotNull(authorizer.requests.value), cipher())

        assertEquals(CryptoError.AUTHENTICATION_FAILED, result.await())
    }

    @Test
    fun cancelledRequestCannotCompleteTheNextRequest() = runTest {
        val authorizer = BiometricAuthorizer()
        val first = async { authorizer.authenticate(cipher(), BiometricAction.ENCRYPT) }
        runCurrent()
        val oldRequest = requireNotNull(authorizer.requests.value)
        first.cancel()
        first.join()
        assertNull(authorizer.requests.value)

        val nextCipher = cipher()
        val second = async { authorizer.authenticate(nextCipher, BiometricAction.DECRYPT) }
        runCurrent()
        authorizer.fail(oldRequest, CryptoError.AUTHENTICATION_CANCELLED)
        assertFalse(second.isCompleted)
        authorizer.succeed(requireNotNull(authorizer.requests.value), nextCipher)
        assertSame(nextCipher, second.await())
    }

    private suspend fun failure(operation: suspend () -> Unit): CryptoError? =
        try {
            operation()
            null
        } catch (failure: CryptoFailure) {
            failure.reason
        }
}

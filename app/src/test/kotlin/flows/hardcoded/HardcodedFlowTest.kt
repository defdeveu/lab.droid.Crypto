package defdev.labdroid.crypto.flows.hardcoded

import defdev.labdroid.crypto.error.CryptoError
import defdev.labdroid.crypto.error.CryptoFailure
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HardcodedFlowTest {
    private val service = HardcodedFlow()

    @Test
    fun encryptMatchesKnownValue() = runTest {
        assertEquals("5f63e0723c198f7ddc73aff3d023bdb0", service.encrypt("Demo message"))
    }

    @Test
    fun decryptReversesEncrypt() = runTest {
        val plaintext = "development security"
        assertEquals(plaintext, service.decrypt(service.encrypt(plaintext)))
    }

    @Test
    fun encryptReturnsLowercaseHexadecimal() = runTest {
        val ciphertext = service.encrypt("a")
        assertTrue(ciphertext.all { it in "0123456789abcdef" })
    }

    @Test
    fun decryptRejectsNonHexadecimalInput() = runTest {
        val failure = try {
            service.decrypt("not-hex")
            null
        } catch (caught: CryptoFailure) {
            caught
        }
        assertEquals(CryptoError.INVALID_HEX, failure?.reason)
    }

    @Test
    fun decryptRejectsMalformedCiphertext() = runTest {
        val failure = try {
            service.decrypt("00112233445566778899aabbccddee")
            null
        } catch (caught: CryptoFailure) {
            caught
        }
        assertEquals(CryptoError.OPERATION_FAILED, failure?.reason)
    }
}

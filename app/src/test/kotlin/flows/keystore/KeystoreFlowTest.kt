package defdev.labdroid.crypto.flows.keystore

import defdev.labdroid.crypto.crypto.SecretKeySource

import defdev.labdroid.crypto.error.CryptoError
import defdev.labdroid.crypto.error.CryptoFailure
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeystoreFlowTest {
    private val service = KeystoreFlow(FixedKeySource())

    @Test
    fun decryptReversesEncrypt() {
        val plaintext = "development security"
        assertEquals(plaintext, service.decrypt(service.encrypt(plaintext)))
    }

    @Test
    fun encryptReturnsLowercaseHexadecimal() {
        val ciphertext = service.encrypt("a")
        assertTrue(ciphertext.all { it in "0123456789abcdef" })
    }

    @Test
    fun decryptRejectsNonHexadecimalInput() {
        val failure = try {
            service.decrypt("not-hex")
            null
        } catch (caught: CryptoFailure) {
            caught
        }
        assertEquals(CryptoError.INVALID_HEX, failure?.reason)
    }

    @Test
    fun decryptRejectsMalformedCiphertext() {
        val failure = try {
            service.decrypt("00112233")
            null
        } catch (caught: CryptoFailure) {
            caught
        }
        assertEquals(CryptoError.OPERATION_FAILED, failure?.reason)
    }

    private class FixedKeySource : SecretKeySource {
        override fun key(): SecretKey = SecretKeySpec(ByteArray(32) { it.toByte() }, "AES")
    }
}

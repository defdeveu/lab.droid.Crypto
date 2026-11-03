package defdev.labdroid.crypto.crypto

import defdev.labdroid.crypto.error.CryptoError
import defdev.labdroid.crypto.error.CryptoFailure
import javax.crypto.spec.SecretKeySpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AesGcmCipherTest {
    private val key = SecretKeySpec(ByteArray(32), "AES")

    @Test
    fun decryptReversesEncrypt() {
        val message = "Message — üdvözlet 🔑"
        assertEquals(message, AesGcmCipher.decrypt(AesGcmCipher.encrypt(message, key), key))
    }

    @Test
    fun encryptReturnsHexadecimalWithTheExpectedLength() {
        val message = "Demo message"
        val ciphertext = AesGcmCipher.encrypt(message, key)
        assertTrue(ciphertext.all { it in "0123456789abcdef" })
        assertEquals((12 + message.toByteArray(Charsets.UTF_8).size + 16) * 2, ciphertext.length)
    }

    @Test
    fun decryptRejectsNonHexadecimalInput() {
        assertEquals(CryptoError.INVALID_HEX, failureFor("not-hex"))
    }

    @Test
    fun decryptRejectsTruncatedInput() {
        assertEquals(CryptoError.OPERATION_FAILED, failureFor("00".repeat(27)))
    }

    private fun failureFor(ciphertext: String): CryptoError? =
        try {
            AesGcmCipher.decrypt(ciphertext, key)
            null
        } catch (failure: CryptoFailure) {
            failure.reason
        }
}

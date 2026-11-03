package defdev.labdroid.crypto.flows.hardcoded

import defdev.labdroid.crypto.crypto.CryptoService
import defdev.labdroid.crypto.crypto.HexCodec

import defdev.labdroid.crypto.error.CryptoError
import defdev.labdroid.crypto.error.CryptoFailure
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class HardcodedFlow : CryptoService {
    override fun encrypt(plaintext: String): String =
        HexCodec.encode(transform(Cipher.ENCRYPT_MODE, plaintext.toByteArray(Charsets.UTF_8)))

    override fun decrypt(ciphertext: String): String {
        val payload = HexCodec.decode(ciphertext) ?: throw CryptoFailure(CryptoError.INVALID_HEX)
        return String(transform(Cipher.DECRYPT_MODE, payload), Charsets.UTF_8)
    }

    private fun transform(mode: Int, input: ByteArray): ByteArray =
        try {
            val key = SecretKeySpec(KEY.toByteArray(Charsets.UTF_8), ALGORITHM)
            Cipher.getInstance(TRANSFORMATION).apply { init(mode, key) }.doFinal(input)
        } catch (failure: GeneralSecurityException) {
            throw CryptoFailure(CryptoError.OPERATION_FAILED)
        } catch (failure: IllegalArgumentException) {
            throw CryptoFailure(CryptoError.OPERATION_FAILED)
        }

    companion object {
        const val KEY = "Bar12345Bar12345"
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/ECB/PKCS5PADDING"
    }
}

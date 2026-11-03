package defdev.labdroid.crypto.crypto

import defdev.labdroid.crypto.error.CryptoError
import defdev.labdroid.crypto.error.CryptoFailure
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.SecretKey

object AesEcbCipher {
    fun encrypt(plaintext: String, key: SecretKey): String =
        HexCodec.encode(transform(Cipher.ENCRYPT_MODE, plaintext.toByteArray(Charsets.UTF_8), key))

    fun decrypt(ciphertext: String, key: SecretKey): String {
        val payload = HexCodec.decode(ciphertext) ?: throw CryptoFailure(CryptoError.INVALID_HEX)
        return String(transform(Cipher.DECRYPT_MODE, payload, key), Charsets.UTF_8)
    }

    private fun transform(mode: Int, input: ByteArray, key: SecretKey): ByteArray =
        try {
            Cipher.getInstance("AES/ECB/PKCS5PADDING").apply {
                init(mode, key)
            }.doFinal(input)
        } catch (_: GeneralSecurityException) {
            throw CryptoFailure(CryptoError.OPERATION_FAILED)
        } catch (_: IllegalArgumentException) {
            throw CryptoFailure(CryptoError.OPERATION_FAILED)
        }
}

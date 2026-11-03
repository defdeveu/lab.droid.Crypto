package defdev.labdroid.crypto.crypto

import defdev.labdroid.crypto.error.CryptoError
import defdev.labdroid.crypto.error.CryptoFailure
import java.security.GeneralSecurityException
import java.security.InvalidKeyException
import java.security.ProviderException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object AesGcmCipher {
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128

    class Decryption internal constructor(val cipher: Cipher, internal val encrypted: ByteArray)

    fun encrypt(plaintext: String, key: SecretKey): String =
        finishEncryption(plaintext, prepareEncryption(key))

    fun prepareEncryption(key: SecretKey): Cipher = protect {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        cipher
    }

    fun finishEncryption(plaintext: String, cipher: Cipher): String = protect {
        val iv = cipher.iv
        if (iv.size != IV_BYTES) {
            throw CryptoFailure(CryptoError.OPERATION_FAILED)
        }
        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        HexCodec.encode(iv + encrypted)
    }

    fun decrypt(ciphertext: String, key: SecretKey): String {
        val operation = prepareDecryption(ciphertext, key)
        return finishDecryption(operation, operation.cipher)
    }

    fun prepareDecryption(ciphertext: String, key: SecretKey): Decryption {
        val payload = HexCodec.decode(ciphertext) ?: throw CryptoFailure(CryptoError.INVALID_HEX)
        if (payload.size < IV_BYTES + TAG_BITS / 8) {
            throw CryptoFailure(CryptoError.OPERATION_FAILED)
        }
        return protect {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, payload.copyOfRange(0, IV_BYTES)))
            Decryption(cipher, payload.copyOfRange(IV_BYTES, payload.size))
        }
    }

    fun finishDecryption(operation: Decryption, cipher: Cipher): String = protect {
        cipher.doFinal(operation.encrypted).toString(Charsets.UTF_8)
    }

    private inline fun <T> protect(operation: () -> T): T =
        try {
            operation()
        } catch (_: InvalidKeyException) {
            throw CryptoFailure(CryptoError.KEY_UNAVAILABLE)
        } catch (_: ProviderException) {
            throw CryptoFailure(CryptoError.KEY_UNAVAILABLE)
        } catch (_: GeneralSecurityException) {
            throw CryptoFailure(CryptoError.OPERATION_FAILED)
        } catch (_: IllegalArgumentException) {
            throw CryptoFailure(CryptoError.OPERATION_FAILED)
        }
}

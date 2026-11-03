package defdev.labdroid.crypto.flows.keystore

import defdev.labdroid.crypto.crypto.CryptoService
import defdev.labdroid.crypto.crypto.HexCodec
import defdev.labdroid.crypto.crypto.SecretKeySource

import defdev.labdroid.crypto.error.CryptoError
import defdev.labdroid.crypto.error.CryptoFailure
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

class KeystoreFlow(private val keySource: SecretKeySource) : CryptoService {
    override fun encrypt(plaintext: String): String =
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, keySource.key())
            HexCodec.encode(cipher.iv + cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8)))
        } catch (failure: GeneralSecurityException) {
            throw CryptoFailure(CryptoError.OPERATION_FAILED)
        }

    override fun decrypt(ciphertext: String): String {
        val payload = HexCodec.decode(ciphertext) ?: throw CryptoFailure(CryptoError.INVALID_HEX)
        if (payload.size <= IV_SIZE_BYTES) {
            throw CryptoFailure(CryptoError.OPERATION_FAILED)
        }
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                keySource.key(),
                GCMParameterSpec(TAG_BITS, payload, 0, IV_SIZE_BYTES),
            )
            String(
                cipher.doFinal(payload, IV_SIZE_BYTES, payload.size - IV_SIZE_BYTES),
                Charsets.UTF_8,
            )
        } catch (failure: GeneralSecurityException) {
            throw CryptoFailure(CryptoError.OPERATION_FAILED)
        }
    }

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_SIZE_BYTES = 12
        private const val TAG_BITS = 128
    }
}

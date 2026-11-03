package defdev.labdroid.crypto.flows.hardcoded

import defdev.labdroid.crypto.crypto.AesEcbCipher
import defdev.labdroid.crypto.crypto.CryptoService
import javax.crypto.spec.SecretKeySpec

class HardcodedFlow : CryptoService {
    private val key = SecretKeySpec(KEY.toByteArray(Charsets.UTF_8), "AES")

    override val keyValue: String
        get() = KEY

    override suspend fun encrypt(plaintext: String): String =
        AesEcbCipher.encrypt(plaintext, key)

    override suspend fun decrypt(ciphertext: String): String =
        AesEcbCipher.decrypt(ciphertext, key)

    companion object {
        const val KEY = "0123456789abcdef0123456789abcde1"
    }
}

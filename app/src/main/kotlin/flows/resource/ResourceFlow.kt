package defdev.labs.droid.crypto.flows.resource

import android.content.res.Resources
import defdev.labs.droid.crypto.R
import defdev.labs.droid.crypto.crypto.AesEcbCipher
import defdev.labs.droid.crypto.crypto.CryptoService
import defdev.labs.droid.crypto.error.CryptoError
import defdev.labs.droid.crypto.error.CryptoFailure
import java.io.IOException
import javax.crypto.spec.SecretKeySpec

class ResourceFlow(private val resources: Resources) : CryptoService {
    private val keyBytes: ByteArray by lazy { readKey() }
    private val key by lazy { SecretKeySpec(keyBytes, "AES") }

    override val keyValue: String
        get() = keyBytes.toString(Charsets.UTF_8)

    override suspend fun encrypt(plaintext: String): String =
        AesEcbCipher.encrypt(plaintext, key)

    override suspend fun decrypt(ciphertext: String): String =
        AesEcbCipher.decrypt(ciphertext, key)

    private fun readKey(): ByteArray =
        try {
            resources.openRawResource(R.raw.resource_key).use { it.readBytes() }.also {
                if (it.size != 32) {
                    throw CryptoFailure(CryptoError.KEY_UNAVAILABLE)
                }
            }
        } catch (_: Resources.NotFoundException) {
            throw CryptoFailure(CryptoError.KEY_UNAVAILABLE)
        } catch (_: IOException) {
            throw CryptoFailure(CryptoError.KEY_UNAVAILABLE)
        }
}

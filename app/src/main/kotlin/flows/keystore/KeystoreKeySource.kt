package defdev.labdroid.crypto.flows.keystore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import defdev.labdroid.crypto.error.CryptoError
import defdev.labdroid.crypto.error.CryptoFailure
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import defdev.labdroid.crypto.crypto.SecretKeySource

class KeystoreKeySource(private val alias: String = DEFAULT_ALIAS) : SecretKeySource {
    override fun key(): SecretKey =
        try {
            val keyStore = KeyStore.getInstance(PROVIDER).apply { load(null) }
            val existing = keyStore.getKey(alias, null)
            if (existing is SecretKey) {
                existing
            } else {
                generate()
            }
        } catch (failure: GeneralSecurityException) {
            throw CryptoFailure(CryptoError.KEY_UNAVAILABLE)
        } catch (failure: IOException) {
            throw CryptoFailure(CryptoError.KEY_UNAVAILABLE)
        }

    private fun generate(): SecretKey {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
        generator.init(
            KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE_BITS)
                .build()
        )
        return generator.generateKey()
    }

    companion object {
        private const val PROVIDER = "AndroidKeyStore"
        private const val DEFAULT_ALIAS = "lab.droid.crypto.aes"
        private const val KEY_SIZE_BITS = 256
    }
}

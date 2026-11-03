package defdev.labdroid.crypto.flows.keystore

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import defdev.labdroid.crypto.crypto.KeySecurity
import defdev.labdroid.crypto.error.CryptoError
import defdev.labdroid.crypto.error.CryptoFailure
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.ProviderException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

class KeystoreKey(private val context: Context) {
    private val preferences = context.getSharedPreferences("keystore_provisioning", Context.MODE_PRIVATE)

    fun initialize(): Unit = protect {
        synchronized(lock) {
            val store = openStore()
            if (store.containsAlias(ALIAS)) {
                validate(readKey(store))
            } else {
                if (preferences.getBoolean(PROVISIONED, false)) {
                    throw CryptoFailure(CryptoError.KEY_UNAVAILABLE)
                }
                validate(createKey(store))
            }
            if (!preferences.edit().putBoolean(PROVISIONED, true).commit()) {
                throw CryptoFailure(CryptoError.KEY_UNAVAILABLE)
            }
        }
    }

    fun loadExisting(): SecretKey = protect {
        synchronized(lock) {
            if (!preferences.getBoolean(PROVISIONED, false)) {
                throw CryptoFailure(CryptoError.KEY_UNAVAILABLE)
            }
            readKey(openStore()).also { validate(it) }
        }
    }

    @Suppress("DEPRECATION")
    fun security(): KeySecurity = protect {
        val info = keyInfo(loadExisting())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            when (info.securityLevel) {
                KeyProperties.SECURITY_LEVEL_SOFTWARE -> KeySecurity.SOFTWARE
                KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> KeySecurity.TEE
                KeyProperties.SECURITY_LEVEL_STRONGBOX -> KeySecurity.STRONGBOX
                KeyProperties.SECURITY_LEVEL_UNKNOWN_SECURE -> KeySecurity.HARDWARE
                else -> KeySecurity.UNKNOWN
            }
        } else if (info.isInsideSecureHardware) {
            KeySecurity.HARDWARE
        } else {
            KeySecurity.SOFTWARE
        }
    }

    private fun openStore(): KeyStore = KeyStore.getInstance(PROVIDER).apply { load(null) }

    private fun readKey(store: KeyStore): SecretKey =
        store.getKey(ALIAS, null) as? SecretKey ?: throw CryptoFailure(CryptoError.KEY_UNAVAILABLE)

    private fun keyInfo(key: SecretKey): KeyInfo =
        SecretKeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
            .getKeySpec(key, KeyInfo::class.java) as KeyInfo

    private fun validate(key: SecretKey) {
        val info = keyInfo(key)
        if (key.algorithm != KeyProperties.KEY_ALGORITHM_AES || key.encoded != null ||
            info.keySize != 256 || info.origin != KeyProperties.ORIGIN_GENERATED ||
            info.purposes != PURPOSES || info.isUserAuthenticationRequired ||
            info.blockModes.toSet() != setOf(KeyProperties.BLOCK_MODE_GCM) ||
            info.encryptionPaddings.toSet() != setOf(KeyProperties.ENCRYPTION_PADDING_NONE)
        ) {
            throw CryptoFailure(CryptoError.KEY_UNAVAILABLE)
        }
    }

    private fun createKey(store: KeyStore): SecretKey {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
        ) {
            try {
                return generate(strongBox = true)
            } catch (_: StrongBoxUnavailableException) {
                if (store.containsAlias(ALIAS)) {
                    throw CryptoFailure(CryptoError.KEY_UNAVAILABLE)
                }
            }
        }
        return generate(strongBox = false)
    }

    private fun generate(strongBox: Boolean): SecretKey {
        val builder = KeyGenParameterSpec.Builder(ALIAS, PURPOSES)
            .setKeySize(256)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .setUserAuthenticationRequired(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.setIsStrongBoxBacked(strongBox)
        }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER).apply {
            init(builder.build())
        }.generateKey()
    }

    private inline fun <T> protect(operation: () -> T): T =
        try {
            operation()
        } catch (_: GeneralSecurityException) {
            throw CryptoFailure(CryptoError.KEY_UNAVAILABLE)
        } catch (_: IOException) {
            throw CryptoFailure(CryptoError.KEY_UNAVAILABLE)
        } catch (_: ProviderException) {
            throw CryptoFailure(CryptoError.KEY_UNAVAILABLE)
        } catch (_: SecurityException) {
            throw CryptoFailure(CryptoError.KEY_UNAVAILABLE)
        } catch (_: ClassCastException) {
            throw CryptoFailure(CryptoError.KEY_UNAVAILABLE)
        }

    companion object {
        private const val PROVIDER = "AndroidKeyStore"
        private const val ALIAS = "defdev.labdroid.crypto.keystore.gcm.v1"
        private const val PROVISIONED = "flow4.provisioned"
        private const val PURPOSES = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        private val lock = Any()
    }
}

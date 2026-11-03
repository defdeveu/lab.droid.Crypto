package defdev.labdroid.crypto.flows.biometric

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import androidx.biometric.BiometricManager
import defdev.labdroid.crypto.crypto.KeySecurity
import defdev.labdroid.crypto.crypto.KeystoreMetadata
import defdev.labdroid.crypto.error.CryptoError
import defdev.labdroid.crypto.error.CryptoFailure
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.ProviderException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class BiometricKey(private val context: Context) {
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
                requireBiometrics()
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
            readKey(openStore()).also {
                validate(it)
                requireBiometrics()
            }
        }
    }

    fun security(): KeySecurity = protect { KeystoreMetadata.security(loadExisting()) }

    private fun requireBiometrics() {
        when (BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Unit
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> throw CryptoFailure(CryptoError.BIOMETRIC_NOT_ENROLLED)
            else -> throw CryptoFailure(CryptoError.BIOMETRIC_UNAVAILABLE)
        }
    }

    private fun openStore(): KeyStore = KeyStore.getInstance(PROVIDER).apply { load(null) }

    private fun readKey(store: KeyStore): SecretKey =
        store.getKey(ALIAS, null) as? SecretKey ?: throw CryptoFailure(CryptoError.KEY_UNAVAILABLE)

    private fun validate(key: SecretKey) {
        val info = KeystoreMetadata.info(key)
        if (key.algorithm != KeyProperties.KEY_ALGORITHM_AES || key.encoded != null ||
            info.keySize != 256 || info.origin != KeyProperties.ORIGIN_GENERATED ||
            info.purposes != PURPOSES || !info.isUserAuthenticationRequired ||
            info.userAuthenticationValidityDurationSeconds > 0 || !info.isInvalidatedByBiometricEnrollment ||
            info.blockModes.toSet() != setOf(KeyProperties.BLOCK_MODE_GCM) ||
            info.encryptionPaddings.toSet() != setOf(KeyProperties.ENCRYPTION_PADDING_NONE) ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                info.userAuthenticationType != KeyProperties.AUTH_BIOMETRIC_STRONG)
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

    @Suppress("DEPRECATION")
    private fun generate(strongBox: Boolean): SecretKey {
        val builder = KeyGenParameterSpec.Builder(ALIAS, PURPOSES)
            .setKeySize(256)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
        } else {
            builder.setUserAuthenticationValidityDurationSeconds(-1)
        }
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
        private const val ALIAS = "defdev.labdroid.crypto.biometric.gcm.v1"
        private const val PROVISIONED = "flow5.provisioned"
        private const val PURPOSES = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        private val lock = Any()
    }
}

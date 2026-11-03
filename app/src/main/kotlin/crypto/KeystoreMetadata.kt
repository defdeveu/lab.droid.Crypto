package defdev.labdroid.crypto.crypto

import android.os.Build
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

object KeystoreMetadata {
    fun info(key: SecretKey): KeyInfo =
        SecretKeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            .getKeySpec(key, KeyInfo::class.java) as KeyInfo

    @Suppress("DEPRECATION")
    fun security(key: SecretKey): KeySecurity {
        val info = info(key)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
}

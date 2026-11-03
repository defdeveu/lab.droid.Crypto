package defdev.labdroid.crypto.crypto

import javax.crypto.SecretKey

interface SecretKeySource {
    fun key(): SecretKey
}

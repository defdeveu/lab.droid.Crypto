package defdev.labdroid.crypto.crypto

interface CryptoService {
    val keyValue: String?
        get() = null

    suspend fun keySecurity(): KeySecurity? = null

    suspend fun encrypt(plaintext: String): String

    suspend fun decrypt(ciphertext: String): String
}

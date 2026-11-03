package defdev.labdroid.crypto.crypto

interface CryptoService {
    val keyValue: String?
        get() = null

    fun encrypt(plaintext: String): String

    fun decrypt(ciphertext: String): String
}

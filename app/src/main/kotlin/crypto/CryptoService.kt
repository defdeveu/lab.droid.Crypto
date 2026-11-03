package defdev.labdroid.crypto.crypto

interface CryptoService {
    fun encrypt(plaintext: String): String

    fun decrypt(ciphertext: String): String
}

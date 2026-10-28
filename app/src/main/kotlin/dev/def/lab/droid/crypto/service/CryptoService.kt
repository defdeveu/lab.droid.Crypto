package dev.def.lab.droid.crypto.service

interface CryptoService {
    fun encrypt(plaintext: String): String

    fun decrypt(ciphertext: String): String
}

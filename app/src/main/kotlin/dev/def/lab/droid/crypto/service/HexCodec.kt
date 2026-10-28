package dev.def.lab.droid.crypto.service

object HexCodec {
    private const val DIGITS = "0123456789abcdef"

    fun encode(bytes: ByteArray): String {
        val text = StringBuilder(bytes.size * 2)
        for (byte in bytes) {
            val value = byte.toInt() and 0xFF
            text.append(DIGITS[value ushr 4])
            text.append(DIGITS[value and 0x0F])
        }
        return text.toString()
    }

    fun decode(text: String): ByteArray? {
        if (text.length % 2 != 0) {
            return null
        }
        val bytes = ByteArray(text.length / 2)
        for (index in bytes.indices) {
            val high = Character.digit(text[index * 2], 16)
            val low = Character.digit(text[index * 2 + 1], 16)
            if (high < 0 || low < 0) {
                return null
            }
            bytes[index] = ((high shl 4) or low).toByte()
        }
        return bytes
    }
}

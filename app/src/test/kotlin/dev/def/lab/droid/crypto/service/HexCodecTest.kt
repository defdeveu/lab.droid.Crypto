package dev.def.lab.droid.crypto.service

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HexCodecTest {
    @Test
    fun encodeRendersLowercaseHexadecimal() {
        assertEquals("00ff10", HexCodec.encode(byteArrayOf(0x00, 0xFF.toByte(), 0x10)))
    }

    @Test
    fun decodeReversesEncode() {
        val bytes = byteArrayOf(0x00, 0x7F, 0x80.toByte(), 0xFF.toByte())
        assertArrayEquals(bytes, HexCodec.decode(HexCodec.encode(bytes)))
    }

    @Test
    fun decodeRejectsOddLengthInput() {
        assertNull(HexCodec.decode("abc"))
    }

    @Test
    fun decodeRejectsNonHexadecimalCharacters() {
        assertNull(HexCodec.decode("zz"))
    }
}

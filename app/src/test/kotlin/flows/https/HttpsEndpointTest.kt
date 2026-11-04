package defdev.labdroid.crypto.flows.https

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HttpsEndpointTest {
    @Test
    fun acceptsAnHttpsEndpoint() {
        assertEquals("https://example.test/crypto/key", HttpsFlow.parseEndpoint("https://example.test/crypto/key").toString())
    }

    @Test
    fun rejectsOtherSchemesAndMissingEndpoints() {
        for (value in listOf("", "not a URL", "https://", "http://example.test/key", "ftp://example.test/key")) {
            assertNull(value, HttpsFlow.parseEndpoint(value))
        }
    }
}

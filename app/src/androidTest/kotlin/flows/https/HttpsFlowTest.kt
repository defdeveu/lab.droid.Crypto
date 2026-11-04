package defdev.labdroid.crypto.flows.https

import defdev.labdroid.crypto.BuildConfig
import defdev.labdroid.crypto.error.CryptoError
import defdev.labdroid.crypto.error.CryptoFailure
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class HttpsFlowTest {
    @Test
    fun operationsRequestTheirKeySeparately() = runBlocking {
        val replies = Replies()
        val flow = HttpsFlow(ENDPOINT.toHttpUrl(), replies.client())
        assertNull(flow.keyValue)
        assertEquals(0, replies.requests.get())

        val ciphertext = flow.encrypt("Demo message")
        assertEquals(1, replies.requests.get())
        assertEquals(KEY, flow.keyValue)
        assertEquals("Demo message", flow.decrypt(ciphertext))
        assertEquals(2, replies.requests.get())
    }

    @Test
    fun unsuccessfulResponsesClearTheKey() = runBlocking {
        val responses = listOf(
            Reply(status = 302),
            Reply(status = 503),
            Reply(mediaType = "text/plain"),
            Reply(body = "not JSON"),
            Reply(body = "{\"key\":123}"),
            Reply(body = "{\"key\":\"short\"}"),
            Reply(body = "{\"key\":\"${"é".repeat(32)}\"}"),
            Reply(body = "{\"key\":\"$KEY\"} trailing"),
            Reply(body = " ".repeat(1025)),
        )
        for (reply in responses) {
            val replies = Replies()
            val flow = HttpsFlow(ENDPOINT.toHttpUrl(), replies.client())
            val ciphertext = flow.encrypt("Demo message")
            assertEquals(KEY, flow.keyValue)
            replies.reply = reply
            expectDownloadFailure { flow.decrypt(ciphertext) }
            assertNull(flow.keyValue)
            assertEquals(2, replies.requests.get())
        }
    }

    @Test
    fun transportFailureIsReturnedWithoutAnotherAttempt() = runBlocking {
        val replies = Replies()
        val flow = HttpsFlow(ENDPOINT.toHttpUrl(), replies.client())
        flow.encrypt("Demo message")
        replies.failure = IOException("Connection failed")

        expectDownloadFailure { flow.encrypt("New message") }

        assertNull(flow.keyValue)
        assertEquals(2, replies.requests.get())
    }

    @Test
    fun cancellingAnOperationCancelsItsCall() = runBlocking {
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        val startedCall = AtomicReference<Call>()
        val client = OkHttpClient.Builder()
            .eventListener(object : EventListener() {
                override fun callStart(call: Call) {
                    startedCall.set(call)
                }
            })
            .addInterceptor { chain ->
                started.countDown()
                check(release.await(5, TimeUnit.SECONDS))
                Reply().response(chain)
            }
            .build()
        val flow = HttpsFlow(ENDPOINT.toHttpUrl(), client)
        withTimeout(10_000) {
            val operation = async { flow.encrypt("Demo message") }
            try {
                withContext(Dispatchers.IO) { assertTrue(started.await(5, TimeUnit.SECONDS)) }
                operation.cancelAndJoin()
                assertTrue(startedCall.get().isCanceled())
            } finally {
                release.countDown()
                operation.cancelAndJoin()
            }
        }
    }

    @Test
    fun configuredServerCompletesTwoRequestsAndARoundTrip() = runBlocking {
        val endpoint = requireNotNull(HttpsFlow.parseEndpoint(BuildConfig.LAB_ENDPOINT))
        val responses = AtomicInteger()
        val client = OkHttpClient.Builder()
            .eventListener(object : EventListener() {
                override fun responseHeadersEnd(call: Call, response: Response) {
                    if (response.request.url == endpoint && response.code == 200) {
                        responses.incrementAndGet()
                    }
                }
            })
            .build()
        val flow = HttpsFlow(endpoint, client)

        val ciphertext = flow.encrypt("Demo message")
        assertEquals("Demo message", flow.decrypt(ciphertext))
        assertEquals(2, responses.get())
        assertEquals(32, flow.keyValue?.length)
    }

    private suspend fun expectDownloadFailure(operation: suspend () -> String) {
        try {
            operation()
            fail("Expected a download failure")
        } catch (failure: CryptoFailure) {
            assertEquals(CryptoError.KEY_DOWNLOAD_FAILED, failure.reason)
        }
    }

    private data class Reply(
        val status: Int = 200,
        val mediaType: String = "application/json; charset=utf-8",
        val body: String = "{\"key\":\"$KEY\"}",
    ) {
        fun response(chain: Interceptor.Chain): Response = Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(status)
            .message("Response")
            .body(body.toResponseBody(mediaType.toMediaType()))
            .build()
    }

    private class Replies : Interceptor {
        @Volatile var reply = Reply()
        @Volatile var failure: IOException? = null
        val requests = AtomicInteger()

        override fun intercept(chain: Interceptor.Chain): Response {
            requests.incrementAndGet()
            failure?.let { throw it }
            return reply.response(chain)
        }

        fun client(): OkHttpClient = OkHttpClient.Builder().addInterceptor(this).build()
    }

    private companion object {
        const val ENDPOINT = "https://example.test/key"
        const val KEY = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456"
    }
}

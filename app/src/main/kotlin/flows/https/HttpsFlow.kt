package defdev.labdroid.crypto.flows.https

import defdev.labdroid.crypto.crypto.AesEcbCipher
import defdev.labdroid.crypto.crypto.CryptoService
import defdev.labdroid.crypto.error.CryptoError
import defdev.labdroid.crypto.error.CryptoFailure
import java.io.IOException
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

class HttpsFlow(
    private val endpoint: HttpUrl,
    client: OkHttpClient = OkHttpClient(),
) : CryptoService {
    private val client = client.newBuilder()
        .followRedirects(false)
        .followSslRedirects(false)
        .retryOnConnectionFailure(false)
        .cache(null)
        .build()

    @Volatile
    private var downloadedKey: String? = null

    init {
        require(endpoint.isHttps)
    }

    override val keyValue: String?
        get() = downloadedKey

    override suspend fun encrypt(plaintext: String): String {
        val key = fetchKey()
        return AesEcbCipher.encrypt(plaintext, key)
    }

    override suspend fun decrypt(ciphertext: String): String {
        val key = fetchKey()
        return AesEcbCipher.decrypt(ciphertext, key)
    }

    private suspend fun fetchKey(): SecretKey {
        downloadedKey = null
        val request = Request.Builder().url(endpoint).header("Accept", "application/json").get().build()
        val value = client.newCall(request).awaitKey()
        downloadedKey = value
        return SecretKeySpec(value.toByteArray(Charsets.UTF_8), "AES")
    }

    private suspend fun Call.awaitKey(): String = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { cancel() }
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) {
                    continuation.resumeWithException(CryptoFailure(CryptoError.KEY_DOWNLOAD_FAILED))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val value = try {
                    readKey(response)
                } catch (failure: CryptoFailure) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(failure)
                    }
                    return
                }
                if (continuation.isActive) {
                    continuation.resume(value)
                }
            }
        })
    }

    private fun readKey(response: Response): String =
        try {
            response.use {
                val mediaType = it.body.contentType()
                if (it.code != 200 || mediaType == null || mediaType.type != "application" || mediaType.subtype != "json") {
                    throw CryptoFailure(CryptoError.KEY_DOWNLOAD_FAILED)
                }
                if (it.body.source().request(MAX_RESPONSE_BYTES + 1L)) {
                    throw CryptoFailure(CryptoError.KEY_DOWNLOAD_FAILED)
                }
                val tokens = JSONTokener(it.body.string())
                val document = tokens.nextValue() as? JSONObject
                    ?: throw CryptoFailure(CryptoError.KEY_DOWNLOAD_FAILED)
                val value = document.opt("key") as? String
                    ?: throw CryptoFailure(CryptoError.KEY_DOWNLOAD_FAILED)
                if (tokens.nextClean() != '\u0000' || value.length != 32 || value.any { character -> character.code > 127 }) {
                    throw CryptoFailure(CryptoError.KEY_DOWNLOAD_FAILED)
                }
                value
            }
        } catch (_: IOException) {
            throw CryptoFailure(CryptoError.KEY_DOWNLOAD_FAILED)
        } catch (_: JSONException) {
            throw CryptoFailure(CryptoError.KEY_DOWNLOAD_FAILED)
        }

    companion object {
        private const val MAX_RESPONSE_BYTES = 1024

        fun parseEndpoint(value: String): HttpUrl? = value.toHttpUrlOrNull()?.takeIf { it.isHttps }
    }
}

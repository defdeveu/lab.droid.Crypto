package dev.def.lab.droid.crypto

import dev.def.lab.droid.crypto.error.CryptoError
import dev.def.lab.droid.crypto.error.CryptoFailure
import dev.def.lab.droid.crypto.service.CryptoService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ComparisonViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun comparePublishesBothResults() = runTest(dispatcher) {
        val viewModel = ComparisonViewModel(
            StubCryptoService("fixed"),
            StubCryptoService("device"),
            dispatcher,
        )
        viewModel.onPlaintextChange("hello")

        viewModel.compare()
        advanceUntilIdle()

        assertEquals("fixed:hello", viewModel.uiState.value.fixedKeyCiphertext)
        assertEquals("device:hello", viewModel.uiState.value.deviceKeyCiphertext)
    }

    @Test
    fun compareWithoutAMessageReportsAnError() = runTest(dispatcher) {
        val viewModel = ComparisonViewModel(
            StubCryptoService("fixed"),
            StubCryptoService("device"),
            dispatcher,
        )

        viewModel.compare()
        advanceUntilIdle()

        assertEquals(CryptoError.EMPTY_INPUT, viewModel.uiState.value.error)
    }

    @Test
    fun aSecondCompareWhileRunningIsIgnored() = runTest(dispatcher) {
        val fixed = StubCryptoService("fixed")
        val device = StubCryptoService("device")
        val viewModel = ComparisonViewModel(fixed, device, dispatcher)
        viewModel.onPlaintextChange("hello")

        viewModel.compare()
        viewModel.compare()
        advanceUntilIdle()

        assertEquals(1, fixed.encryptCalls)
        assertEquals(1, device.encryptCalls)
    }

    @Test
    fun decryptingTheDeviceKeyCiphertextPublishesThePlaintext() = runTest(dispatcher) {
        val viewModel = ComparisonViewModel(
            StubCryptoService("fixed"),
            StubCryptoService("device"),
            dispatcher,
        )
        viewModel.onDeviceKeyCiphertextChange("abcd")

        viewModel.decryptDeviceKeyCiphertext()
        advanceUntilIdle()

        assertEquals("abcd", viewModel.uiState.value.deviceKeyPlaintext)
    }

    @Test
    fun aFailedDecryptClearsThePreviousPlaintext() = runTest(dispatcher) {
        val device = StubCryptoService("device")
        val viewModel = ComparisonViewModel(StubCryptoService("fixed"), device, dispatcher)
        viewModel.onDeviceKeyCiphertextChange("abcd")

        viewModel.decryptDeviceKeyCiphertext()
        advanceUntilIdle()
        assertEquals("abcd", viewModel.uiState.value.deviceKeyPlaintext)

        device.failure = CryptoError.OPERATION_FAILED
        viewModel.onDeviceKeyCiphertextChange("00")
        viewModel.decryptDeviceKeyCiphertext()
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.deviceKeyPlaintext)
        assertEquals(CryptoError.OPERATION_FAILED, viewModel.uiState.value.error)
    }

    @Test
    fun aServiceFailureIsReportedAsATypedError() = runTest(dispatcher) {
        val viewModel = ComparisonViewModel(
            StubCryptoService("fixed", CryptoError.KEY_UNAVAILABLE),
            StubCryptoService("device"),
            dispatcher,
        )
        viewModel.onPlaintextChange("hello")

        viewModel.compare()
        advanceUntilIdle()

        assertEquals(CryptoError.KEY_UNAVAILABLE, viewModel.uiState.value.error)
    }

    @Test
    fun anEmptyDecryptClearsThePreviousPlaintext() = runTest(dispatcher) {
        val viewModel = ComparisonViewModel(
            StubCryptoService("fixed"),
            StubCryptoService("device"),
            dispatcher,
        )
        viewModel.onDeviceKeyCiphertextChange("abcd")

        viewModel.decryptDeviceKeyCiphertext()
        advanceUntilIdle()
        assertEquals("abcd", viewModel.uiState.value.deviceKeyPlaintext)

        viewModel.onDeviceKeyCiphertextChange("")
        viewModel.decryptDeviceKeyCiphertext()
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.deviceKeyPlaintext)
        assertEquals(CryptoError.EMPTY_INPUT, viewModel.uiState.value.error)
    }

    @Test
    fun aFailedCompareClearsBothCiphertextsAndThePlaintext() = runTest(dispatcher) {
        val device = StubCryptoService("device")
        val viewModel = ComparisonViewModel(StubCryptoService("fixed"), device, dispatcher)
        viewModel.onPlaintextChange("hello")

        viewModel.compare()
        advanceUntilIdle()
        viewModel.decryptDeviceKeyCiphertext()
        advanceUntilIdle()
        assertEquals("device:hello", viewModel.uiState.value.deviceKeyPlaintext)

        device.failure = CryptoError.OPERATION_FAILED
        viewModel.compare()
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.fixedKeyCiphertext)
        assertEquals("", viewModel.uiState.value.deviceKeyCiphertext)
        assertEquals("", viewModel.uiState.value.deviceKeyPlaintext)
        assertEquals(CryptoError.OPERATION_FAILED, viewModel.uiState.value.error)
    }

    private class StubCryptoService(
        private val label: String,
        var failure: CryptoError? = null,
    ) : CryptoService {
        var encryptCalls = 0
            private set

        override fun encrypt(plaintext: String): String {
            encryptCalls++
            failure?.let { throw CryptoFailure(it) }
            return "$label:$plaintext"
        }

        override fun decrypt(ciphertext: String): String {
            failure?.let { throw CryptoFailure(it) }
            return ciphertext
        }
    }
}

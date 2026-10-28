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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CryptoViewModelTest {
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
    fun encryptPublishesTheCiphertext() = runTest(dispatcher) {
        val viewModel = CryptoViewModel(FakeCryptoService(), dispatcher)
        viewModel.onPlaintextChange("hello")

        viewModel.encrypt()
        advanceUntilIdle()

        assertEquals("enc:hello", viewModel.uiState.value.ciphertext)
        assertFalse(viewModel.uiState.value.isRunning)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun encryptWithoutAMessageReportsAnError() = runTest(dispatcher) {
        val viewModel = CryptoViewModel(FakeCryptoService(), dispatcher)

        viewModel.encrypt()
        advanceUntilIdle()

        assertEquals(CryptoError.EMPTY_INPUT, viewModel.uiState.value.error)
    }

    @Test
    fun aSecondEncryptWhileRunningIsIgnored() = runTest(dispatcher) {
        val service = FakeCryptoService()
        val viewModel = CryptoViewModel(service, dispatcher)
        viewModel.onPlaintextChange("hello")

        viewModel.encrypt()
        viewModel.encrypt()
        advanceUntilIdle()

        assertEquals(1, service.encryptCalls)
    }

    @Test
    fun decryptPublishesThePlaintext() = runTest(dispatcher) {
        val viewModel = CryptoViewModel(FakeCryptoService(), dispatcher)
        viewModel.onCiphertextChange("0011")

        viewModel.decrypt()
        advanceUntilIdle()

        assertEquals("dec:0011", viewModel.uiState.value.decrypted)
    }

    @Test
    fun aFailedDecryptClearsThePreviousPlaintext() = runTest(dispatcher) {
        val service = FakeCryptoService()
        val viewModel = CryptoViewModel(service, dispatcher)
        viewModel.onCiphertextChange("0011")

        viewModel.decrypt()
        advanceUntilIdle()
        assertEquals("dec:0011", viewModel.uiState.value.decrypted)

        service.failure = CryptoError.OPERATION_FAILED
        viewModel.onCiphertextChange("00")
        viewModel.decrypt()
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.decrypted)
        assertEquals(CryptoError.OPERATION_FAILED, viewModel.uiState.value.error)
    }

    @Test
    fun aServiceFailureIsReportedAsATypedError() = runTest(dispatcher) {
        val viewModel = CryptoViewModel(FakeCryptoService(CryptoError.OPERATION_FAILED), dispatcher)
        viewModel.onPlaintextChange("hello")

        viewModel.encrypt()
        advanceUntilIdle()

        assertEquals(CryptoError.OPERATION_FAILED, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isRunning)
    }

    @Test
    fun anEmptyDecryptClearsThePreviousPlaintext() = runTest(dispatcher) {
        val viewModel = CryptoViewModel(FakeCryptoService(), dispatcher)
        viewModel.onCiphertextChange("0011")

        viewModel.decrypt()
        advanceUntilIdle()
        assertEquals("dec:0011", viewModel.uiState.value.decrypted)

        viewModel.onCiphertextChange("")
        viewModel.decrypt()
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.decrypted)
        assertEquals(CryptoError.EMPTY_INPUT, viewModel.uiState.value.error)
    }

    private class FakeCryptoService(var failure: CryptoError? = null) : CryptoService {
        var encryptCalls = 0
            private set

        override fun encrypt(plaintext: String): String {
            encryptCalls++
            failure?.let { throw CryptoFailure(it) }
            return "enc:$plaintext"
        }

        override fun decrypt(ciphertext: String): String {
            failure?.let { throw CryptoFailure(it) }
            return "dec:$ciphertext"
        }
    }
}

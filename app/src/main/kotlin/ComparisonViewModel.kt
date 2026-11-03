package defdev.labdroid.crypto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import defdev.labdroid.crypto.error.CryptoError
import defdev.labdroid.crypto.error.CryptoFailure
import defdev.labdroid.crypto.crypto.CryptoService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ComparisonUiState(
    val plaintext: String = "",
    val fixedKeyCiphertext: String = "",
    val deviceKeyCiphertext: String = "",
    val deviceKeyPlaintext: String = "",
    val isRunning: Boolean = false,
    val error: CryptoError? = null,
)

class ComparisonViewModel(
    private val fixedKeyCrypto: CryptoService,
    private val deviceKeyCrypto: CryptoService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ComparisonUiState())

    val uiState: StateFlow<ComparisonUiState> = mutableUiState.asStateFlow()

    fun onPlaintextChange(value: String) {
        mutableUiState.update { it.copy(plaintext = value) }
    }

    fun onDeviceKeyCiphertextChange(value: String) {
        mutableUiState.update { it.copy(deviceKeyCiphertext = value) }
    }

    fun compare() {
        if (mutableUiState.value.isRunning) {
            return
        }
        val plaintext = mutableUiState.value.plaintext
        if (plaintext.isEmpty()) {
            mutableUiState.update {
                it.copy(
                    error = CryptoError.EMPTY_INPUT,
                    fixedKeyCiphertext = "",
                    deviceKeyCiphertext = "",
                    deviceKeyPlaintext = "",
                )
            }
            return
        }
        mutableUiState.update { it.copy(isRunning = true, error = null) }
        viewModelScope.launch(dispatcher) {
            try {
                val fixedKeyCiphertext = fixedKeyCrypto.encrypt(plaintext)
                val deviceKeyCiphertext = deviceKeyCrypto.encrypt(plaintext)
                mutableUiState.update {
                    it.copy(
                        isRunning = false,
                        fixedKeyCiphertext = fixedKeyCiphertext,
                        deviceKeyCiphertext = deviceKeyCiphertext,
                        deviceKeyPlaintext = "",
                    )
                }
            } catch (failure: CryptoFailure) {
                mutableUiState.update {
                    it.copy(
                        isRunning = false,
                        error = failure.reason,
                        fixedKeyCiphertext = "",
                        deviceKeyCiphertext = "",
                        deviceKeyPlaintext = "",
                    )
                }
            }
        }
    }

    fun decryptDeviceKeyCiphertext() {
        if (mutableUiState.value.isRunning) {
            return
        }
        val ciphertext = mutableUiState.value.deviceKeyCiphertext
        if (ciphertext.isEmpty()) {
            mutableUiState.update { it.copy(error = CryptoError.EMPTY_INPUT, deviceKeyPlaintext = "") }
            return
        }
        mutableUiState.update { it.copy(isRunning = true, error = null) }
        viewModelScope.launch(dispatcher) {
            try {
                val plaintext = deviceKeyCrypto.decrypt(ciphertext)
                mutableUiState.update { it.copy(isRunning = false, deviceKeyPlaintext = plaintext) }
            } catch (failure: CryptoFailure) {
                mutableUiState.update {
                    it.copy(isRunning = false, error = failure.reason, deviceKeyPlaintext = "")
                }
            }
        }
    }
}

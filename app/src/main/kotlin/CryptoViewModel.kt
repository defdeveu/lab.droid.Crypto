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

data class CryptoUiState(
    val plaintext: String = "",
    val ciphertext: String = "",
    val decrypted: String = "",
    val isRunning: Boolean = false,
    val error: CryptoError? = null,
)

class CryptoViewModel(
    private val crypto: CryptoService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(CryptoUiState())

    val uiState: StateFlow<CryptoUiState> = mutableUiState.asStateFlow()

    fun onPlaintextChange(value: String) {
        mutableUiState.update { it.copy(plaintext = value) }
    }

    fun onCiphertextChange(value: String) {
        mutableUiState.update { it.copy(ciphertext = value) }
    }

    fun encrypt() {
        if (mutableUiState.value.isRunning) {
            return
        }
        val plaintext = mutableUiState.value.plaintext
        if (plaintext.isEmpty()) {
            mutableUiState.update { it.copy(error = CryptoError.EMPTY_INPUT, ciphertext = "") }
            return
        }
        mutableUiState.update { it.copy(isRunning = true, error = null) }
        viewModelScope.launch(dispatcher) {
            try {
                val ciphertext = crypto.encrypt(plaintext)
                mutableUiState.update { it.copy(isRunning = false, ciphertext = ciphertext) }
            } catch (failure: CryptoFailure) {
                mutableUiState.update {
                    it.copy(isRunning = false, error = failure.reason, ciphertext = "")
                }
            }
        }
    }

    fun decrypt() {
        if (mutableUiState.value.isRunning) {
            return
        }
        val ciphertext = mutableUiState.value.ciphertext
        if (ciphertext.isEmpty()) {
            mutableUiState.update { it.copy(error = CryptoError.EMPTY_INPUT, decrypted = "") }
            return
        }
        mutableUiState.update { it.copy(isRunning = true, error = null) }
        viewModelScope.launch(dispatcher) {
            try {
                val plaintext = crypto.decrypt(ciphertext)
                mutableUiState.update { it.copy(isRunning = false, decrypted = plaintext) }
            } catch (failure: CryptoFailure) {
                mutableUiState.update {
                    it.copy(isRunning = false, error = failure.reason, decrypted = "")
                }
            }
        }
    }
}

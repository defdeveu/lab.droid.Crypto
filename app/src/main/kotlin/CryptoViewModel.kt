package defdev.labdroid.crypto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import defdev.labdroid.crypto.crypto.CryptoService
import defdev.labdroid.crypto.crypto.KeySecurity
import defdev.labdroid.crypto.error.CryptoError
import defdev.labdroid.crypto.error.CryptoFailure
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CryptoOperation {
    ENCRYPT,
    DECRYPT,
}

data class CryptoUiState(
    val plaintext: String = "",
    val ciphertext: String = "",
    val decrypted: String = "",
    val keyValue: String = "",
    val keySecurity: KeySecurity? = null,
    val isRunning: Boolean = false,
    val operation: CryptoOperation? = null,
    val error: CryptoError? = null,
)

class CryptoViewModel(
    private val crypto: CryptoService?,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(CryptoUiState(keyValue = readKeyValue()))

    val uiState: StateFlow<CryptoUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            try {
                val security = crypto?.keySecurity()
                mutableUiState.update { it.copy(keySecurity = security) }
            } catch (failure: CryptoFailure) {
                mutableUiState.update { it.copy(error = failure.reason) }
            }
        }
    }

    private fun readKeyValue(): String =
        try {
            crypto?.keyValue.orEmpty()
        } catch (_: CryptoFailure) {
            ""
        }

    fun onPlaintextChange(value: String) {
        mutableUiState.update { it.copy(plaintext = value) }
    }

    fun onCiphertextChange(value: String) {
        mutableUiState.update { it.copy(ciphertext = value, decrypted = "") }
    }

    fun encrypt() {
        if (mutableUiState.value.isRunning) {
            return
        }
        val service = crypto
        if (service == null) {
            mutableUiState.update { it.copy(operation = CryptoOperation.ENCRYPT, error = CryptoError.NOT_IMPLEMENTED) }
            return
        }
        val plaintext = mutableUiState.value.plaintext
        if (plaintext.isEmpty()) {
            mutableUiState.update {
                it.copy(operation = CryptoOperation.ENCRYPT, error = CryptoError.EMPTY_INPUT, ciphertext = "", decrypted = "")
            }
            return
        }
        mutableUiState.update {
            it.copy(isRunning = true, operation = CryptoOperation.ENCRYPT, error = null, decrypted = "")
        }
        viewModelScope.launch(dispatcher) {
            try {
                val ciphertext = service.encrypt(plaintext)
                mutableUiState.update { it.copy(isRunning = false, ciphertext = ciphertext, keyValue = readKeyValue()) }
            } catch (failure: CryptoFailure) {
                mutableUiState.update {
                    it.copy(
                        isRunning = false,
                        error = failure.reason,
                        ciphertext = when (failure.reason) {
                            CryptoError.OPERATION_FAILED, CryptoError.INVALID_HEX, CryptoError.EMPTY_INPUT -> ""
                            else -> it.ciphertext
                        },
                    )
                }
            }
        }
    }

    fun decrypt() {
        if (mutableUiState.value.isRunning) {
            return
        }
        val service = crypto
        if (service == null) {
            mutableUiState.update { it.copy(operation = CryptoOperation.DECRYPT, error = CryptoError.NOT_IMPLEMENTED) }
            return
        }
        val ciphertext = mutableUiState.value.ciphertext
        if (ciphertext.isEmpty()) {
            mutableUiState.update { it.copy(operation = CryptoOperation.DECRYPT, error = CryptoError.EMPTY_INPUT, decrypted = "") }
            return
        }
        mutableUiState.update { it.copy(isRunning = true, operation = CryptoOperation.DECRYPT, error = null) }
        viewModelScope.launch(dispatcher) {
            try {
                val plaintext = service.decrypt(ciphertext)
                mutableUiState.update { it.copy(isRunning = false, decrypted = plaintext, keyValue = readKeyValue()) }
            } catch (failure: CryptoFailure) {
                mutableUiState.update {
                    it.copy(isRunning = false, error = failure.reason, decrypted = "")
                }
            }
        }
    }
}

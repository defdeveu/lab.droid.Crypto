package defdev.labdroid.crypto.ui

import androidx.activity.compose.LocalActivity
import androidx.biometric.AuthenticationRequest
import androidx.biometric.AuthenticationResult
import androidx.biometric.AuthenticationResultCallback
import androidx.biometric.BiometricPrompt
import androidx.biometric.compose.rememberAuthenticationLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import defdev.labdroid.crypto.R
import defdev.labdroid.crypto.error.CryptoError
import defdev.labdroid.crypto.flows.biometric.BiometricAction
import defdev.labdroid.crypto.flows.biometric.BiometricAuthorizer

@Composable
fun BiometricPromptHost(authorizer: BiometricAuthorizer) {
    val request by authorizer.requests.collectAsStateWithLifecycle()
    request?.let { current ->
        key(current) { AuthenticationPrompt(authorizer, current) }
    }
}

@Composable
private fun AuthenticationPrompt(authorizer: BiometricAuthorizer, request: BiometricAuthorizer.Request) {
    val activity = LocalActivity.current
    val callback = remember(authorizer, request) {
        object : AuthenticationResultCallback {
            override fun onAuthResult(result: AuthenticationResult) {
                when (result) {
                    is AuthenticationResult.Success -> {
                        if (result.authType == BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC) {
                            authorizer.succeed(request, result.crypto?.cipher)
                        } else {
                            authorizer.fail(request, CryptoError.AUTHENTICATION_FAILED)
                        }
                    }
                    is AuthenticationResult.Error -> authorizer.fail(request, result.errorCode.toCryptoError())
                    is AuthenticationResult.CustomFallbackSelected ->
                        authorizer.fail(request, CryptoError.AUTHENTICATION_CANCELLED)
                }
            }
        }
    }
    val launcher = rememberAuthenticationLauncher(callback)
    val title = stringResource(
        when (request.action) {
            BiometricAction.ENCRYPT -> R.string.authentication_encrypt_title
            BiometricAction.DECRYPT -> R.string.authentication_decrypt_title
        },
    )
    val subtitle = stringResource(R.string.authentication_subtitle)
    val cancel = stringResource(R.string.action_cancel)
    LaunchedEffect(request, launcher) {
        if (request.startOnce()) {
            launcher.launch(
                AuthenticationRequest.biometricRequest(
                    title,
                    AuthenticationRequest.Biometric.Fallback.CustomOption(cancel),
                ) {
                    setSubtitle(subtitle)
                    setMinStrength(AuthenticationRequest.Biometric.Strength.Class3(BiometricPrompt.CryptoObject(request.cipher)))
                },
            )
        }
    }
    DisposableEffect(request, launcher, activity) {
        onDispose {
            if (activity?.isChangingConfigurations != true) {
                launcher.cancel()
                authorizer.fail(request, CryptoError.AUTHENTICATION_CANCELLED)
            }
        }
    }
}

private fun Int.toCryptoError(): CryptoError = when (this) {
    BiometricPrompt.ERROR_CANCELED,
    BiometricPrompt.ERROR_USER_CANCELED,
    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> CryptoError.AUTHENTICATION_CANCELLED
    BiometricPrompt.ERROR_LOCKOUT,
    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> CryptoError.BIOMETRIC_LOCKED_OUT
    BiometricPrompt.ERROR_NO_BIOMETRICS -> CryptoError.BIOMETRIC_NOT_ENROLLED
    BiometricPrompt.ERROR_HW_NOT_PRESENT,
    BiometricPrompt.ERROR_HW_UNAVAILABLE,
    BiometricPrompt.ERROR_SECURITY_UPDATE_REQUIRED -> CryptoError.BIOMETRIC_UNAVAILABLE
    else -> CryptoError.AUTHENTICATION_FAILED
}

package defdev.labdroid.crypto.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import defdev.labdroid.crypto.R
import defdev.labdroid.crypto.error.CryptoError
import defdev.labdroid.crypto.theme.LabMono
import defdev.labdroid.crypto.theme.LabTitle

@Composable
fun LabHeader(title: String) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMediumEmphasized.copy(
                    fontFamily = LabTitle,
                    fontSize = 19.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight(875),
                ),
                modifier = Modifier.semantics { heading() },
            )
        },
        navigationIcon = {
            Box(modifier = Modifier.padding(start = 16.dp, end = 8.dp)) {
                Icon(
                    painter = painterResource(R.drawable.brand_stamp),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(40.dp),
                )
            }
        },
    )
}

@Composable
fun MessageField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    placeholder: String = "",
    minLines: Int = 3,
) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { event ->
                if (event.key == Key.Tab) {
                    if (event.type == KeyEventType.KeyDown) {
                        focusManager.moveFocus(
                            if (event.isShiftPressed) FocusDirection.Previous else FocusDirection.Next,
                        )
                    }
                    true
                } else {
                    false
                }
            },
        enabled = enabled,
        label = {
            Text(
                text = label,
                fontWeight = MaterialTheme.typography.labelLargeEmphasized.fontWeight
            )
        },
        placeholder = { Text(text = placeholder) },
        minLines = minLines,
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = LabMono),
        shape = MaterialTheme.shapes.large,
    )
}

@Composable
fun OutputField(title: String, text: String, placeholder: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.largeIncreased,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmallEmphasized)
            SelectionContainer {
                Text(
                    text = text.ifEmpty { placeholder },
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = LabMono),
                    color = if (text.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OperationFeedback(isRunning: Boolean, error: CryptoError?) {
    if (isRunning) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LoadingIndicator()
            Text(
                text = stringResource(R.string.operation_working),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else if (error != null) {
        Text(
            text = stringResource(error.messageRes()),
            style = MaterialTheme.typography.bodyMedium,
            color = if (error == CryptoError.NOT_IMPLEMENTED || error == CryptoError.AUTHENTICATION_CANCELLED) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
fun PrimaryAction(
    label: String,
    onClick: () -> Unit,
    isRunning: Boolean,
    showShield: Boolean = false,
    enabled: Boolean = true,
) {
    val height = 56.dp
    Button(
        onClick = onClick,
        enabled = enabled && !isRunning,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = height),
        colors = if (isRunning) {
            ButtonDefaults.buttonColors(
                disabledContainerColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            ButtonDefaults.buttonColors()
        },
        shapes = ButtonDefaults.shapes(
            shape = MaterialTheme.shapes.large,
            pressedShape = ButtonDefaults.mediumPressedShape,
        ),
        contentPadding = ButtonDefaults.contentPaddingFor(height),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ButtonDefaults.MediumIconSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showShield) {
                Icon(
                    painter = painterResource(R.drawable.ic_shield_moon),
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.MediumIconSize),
                )
            }
            Text(text = label, style = ButtonDefaults.textStyleFor(height))
        }
    }
}

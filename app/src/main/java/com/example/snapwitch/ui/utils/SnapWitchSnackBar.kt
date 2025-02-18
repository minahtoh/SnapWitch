package com.example.snapwitch.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay

class SnapWitchSnackBarState {
    var currentSnackbarData by mutableStateOf<SnackbarData?>(null)
        private set

    fun showSnackbar(
        message: String,
        duration: Long = 8000L,
        action: String? = null,
        onActionClick: (() -> Unit)? = null
    ) {
        currentSnackbarData = SnackbarData(
            message = message,
            duration = duration,
            action = action,
            onActionClick = onActionClick
        )
    }

    fun dismissCurrent() {
        currentSnackbarData = null
    }
}

data class SnackbarData(
    val message: String,
    val duration: Long = 3000L,
    val action: String? = null,
    val onActionClick: (() -> Unit)? = null
)

@Composable
fun SnapWitchSnackbarHost(
    modifier: Modifier = Modifier,
    snackbarData: SnackbarData? = null,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = snackbarData != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        snackbarData?.let { data ->
            SnapWitchSnackbar(
                snackbarData = data,
                onDismiss = onDismiss,
                modifier = Modifier
            )
        }
    }
}

@Composable
fun SnapWitchSnackbar(
    snackbarData: SnackbarData,
    onDismiss: () -> Unit,
    modifier: Modifier
) {
    val scope = rememberCoroutineScope()
    val backgroundColor = MaterialTheme.colorScheme.onPrimaryContainer
    val contentColor = MaterialTheme.colorScheme.inversePrimary

    LaunchedEffect(snackbarData) {
        delay(snackbarData.duration)
        onDismiss()
    }


    Surface(
        modifier = modifier
            .padding(16.dp)
            .wrapContentHeight()
            .width(450.dp),
        shape = RoundedCornerShape(25.dp),
        color = backgroundColor,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = contentColor
            )

            Text(
                text = snackbarData.message,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )

            if (snackbarData.action != null) {
                TextButton(
                    onClick = {
                        snackbarData.onActionClick?.invoke()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = contentColor
                    )
                ) {
                    Text(snackbarData.action)
                }
            }

            IconButton(
                onClick = { onDismiss() },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = contentColor
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss"
                )
            }
        }
    }
}

@Composable
fun rememberSnackbarState() = remember { SnapWitchSnackBarState() }

@Composable
fun PopupSnackbarHost(
    snackbarState: SnapWitchSnackBarState,
    modifier: Modifier = Modifier
) {
    if (snackbarState.currentSnackbarData != null) {
        Popup(
            alignment = Alignment.BottomCenter,
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            SnapWitchSnackbar(
                snackbarData = snackbarState.currentSnackbarData!!,
                onDismiss = { snackbarState.dismissCurrent() },
                modifier = modifier
            )
        }
    }
}
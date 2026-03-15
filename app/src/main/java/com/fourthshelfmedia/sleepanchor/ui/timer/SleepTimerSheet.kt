package com.fourthshelfmedia.sleepanchor.ui.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fourthshelfmedia.sleepanchor.ui.theme.AnchorAccent
import com.fourthshelfmedia.sleepanchor.ui.theme.AnchorCard
import com.fourthshelfmedia.sleepanchor.ui.theme.AnchorSurface

data class TimerOption(val label: String, val minutes: Int)

private val timerOptions = listOf(
    TimerOption("15 min", 15),
    TimerOption("30 min", 30),
    TimerOption("1 hour", 60),
    TimerOption("2 hours", 120),
    TimerOption("3 hours", 180),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerSheet(
    isTimerActive: Boolean,
    onSetTimer: (minutes: Int, fadeOut: Boolean) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var fadeOutEnabled by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AnchorSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                text = "Sleep Timer",
                style = MaterialTheme.typography.headlineMedium,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Timer preset buttons
            timerOptions.forEach { option ->
                Button(
                    onClick = { onSetTimer(option.minutes, fadeOutEnabled) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AnchorCard,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fade out toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Fade out",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Gradually lower volume over last 5 minutes",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Switch(
                    checked = fadeOutEnabled,
                    onCheckedChange = { fadeOutEnabled = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = AnchorAccent),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cancel button (only if timer is active)
            if (isTimerActive) {
                OutlinedButton(
                    onClick = onCancelTimer,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Cancel Timer")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

package es.aipp.shiftclock.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.aipp.shiftclock.R

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.about)) },
        text = {
            Column {
                Text("Desarrollador: Roberto J.", style = MaterialTheme.typography.bodyLarge)
                Text("Email: rj@aipp.es", style = MaterialTheme.typography.bodyLarge)
                Text("Empresa: aipp.es", style = MaterialTheme.typography.bodyMedium)
                Text("Versión: 0.8", style = MaterialTheme.typography.bodyMedium)
                Text("Licencia: GPLv3", style = MaterialTheme.typography.bodyMedium)
                Text("Github: https://github.com/Omarchio23/ShiftClock", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.accept))
            }
        }
    )
}

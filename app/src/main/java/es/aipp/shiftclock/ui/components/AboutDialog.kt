package es.aipp.shiftclock.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.aipp.shiftclock.R

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary) },
        title = { 
            Text(
                stringResource(R.string.about), 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Bold 
            ) 
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("Versión 1.0.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Desarrollador: Roberto J.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text("Email: rj@aipp.es", style = MaterialTheme.typography.bodyMedium)
                        Text("Empresa: aipp.es", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Copyright © 2026 Roberto J.\nTodos los derechos reservados.", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.accept))
            }
        }
    )
}

package com.middin.innovatie.app.ui.bluetooth

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.middin.innovatie.app.R
import com.middin.innovatie.app.ui.theme.MiddinDimens

@Composable
fun BluetoothScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var connectOk by remember { mutableStateOf(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { ok -> connectOk = ok }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val ok = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED
            if (!ok) {
                launcher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                connectOk = true
            }
        }
    }

    val adapter = remember {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?)?.adapter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = MiddinDimens.screenHorizontalPadding(),
                vertical = MiddinDimens.screenVerticalPadding(),
            ),
    ) {
        Text(stringResource(R.string.bt_intro), style = MaterialTheme.typography.bodyMedium)
        when {
            adapter == null -> {
                Text(
                    stringResource(R.string.bt_no_adapter),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            !connectOk -> {
                Text(
                    stringResource(R.string.bt_permission_needed),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            else -> {
                Text(
                    stringResource(R.string.bt_paired_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp),
                )
                val devices = adapter.bondedDevices?.sortedBy { it.name ?: it.address } ?: emptyList()
                if (devices.isEmpty()) {
                    Text(stringResource(R.string.bt_none_paired), modifier = Modifier.padding(top = 8.dp))
                } else {
                    devices.forEach { d ->
                        Text(
                            "${d.name ?: d.address}\n${d.address}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                }
            }
        }
    }
}

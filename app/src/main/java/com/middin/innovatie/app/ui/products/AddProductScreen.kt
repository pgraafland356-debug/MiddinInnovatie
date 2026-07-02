package com.middin.innovatie.app.ui.products

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.middin.innovatie.app.R
import com.middin.innovatie.app.ui.rememberAppContainer
import com.middin.innovatie.app.ui.theme.MiddinDimens
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AddProductScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { ContextCompat.getMainExecutor(context) }
    val scope = rememberCoroutineScope()
    val viewModel: AddProductViewModel = viewModel(
        factory = AddProductViewModel.factory(rememberAppContainer().database.productDao()),
    )

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var cameraReady by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        cameraReady = granted
    }

    LaunchedEffect(Unit) {
        val ok = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        if (ok) {
            cameraReady = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // PreviewView defaults to SurfaceView (PERFORMANCE), which composites above siblings and overlaps
    // fields inside a scroll. COMPATIBLE uses TextureView and stays in the normal view stack.
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = MiddinDimens.screenHorizontalPadding(),
                vertical = MiddinDimens.screenVerticalPadding(),
            ),
    ) {
        if (!cameraReady) {
            Text(stringResource(R.string.product_camera_permission), modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(12.dp))
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RectangleShape),
            update = { view ->
                if (previewView !== view) {
                    previewView = view
                }
            },
        )
        Button(
            onClick = {
                val cap = imageCapture ?: return@Button
                val file = File(context.cacheDir, "cap_${System.currentTimeMillis()}.jpg")
                val opts = ImageCapture.OutputFileOptions.Builder(file).build()
                cap.takePicture(
                    opts,
                    executor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            photoPath = file.absolutePath
                            errorText = null
                        }

                        override fun onError(exception: ImageCaptureException) {
                            errorText = exception.message ?: "Capture failed"
                        }
                    },
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            enabled = cameraReady && imageCapture != null,
        ) {
            Text(stringResource(R.string.product_capture_photo))
        }
        photoPath?.let {
            Text(
                stringResource(R.string.product_photo_saved),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding(),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                label = { Text(stringResource(R.string.product_name_label)) },
                singleLine = true,
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = { Text(stringResource(R.string.product_desc_label)) },
                minLines = 3,
            )
            errorText?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    scope.launch {
                        runCatching {
                            viewModel.saveProduct(context, name, description, photoPath)
                            onClose()
                        }.onFailure { e ->
                            errorText = e.message ?: "Error"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank(),
            ) {
                Text(stringResource(R.string.product_save))
            }
            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                Text(stringResource(R.string.product_cancel))
            }
        }
    }

    DisposableEffect(previewView, cameraReady, lifecycleOwner) {
        val pv = previewView
        if (pv == null || !cameraReady) {
            return@DisposableEffect onDispose { }
        }
        val future = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            runCatching {
                val provider = future.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(pv.surfaceProvider)
                }
                val capture = ImageCapture.Builder().build()
                imageCapture = capture
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    capture,
                )
            }
        }
        future.addListener(listener, executor)
        onDispose {
            runCatching {
                if (future.isDone) {
                    future.get().unbindAll()
                }
            }
        }
    }
}

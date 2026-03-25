package com.middin.innovatie.app.ui.products

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.middin.innovatie.app.R
import com.middin.innovatie.app.data.local.Product
import com.middin.innovatie.app.data.local.ProductDao
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.tasks.await
import java.io.File

class AddProductViewModel(
    private val dao: ProductDao,
) : ViewModel() {

    suspend fun saveProduct(
        context: Context,
        name: String,
        description: String,
        photoPath: String?,
    ) {
        require(name.isNotBlank()) { context.getString(R.string.product_name_required) }
        var desc = description.trim()
        if (!photoPath.isNullOrBlank()) {
            val labels = runCatching { detectLabels(context, photoPath) }.getOrDefault("")
            if (labels.isNotEmpty()) {
                val prefix = context.getString(R.string.product_labels_prefix)
                desc = if (desc.isEmpty()) "$prefix $labels" else "$desc\n$prefix $labels"
            }
        }
        val uri = photoPath?.let { File(it).toURI().toString() }
        dao.insert(
            Product(
                name = name.trim(),
                description = desc,
                imageUri = uri,
            ),
        )
    }

    private suspend fun detectLabels(context: Context, path: String): String {
        val uri = Uri.fromFile(File(path))
        val image = InputImage.fromFilePath(context, uri)
        val labels = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS).process(image).await()
        return labels.take(5).joinToString { it.text }
    }

    companion object {
        fun factory(dao: ProductDao) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AddProductViewModel(dao) as T
        }
    }
}

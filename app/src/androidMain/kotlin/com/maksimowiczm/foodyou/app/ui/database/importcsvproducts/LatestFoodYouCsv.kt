package com.maksimowiczm.foodyou.app.ui.database.importcsvproducts

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import java.io.IOException
import java.security.MessageDigest

internal class LatestFoodYouCsv(context: Context) {
    private val resolver = context.contentResolver
    private val preferences = context.getSharedPreferences("latest_food_you_csv", Context.MODE_PRIVATE)

    fun folder(): Uri? =
        preferences.getString("folder", null)?.let(Uri::parse)?.takeIf { uri ->
            resolver.persistedUriPermissions.any { it.uri == uri && it.isReadPermission }
        }

    fun setFolder(uri: Uri) {
        val previous = folder()
        resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        preferences.edit().putString("folder", uri.toString()).apply()
        if (previous != null && previous != uri) {
            runCatching {
                resolver.releasePersistableUriPermission(previous, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }

    fun newest(tree: Uri): File? {
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(
            tree,
            DocumentsContract.getTreeDocumentId(tree),
        )
        val columns = arrayOf(
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_LAST_MODIFIED,
        )
        val cursor = resolver.query(children, columns, null, null, null)
            ?: throw IOException("Could not open the import folder.")
        return cursor.use {
            var newest: File? = null
            while (it.moveToNext()) {
                val name = it.getString(1) ?: continue
                if (it.getString(2) == Document.MIME_TYPE_DIR || !isFoodYouCsv(name)) continue
                val uri = DocumentsContract.buildDocumentUriUsingTree(tree, it.getString(0))
                val candidate = File(uri, name, it.getLong(3))
                val current = newest
                if (current == null || fileOrder.compare(candidate, current) > 0) {
                    newest = candidate
                }
            }
            newest
        }
    }

    fun wasImported(digest: String) = preferences.getBoolean("imported:$digest", false)

    fun rememberImport(digest: String) {
        preferences.edit().putBoolean("imported:$digest", true).apply()
    }

    data class File(val uri: Uri, val name: String, val modified: Long)

    companion object {
        private val fileOrder = compareBy<File>({ it.modified }, { it.name }, { it.uri.toString() })

        internal fun isFoodYouCsv(name: String): Boolean =
            name.startsWith("FoodYou-", ignoreCase = true) && name.endsWith(".csv", ignoreCase = true)

        internal fun digest(content: ByteArray): String =
            MessageDigest.getInstance("SHA-256").digest(content).joinToString("") { "%02x".format(it) }
    }
}

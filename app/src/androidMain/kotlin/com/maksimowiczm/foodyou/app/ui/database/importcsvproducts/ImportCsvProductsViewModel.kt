package com.maksimowiczm.foodyou.app.ui.database.importcsvproducts

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.csv.CsvParser
import com.maksimowiczm.foodyou.common.domain.food.FoodSource
import com.maksimowiczm.foodyou.common.log.Logger
import com.maksimowiczm.foodyou.importexport.domain.entity.ProductField
import com.maksimowiczm.foodyou.importexport.domain.usecase.ImportCsvProductUseCase
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class ImportCsvProductsViewModel(
    private val importCsvProductUseCase: ImportCsvProductUseCase,
    private val csvParser: CsvParser,
    private val logger: Logger,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.WaitingForFile)
    val uiState = _uiState.asStateFlow()

    private var fileContent: ByteArray? = null

    fun importLatest(context: Context, selectedFolder: Uri? = null) {
        if (_uiState.value is UiState.Importing) return
        _uiState.value = UiState.Importing(0)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inbox = LatestFoodYouCsv(context)
                if (selectedFolder != null) inbox.setFolder(selectedFolder)
                val folder = inbox.folder()
                if (folder == null) {
                    _uiState.value = UiState.ChooseFolder
                    return@launch
                }

                val file = inbox.newest(folder)
                if (file == null) {
                    _uiState.value = UiState.LatestResult(
                        "No FoodYou-*.csv files found in the import folder."
                    )
                    return@launch
                }

                val content = readCsv(file.uri, context, limit = 2 * 1024 * 1024)
                val digest = LatestFoodYouCsv.digest(content)
                if (inbox.wasImported(digest)) {
                    _uiState.value = UiState.LatestResult("${file.name} was already imported.")
                    return@launch
                }

                val stream = content.toList().asFlow()
                val mapper = foodYouCsvMapping(csvParser, stream, file.name)

                val count = importCsvProducts(stream, mapper)
                inbox.rememberImport(digest)
                _uiState.value = UiState.LatestResult(
                    "${file.name}\n$count ${if (count == 1) "product" else "products"} added."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: SecurityException) {
                _uiState.value = UiState.LatestResult("Choose the import folder again to restore access.")
            } catch (e: Exception) {
                logger.w(TAG, e) { "Could not import latest Food You CSV" }
                _uiState.value = UiState.LatestResult(e.message ?: "Could not import the latest CSV.")
            }
        }
    }

    private fun readCsv(uri: Uri, context: Context, limit: Int? = null): ByteArray {
        val stream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Could not open the CSV file.")
        val bytes = stream.use { input ->
            if (limit == null) input.readBytes() else {
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    val size = input.read(buffer)
                    if (size < 0) break
                    if (output.size() + size > limit) {
                        throw IOException("CSV is too large for quick import. Use manual CSV import.")
                    }
                    output.write(buffer, 0, size)
                }
                output.toByteArray()
            }
        }
        val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        return if (bytes.size >= bom.size && bom.indices.all { bytes[it] == bom[it] }) {
            bytes.copyOfRange(bom.size, bytes.size)
        } else {
            bytes
        }
    }

    fun handleCsv(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val content = readCsv(uri, context)
                val header = csvParser.parse(content.toList().asFlow()).firstOrNull().orEmpty()

                if (header.isEmpty() || header.any { it == null }) {
                    _uiState.value =
                        UiState.FailedToOpenFile("CSV file is empty or could not be read.")
                } else {
                    this@ImportCsvProductsViewModel.fileContent = content
                    _uiState.value = UiState.FileOpened(header.mapNotNull { it })
                }
            } catch (e: IOException) {
                _uiState.value = UiState.FailedToOpenFile("Error reading CSV file: ${e.message}")
            }
        }
    }

    fun import(fieldMap: Map<ProductField, String>) {
        val header = (_uiState.value as? UiState.FileOpened)?.header

        if (header == null) {
            _uiState.value = UiState.FailedToImport("No file opened. Please open a CSV file first.")
            return
        }

        if (requiredCsvFields.any { it !in fieldMap }) {
            _uiState.value =
                UiState.MissingRequiredFields(
                    "Missing required fields: ${requiredCsvFields.joinToString(", ")}. Please ensure all required fields are mapped."
                )
            return
        }

        // Read whole file content into memory. We are on mobile anyway, if it doesn't fit in memory
        // it won't be comfortable to use anyway. Might have to fix later 😆
        val fileContent = this.fileContent
        if (fileContent == null) {
            _uiState.value = UiState.FailedToImport("Stream is null. Please open a CSV file first.")
            return
        }

        _uiState.value = UiState.Importing(0)

        val mapper =
            header.map { columnName ->
                fieldMap.firstNotNullOfOrNull { (field, value) ->
                    if (value == columnName) field else null
                }
            }

        viewModelScope.launch {
            try {
                val count = importCsvProducts(fileContent.toList().asFlow(), mapper)
                _uiState.value = UiState.ImportSuccess(count)
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                logger.w(TAG, e) { "Failed to import file" }
                _uiState.value = UiState.FailedToImport(e.message)
            } catch (e: Exception) {
                logger.w(TAG, e) { "Failed to import file" }
                _uiState.value = UiState.FailedToImport(e.message)
            } finally {
                this@ImportCsvProductsViewModel.fileContent = null
            }
        }
    }

    private suspend fun importCsvProducts(stream: Flow<Byte>, mapper: List<ProductField?>): Int =
        importCsvProductUseCase
            .import(
                mapper = mapper,
                stream = stream,
                source = FoodSource.Type.User,
                skipHeader = true,
            )
            .onEach { _uiState.value = UiState.Importing(it) }
            .last()

    private companion object {
        private const val TAG = "ImportCsvProductsViewModel"
    }
}

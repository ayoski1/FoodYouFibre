package com.maksimowiczm.foodyou.app.ui.database.importcsvproducts

import com.maksimowiczm.foodyou.common.csv.CsvParser
import com.maksimowiczm.foodyou.importexport.domain.entity.ProductField
import com.maksimowiczm.foodyou.importexport.domain.entity.csvHeader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.firstOrNull

internal val requiredCsvFields =
    setOf(
        ProductField.Name,
        ProductField.Energy,
        ProductField.Proteins,
        ProductField.Carbohydrates,
        ProductField.Fats,
    )

/** Validate the entire file before starting a database import. The byte flow must be reusable. */
internal suspend fun foodYouCsvMapping(
    csvParser: CsvParser,
    stream: Flow<Byte>,
    fileName: String,
): List<ProductField?> {
    val fields = ProductField.entries.associateBy { it.csvHeader() }
    val header = csvParser.parse(stream).firstOrNull().orEmpty()
    val mapper = header.map { fields[it?.trim()] }
    val mapped = mapper.filterNotNull()
    require(mapped.containsAll(requiredCsvFields) && mapped.size == mapped.distinct().size) {
        "$fileName has missing or repeated Food You columns."
    }
    val requiredColumns = mapper.mapIndexedNotNull { index, field ->
        if (field in requiredCsvFields) index to field else null
    }
    var rowCount = 0
    csvParser.parse(stream).drop(1).collect { row ->
        rowCount++
        require(row.size == header.size && requiredColumns.all { (index, field) ->
            val value = row[index]?.trim()
            if (field == ProductField.Name) !value.isNullOrBlank()
            else value?.toDoubleOrNull()?.let { it.isFinite() && it >= 0 } == true
        }) {
            "$fileName has a missing name or invalid nutrition values on product row $rowCount."
        }
    }
    require(rowCount > 0) { "$fileName contains no products." }
    return mapper
}

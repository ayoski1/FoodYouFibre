package com.maksimowiczm.foodyou.app.ui.database.importcsvproducts

import com.maksimowiczm.foodyou.common.infrastructure.csv.CsvParserImpl
import com.maksimowiczm.foodyou.importexport.domain.entity.ProductField
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking

class FoodYouCsvMappingTest {
    @Test
    fun matchesReorderedColumnsAndPreservesFibreAndServing() = runBlocking<Unit> {
        val mapping = mapping(
            "Name,Carbohydrates (g),Dietary Fiber (g),Energy (kcal),Fats (g),Proteins (g),Serving Weight (g)," +
                "Package Weight (g),Note\r\n\"Nuts, mixed\",5.9,8.8,649,58.2,21.1,150,300,\"Half the bag\""
        )

        assertEquals(ProductField.Carbohydrates, mapping[1])
        assertEquals(ProductField.DietaryFiber, mapping[2])
        assertEquals(ProductField.ServingWeight, mapping[6])
        assertEquals(ProductField.PackageWeight, mapping[7])
    }

    @Test
    fun rejectsInvalidLaterRowsBeforeImport() = runBlocking<Unit> {
        assertFailsWith<IllegalArgumentException> {
            mapping("$header\nRice,177,3.5,37,1.5\nNuts,649,NaN,5.9,58.2")
        }
    }

    @Test
    fun rejectsEmptyAndAmbiguousFiles() = runBlocking<Unit> {
        assertFailsWith<IllegalArgumentException> { mapping("$header\n") }
        assertFailsWith<IllegalArgumentException> {
            mapping("$header,Name\nRice,177,3.5,37,1.5,Rice")
        }
    }

    private suspend fun mapping(csv: String) =
        foodYouCsvMapping(CsvParserImpl(), csv.encodeToByteArray().toList().asFlow(), "FoodYou-test.csv")

    private val header = "Name,Energy (kcal),Proteins (g),Carbohydrates (g),Fats (g)"
}

#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
kotlinc app/src/commonMain/kotlin/com/maksimowiczm/foodyou/common/domain/food/{NutrientsHelper,NutrientValue,NutritionFacts,NutritionFactsField}.kt app/src/commonTest/kotlin/com/maksimowiczm/foodyou/common/domain/food/FibreAccountingChecks.kt tools/FibreChecks.kt -include-runtime -d "${TMPDIR:-/tmp}/food-you-fibre-checks.jar"
java -jar "${TMPDIR:-/tmp}/food-you-fibre-checks.jar"

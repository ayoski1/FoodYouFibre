# Food You Fibre

Personal test fork based on upstream Food You 3.4.9, commit
00637df98ef36b6503e796f7d5152275b201022b. Branch: feature/fibre-summary.

## Implemented

- Separate Android application ID `com.maksimowiczm.foodyou.fibre` and app name
  Food You Fibre. The official app and its data remain in their own sandbox.
- Compact fibre amount and existing daily target beside the three macro bars.
  Tap for total, soluble and insoluble recorded amounts. Unknown is shown as a
  dash; incomplete recorded amounts have a plus sign and an explanation.
- Wrapping summary layout for narrow screens. Full device layout and large-font
  checks must be completed on the compiled app before routine use.
- Product editor carbohydrate explicitly excludes fibre. Its automatic energy
  calculation includes total fibre at 2 kcal/g. Both manual and remote-product
  forms react to fibre edits. Supplied calories start in manual mode and are
  preserved unless the user deliberately enables calculation.
- Automatic estimates with missing fibre carry a visible explanation. Fibre
  remains unknown in saved data; missing values are never stored as a false zero.
- USDA imports convert total carbohydrate to carbohydrate excluding fibre once
  at the source boundary. Supplied energy is retained. Unknown/invalid fibre
  makes that conversion unavailable rather than guessing it.
- Aggregation retains unknown nutrients through recipes and meals. Label energy
  and fibre are summed independently, without adding energy twice or counting
  soluble/insoluble fractions on top of total fibre.
- Existing CSV fields for total, soluble and insoluble fibre remain compatible.
  Chat-generated CSVs should use UK-style carbohydrate excluding fibre and
  identify estimates in Note. No API key or in-app AI backend is required.

## Deliberately unresolved

- Existing diary entries and products are not rewritten. Legacy USDA products
  may retain total-carbohydrate values and need review/reimport as new products.
- Open Food Facts and arbitrary CSVs can have ambiguous carbohydrate definitions.
  They are not automatically converted based on country guesses. Confirm the
  basis when preparing/importing them. A dedicated import-basis selector remains
  future work.
- Quick-add has no fibre input in upstream 3.4.9. These entries remain incomplete
  for fibre. Use a saved product with fibre values for complete accounting.
- Calorie and macro goals are not automatically changed. Percentage-based goals
  still use upstream macro allocation; set explicit gram goals for this test.
- Soluble/insoluble fractions do not drive calorie factors. Solubility does not
  reliably establish fermentability. In-app AI and inferred fibre composition
  are not implemented.
- Settings and goals are not covered by upstream's experimental database backup.
  Record them separately. Do not replace or uninstall your original app.

## Validation and build

`tools/check-fibre.sh` compiles the actual accounting and aggregation sources
with Kotlin 2.3.10 and runs fourteen checks independently of Android.

With JDK 21 and Android SDK 36 installed:

```
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

The same accounting checks are included in the Gradle common test sources.
`.github/workflows/fibre-build.yml` builds a test APK and runs JVM unit tests on
GitHub Actions after publishing the source to a connected account. This workflow
has not yet run on GitHub. Its APK uses test signing; stable signing-key setup is
required before promising seamless updates across build machines.

## Phone acceptance checks

1. Install alongside the original app and start with fictional data.
2. Log P10/C20/F5/fibre10: automatic estimate must be 185 kcal.
3. Import a product with explicit 185 kcal, edit fibre with calculation disabled:
   calories must stay 185. Enable calculation explicitly and check recomputation.
4. Log a food with missing fibre. The daily fibre subtotal must indicate that
   information is incomplete, including after recipe and portion scaling.
5. Check soluble4/insoluble6/total10 remains total10, never20.
6. Check narrow width, large text and tapping the fibre breakdown.
7. Only then restore a COPY of the diary in the fork using Food You's own backup
   interface and compare dates, amounts and totals. Set goals/settings separately.

Source and upstream GPL-3.0 licence are included. The test fork is published in
`ayoski1/FoodYouFibre` on branch `feature/fibre-summary`. Phone verification remains pending.

## Build result 12 November 2026

APK compiled and signature verified. Compiled JUnit runs passed fourteen accounting checks and forty CSV tests. Full phone layout and diary-restore checks remain pending. Test signing material is held separately from this source; do not commit private keys. See the delivered VALIDATION.txt for the exact verification scope.

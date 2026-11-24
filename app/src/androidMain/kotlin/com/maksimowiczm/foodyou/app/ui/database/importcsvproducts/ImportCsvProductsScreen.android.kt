package com.maksimowiczm.foodyou.app.ui.database.importcsvproducts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.component.SomethingWentWrongScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun ImportCsvProductsScreen(
    onBack: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier,
    latest: Boolean,
) {
    val viewModel: ImportCsvProductsViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    val context = LocalContext.current
    if (latest) {
        LatestCsvScreen(uiState, viewModel, onBack, modifier)
        return
    }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) {
                onBack()
            } else {
                viewModel.handleCsv(uri, context)
            }
        }

    LaunchedEffect(uiState) {
        when (uiState) {
            UiState.WaitingForFile -> launcher.launch(arrayOf("text/csv", "*/*"))
            else -> Unit
        }
    }

    when (uiState) {
        is UiState.WithError ->
            SomethingWentWrongScreen(
                onBack = onBack,
                message = uiState.message,
                modifier = modifier,
            )

        is UiState.FileOpened ->
            ImportCsvProductsScreen(
                header = uiState.header.sorted(),
                onBack = onBack,
                onImport = viewModel::import,
                modifier = modifier,
            )

        UiState.WaitingForFile ->
            Scaffold(modifier) { paddingValues ->
                Box(
                    modifier =
                        Modifier.fillMaxSize()
                            .padding(paddingValues)
                            .consumeWindowInsets(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator()
                }
            }

        UiState.ChooseFolder, is UiState.LatestResult -> Unit
        is UiState.ImportSuccess -> ImportingSuccessScreen(uiState.count, onBack, modifier)
        is UiState.Importing -> ImportingProductsScreen(uiState.count, modifier)
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun LatestCsvScreen(
    state: UiState,
    viewModel: ImportCsvProductsViewModel,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current.applicationContext
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) viewModel.importLatest(context, uri)
    }
    LaunchedEffect(viewModel) {
        if (viewModel.uiState.value == UiState.WaitingForFile) viewModel.importLatest(context)
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Import latest Food You CSV") },
                navigationIcon = { ArrowBackIconButton(onBack) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (state) {
                UiState.ChooseFolder -> {
                    Text("Choose a folder for your FoodYou-*.csv files, such as Downloads/FoodYou.")
                    Button(onClick = { picker.launch(null) }) { Text("Choose import folder") }
                }

                is UiState.LatestResult -> {
                    Text(state.message)
                    Button(onClick = { picker.launch(null) }) { Text("Change import folder") }
                }

                else -> LoadingIndicator()
            }
        }
    }
}

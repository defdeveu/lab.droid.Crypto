package defdev.labdroid.crypto

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import defdev.labdroid.crypto.ui.ComparisonScreen
import defdev.labdroid.crypto.ui.ExerciseScreen

@Composable
fun CryptoApp(container: AppContainer) {
    var showComparison by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = showComparison) {
        showComparison = false
    }

    if (showComparison) {
        val viewModel: ComparisonViewModel = viewModel(factory = container.comparisonViewModelFactory)
        ComparisonScreen(viewModel = viewModel, onBack = { showComparison = false })
    } else {
        val viewModel: CryptoViewModel = viewModel(factory = container.cryptoViewModelFactory)
        ExerciseScreen(viewModel = viewModel, onOpenComparison = { showComparison = true })
    }
}

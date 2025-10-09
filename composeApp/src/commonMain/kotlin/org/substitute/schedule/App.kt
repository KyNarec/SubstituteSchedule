package org.substitute.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import kotlinx.coroutines.launch
import org.substitute.schedule.networking.DsbApiClient
import org.substitute.schedule.networking.TimeTable
import org.substitute.schedule.ui.theme.AppTheme
import org.substitute.schedule.utils.enums.SelectedDay

@Composable
fun App(
    client: DsbApiClient
) {
    AppTheme() {
        var selectedDay by remember { mutableStateOf(SelectedDay.TODAY) }
        var distinctTables by remember { mutableStateOf<List<TimeTable>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // Launch the coroutine only once when the composable is first created
        LaunchedEffect(Unit) {
            try {
                val tables = client.getTimeTables()
                distinctTables = tables.distinctBy { it.uuid }
                println("Distinct Tables: $distinctTables")
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Error loading timetables: ${e.message}"
                isLoading = false
            }
        }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row {
                Button(onClick = { selectedDay = SelectedDay.TODAY }) {
                    Text("Today")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { selectedDay = SelectedDay.TOMORROW }) {
                    Text("Tomorrow")
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    // Show loading indicator
                    isLoading -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Text("Loading timetables...")
                        }
                    }

                    // Show error message
                    errorMessage != null -> {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    // Show "no data" message
                    distinctTables.isEmpty() -> {
                        Text("No timetables available")
                    }

                    // Show WebView with data
                    else -> {
                        val selectedIndex = when (selectedDay) {
                            SelectedDay.TODAY -> 0
                            SelectedDay.TOMORROW -> 1
                        }

                        // Make sure we don't go out of bounds
                        if (selectedIndex < distinctTables.size) {
                            val url = distinctTables[selectedIndex].detail

                            // Use key() to force recreation when selectedDay changes
                            key(selectedDay) {
                                val state = rememberWebViewState(url)
                                val loadingState = state.loadingState

                                Column(modifier = Modifier.fillMaxSize()) {
                                    if (loadingState is LoadingState.Loading) {
                                        LinearProgressIndicator(
                                            progress = { loadingState.progress },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    WebView(
                                        state = state,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        } else {
                            Text("Selected day not available")
                        }
                    }
                }
            }
        }
    }
}
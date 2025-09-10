package com.wismna.geoffroy.donext.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.presentation.screen.TaskListScreen
import com.wismna.geoffroy.donext.presentation.ui.theme.DoNextTheme
import com.wismna.geoffroy.donext.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoNextTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel: MainViewModel = hiltViewModel<MainViewModel>()
                    MainScreen(
                        viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    if (viewModel.isLoading) {
        // Show loading or empty state
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val startDestination = viewModel.taskLists[0]
        // TODO: get last opened tab from saved settings
        var selectedDestination by rememberSaveable { mutableIntStateOf(0) }

        Scaffold(modifier = modifier) { contentPadding ->
            PrimaryTabRow(
                selectedTabIndex = selectedDestination,
                modifier = Modifier.padding(contentPadding)
            ) {
                viewModel.taskLists.forEachIndexed { index, destination ->
                    Tab(
                        selected = selectedDestination == index,
                        onClick = {
                            navController.navigate(route = destination.name)
                            selectedDestination = index
                        },
                        text = {
                            Text(
                                text = destination.name,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
            AppNavHost(navController, startDestination, viewModel)
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: TaskList,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController,
        startDestination = startDestination.name
    ) {
        viewModel.taskLists.forEach { destination ->
            composable(destination.name) {
                TaskListScreen(destination, modifier)
            }
        }
    }
}


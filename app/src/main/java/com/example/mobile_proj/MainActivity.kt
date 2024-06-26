package com.example.mobile_proj

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.mobile_proj.data.database.Workout
import com.example.mobile_proj.data.models.Theme
import com.example.mobile_proj.database.AlertDialogConnection
import com.example.mobile_proj.database.Connection
import com.example.mobile_proj.ui.NavGraph
import com.example.mobile_proj.ui.WorkoutViewModel
import com.example.mobile_proj.ui.screens.addWorkout.AddWorkoutViewModel
import com.example.mobile_proj.ui.screens.settings.ThemeViewModel
import com.example.mobile_proj.ui.theme.MobileprojTheme
import io.realm.kotlin.mongodb.exceptions.InvalidCredentialsException
import io.realm.kotlin.mongodb.exceptions.ServiceException
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.koin.androidx.compose.koinViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Connection(this)

        setContent {
            val themeViewModel = koinViewModel<ThemeViewModel>()
            val themeState by themeViewModel.state.collectAsStateWithLifecycle()
            val openAlertDialog = remember { mutableStateOf(false) }
            val openAlertDialogCreds = remember { mutableStateOf(false) }

            val workoutViewModel = koinViewModel<WorkoutViewModel>()

            var remoteWorkout: Array<Workout> = arrayOf()
            try {
                db.start()
                remoteWorkout = db.retrieveWorkouts(db.retrieveFromSharedPreference().first)
            } catch (e: ServiceException) {
                openAlertDialog.value = true
            } catch (e: InvalidCredentialsException) {
                openAlertDialogCreds.value = true
            }

            val workout = runBlocking {
                return@runBlocking workoutViewModel.getWorkout()
            }

            if (this.intent.getBooleanExtra("reload", true)) {
                workout.forEach {
                    workoutViewModel.deleteWorkout(it)
                }
                remoteWorkout.forEach {
                    workoutViewModel.addWorkout(it)
                }

            } else {

                val intersection = remoteWorkout
                    .map { x -> x.idRemote }
                    .intersect(workout.map { x -> x.idRemote }.toSet())

                workout.forEach {
                    if (!intersection.contains(it.idRemote)) {
                        workoutViewModel.deleteWorkout(it)
                    }
                }
                remoteWorkout.forEach {
                    if (!intersection.contains(it.idRemote)) {
                        workoutViewModel.addWorkout(it)
                    }
                }

            }

            MobileprojTheme(darkTheme = when (themeState.theme) {
                Theme.Light -> false
                Theme.Dark -> true
                Theme.System -> isSystemInDarkTheme()
            }){
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        openAlertDialog.value -> {
                            AlertDialogConnection(
                                onDismissRequest = { openAlertDialog.value = false },
                                onConfirmation = { openAlertDialog.value = false },
                                onDismissButton = { this.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))},
                                dialogTitle = "Error in connecting to database",
                                dialogText = "Check your wi-fi connection",
                                icon = Icons.Default.Error
                            )
                        }
                        openAlertDialogCreds.value -> {
                            AlertDialogConnection(
                                onDismissRequest = {},
                                onConfirmation = {},
                                onDismissButton = {},
                                dialogTitle = "Wrong Credentials used to log in to server",
                                dialogText = "Report the bug and a patch will soon arrive",
                                icon = Icons.Default.BugReport
                            )
                        }
                    }


                    val navController = rememberNavController()
                    Scaffold { contentPadding ->
                        NavGraph(
                            navController,
                            modifier = Modifier.padding(contentPadding),
                            themeState,
                            themeViewModel,
                            db,
                            this
                        )
                    }
                }
            }
        }
    }
}

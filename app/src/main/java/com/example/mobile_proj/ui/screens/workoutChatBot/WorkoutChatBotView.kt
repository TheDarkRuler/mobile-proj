package com.example.mobile_proj.ui.screens.workoutChatBot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.android.volley.VolleyError
import com.example.mobile_proj.database.Connection
import com.example.mobile_proj.ui.Route
import com.example.mobile_proj.ui.composables.TopAppBar
import com.example.mobile_proj.ui.screens.addWorkout.AddWorkoutActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreen(
    navController: NavHostController,
    workoutChatBotViewModel: WorkoutChatBotViewModel,
    actions: AddWorkoutActions,
    onSubmit: () -> Unit,
    muscleGroup: String,
    exercise: String,
    db: Connection
) {
    val ctx = LocalContext.current
    var stringOutput by remember { mutableStateOf("") }
    val text by remember { mutableStateOf(exercise) }

    actions.setMuscleGroup(muscleGroup)
    actions.setExercise(exercise)
    actions.setUsername(db.retrieveFromSharedPreference().first)
    Column(modifier = Modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(navController, Route.ChatBot, null)
        TextField(
            value = "Muscle group: $muscleGroup",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.padding(16.dp))

        OutlinedTextField(
            value = text,
            label = { Text(text = "Ask something") },
            readOnly = true,
            onValueChange = { },
        )
        Spacer(modifier = Modifier.padding(16.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Button(onClick = {
                workoutChatBotViewModel.buttonLlamaAPI(text, ctx, object : ApiCallback {
                    override fun onSuccess(result: String) {
                        stringOutput = result
                    }

                    override fun onError(error: VolleyError) {
                        stringOutput = error.toString()
                    }
                })
            }
            ) {
                Text("Bot help me!")
            }
            Button(onClick = {
                onSubmit()
                navController.navigate(Route.Home.route) {
                    popUpTo(Route.Home.route) { inclusive = true }
                }}) {
                Text(text = "Save")
            }
        }
        Column(modifier = Modifier
            .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringOutput,
                Modifier.padding(16.dp))
        }
    }
    actions.setBotchat(stringOutput)
}
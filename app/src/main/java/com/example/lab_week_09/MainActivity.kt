package com.example.lab_week_09

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.lab_week_09.ui.theme.LAB_WEEK_09Theme
import com.example.lab_week_09.ui.theme.OnBackgroundItemText
import com.example.lab_week_09.ui.theme.OnBackgroundTitleText
import com.example.lab_week_09.ui.theme.PrimaryTextButton
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// --------------------------------------------------------------
// DATA CLASS (no codegen annotation)
// --------------------------------------------------------------
data class Student(
    val name: String
)

// --------------------------------------------------------------
// MAIN ACTIVITY
// --------------------------------------------------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LAB_WEEK_09Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    App(navController)
                }
            }
        }
    }
}

// --------------------------------------------------------------
// ROOT NAVIGATION COMPOSABLE
// --------------------------------------------------------------
@Composable
fun App(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // Home route
        composable("home") {
            Home { listJson ->
                // encode to safely include in url
                val encoded = URLEncoder.encode(listJson, StandardCharsets.UTF_8.toString())
                navController.navigate("resultContent/$encoded")
            }
        }

        // Result route with path parameter
        composable(
            "resultContent/{listData}",
            arguments = listOf(navArgument("listData") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val raw = backStackEntry.arguments?.getString("listData").orEmpty()
            val decoded = URLDecoder.decode(raw, StandardCharsets.UTF_8.toString())
            ResultContent(decoded)
        }
    }
}

// --------------------------------------------------------------
// HOME COMPOSABLE
// --------------------------------------------------------------
@Composable
fun Home(
    navigateFromHomeToResult: (String) -> Unit
) {
    // -------------------------------
    // initial list (could come from JSON or empty)
    // -------------------------------
    val initial = listOf(
        Student("Tanu"),
        Student("Tina"),
        Student("Tono")
    )

    // -------------------------------
    // STATE LIST (initial)
    // -------------------------------
    val listData = remember {
        mutableStateListOf<Student>().apply {
            addAll(initial)
        }
    }

    var inputField by remember { mutableStateOf("") }

    // when user presses Finish we convert list -> JSON and navigate
    HomeContent(
        listData = listData,
        inputFieldValue = inputField,
        onInputValueChange = { inputField = it },
        onButtonClick = {
            if (inputField.isNotBlank()) {
                listData.add(Student(inputField))
                inputField = ""
            }
        },
        navigateFromHomeToResult = {
            // Convert listData to JSON via Moshi
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val listType = Types.newParameterizedType(List::class.java, Student::class.java)
            val adapter = moshi.adapter<List<Student>>(listType)
            val json = adapter.toJson(listData.toList())
            navigateFromHomeToResult(json)
        }
    )
}

// --------------------------------------------------------------
// HOME CONTENT UI
// --------------------------------------------------------------
@Composable
fun HomeContent(
    listData: SnapshotStateList<Student>,
    inputFieldValue: String,
    onInputValueChange: (String) -> Unit,
    onButtonClick: () -> Unit,
    navigateFromHomeToResult: () -> Unit
) {
    LazyColumn {
        item {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                OnBackgroundTitleText(text = stringResource(id = R.string.enter_item))

                TextField(
                    value = inputFieldValue,
                    keyboardOptions = KeyboardOptions.Default,
                    onValueChange = { onInputValueChange(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                Row {
                    PrimaryTextButton(
                        text = stringResource(id = R.string.button_click)
                    ) { onButtonClick() }

                    PrimaryTextButton(
                        text = stringResource(id = R.string.button_navigate)
                    ) {
                        navigateFromHomeToResult()
                    }
                }
            }
        }

        items(listData) { item ->
            Column(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OnBackgroundItemText(text = item.name)
            }
        }
    }
}

// --------------------------------------------------------------
// RESULT PAGE: receives JSON string, parse and show list
// --------------------------------------------------------------
@Composable
fun ResultContent(listJson: String) {
    // parse json to List<Student>
    val students = remember(listJson) {
        try {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val listType = Types.newParameterizedType(List::class.java, Student::class.java)
            val adapter = moshi.adapter<List<Student>>(listType)
            adapter.fromJson(listJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList<Student>()
        }
    }

    // display json (optional) then list
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
    ) {
        OnBackgroundTitleText(text = "Result (raw JSON)")
        OnBackgroundItemText(text = listJson)

        Spacer(modifier = Modifier.height(12.dp))

        OnBackgroundTitleText(text = "Result (parsed list)")
        LazyColumn {
            items(students) { s ->
                OnBackgroundItemText(text = s.name)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHome() {
    LAB_WEEK_09Theme {
        Home({ /* no-op preview */ })
    }
}

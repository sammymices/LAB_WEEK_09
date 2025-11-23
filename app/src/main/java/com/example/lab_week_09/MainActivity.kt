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
import com.example.lab_week_09.ui.theme.*

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
// DATA CLASS
// --------------------------------------------------------------
data class Student(
    var name: String
)

// --------------------------------------------------------------
// ROOT NAVIGATION COMPOSABLE
// --------------------------------------------------------------
@Composable
fun App(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        composable("home") {
            Home { listAsString ->
                navController.navigate("resultContent/?listData=$listAsString")
            }
        }

        composable(
            "resultContent/?listData={listData}",
            arguments = listOf(navArgument("listData") {
                type = NavType.StringType
            })
        ) {
            ResultContent(it.arguments?.getString("listData").orEmpty())
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

    val listData = remember {
        mutableStateListOf(
            Student("Tanu"),
            Student("Tina"),
            Student("Tono")
        )
    }

    var inputField = remember { mutableStateOf(Student("")) }

    HomeContent(
        listData,
        inputField.value,
        { input -> inputField.value = inputField.value.copy(name = input) },
        {
            if (inputField.value.name.isNotBlank()) {
                listData.add(inputField.value)
                inputField.value = Student("")
            }
        },
        {
            navigateFromHomeToResult(listData.toList().toString())
        }
    )
}

// --------------------------------------------------------------
// HOME CONTENT (CHILD COMPOSABLE)
// --------------------------------------------------------------
@Composable
fun HomeContent(
    listData: SnapshotStateList<Student>,
    inputField: Student,
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
                    value = inputField.name,
                    keyboardOptions = KeyboardOptions(),
                    onValueChange = { onInputValueChange(it) }
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
// RESULT CONTENT COMPOSABLE
// --------------------------------------------------------------
@Composable
fun ResultContent(listData: String) {

    Column(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OnBackgroundItemText(text = listData)
    }
}

// --------------------------------------------------------------
// PREVIEW
// --------------------------------------------------------------
@Preview(showBackground = true)
@Composable
fun PreviewHome() {
    LAB_WEEK_09Theme {
        Home({})
    }
}

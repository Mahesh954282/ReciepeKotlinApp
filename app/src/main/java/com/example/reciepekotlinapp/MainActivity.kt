package com.example.reciepekotlinapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.google.accompanist.coil.rememberCoilPainter
import com.google.firebase.ktx.Firebase
import com.example.reciepekotlinapp.ui.theme.ReciepeKotlinAppTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow


private val db = Firebase.firestore
class MainActivity : ComponentActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReciepeKotlinAppTheme {
                RecipeFinderApp()
            }
        }
        //addSampleRecipesToFirestore()
    }
}
val sampleRecipes = listOf(
    Recipe("1", "Spaghetti Carbonara", "A classic Italian pasta dish with eggs, cheese, bacon, and black pepper.", "C:\\Users\\luvmo\\AndroidStudioProjects\\ReciepeKotlinApp\\app\\src\\main\\res\\drawable\\category_salad.png", listOf("Pasta", "Eggs", "Cheese", "Bacon", "Black Pepper"), "20 mins"),
    Recipe("2", "Chicken Curry", "Rich and creamy curry made with tender chicken pieces.", "res/drawable/category_salad.png", listOf("Chicken", "Onion", "Garlic", "Curry Powder", "Coconut Milk"), "40 mins"),
    // Add more sample recipes here
)

fun addSampleRecipesToFirestore() {
    val recipes = listOf(
        mapOf(
            "title" to "Spaghetti Carbonara",
            "description" to "A classic Italian pasta dish with eggs, cheese, bacon, and black pepper.",
            "imageUrl" to "https://images.unsplash.com/photo-1588013273468-315fd88ea34c?q=80&w=1169&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            "ingredients" to listOf("Pasta", "Eggs", "Cheese", "Bacon", "Black Pepper"),
            "estimatedTime" to "20 mins"
        ),
        mapOf(
            "title" to "Chicken Curry",
            "description" to "Rich and creamy curry made with tender chicken pieces.",
            "imageUrl" to "https://images.unsplash.com/photo-1588013273468-315fd88ea34c?q=80&w=1169&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            "ingredients" to listOf("Chicken", "Onion", "Garlic", "Curry Powder", "Coconut Milk"),
            "estimatedTime" to "40 mins"
        )
        // Add more recipes as needed
    )

    recipes.forEach { recipe ->
        db.collection("recipes").add(recipe)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
            }
    }
}



fun getRecipes(): LiveData<List<Recipe>> {
    val recipesLiveData = MutableLiveData<List<Recipe>>()

    db.collection("recipes")
        .get()
        .addOnSuccessListener { documents ->
            val recipesList = documents.mapNotNull { document ->
                document.toObject<Recipe>().copy(id = document.id)
            }
            recipesLiveData.value = recipesList
        }
        .addOnFailureListener { exception ->
            // Handle the error
        }

    return recipesLiveData
}




object AppRoutes {
    const val LOGIN = "login"
    const val RECIPES = "recipes"
    const val RECIPE_DETAILS = "recipeDetails"
}




@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RecipeFinderApp() {
    val navController = rememberNavController()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Recipe Finder",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            )
        }
    ) {
        NavHost(navController = navController, startDestination = AppRoutes.LOGIN) {
            composable(AppRoutes.LOGIN) { LoginScreen(navController) }
            composable(AppRoutes.RECIPES) {
                RecipeScreen(recipes = sampleRecipes, navController)
            }
            composable("${AppRoutes.RECIPE_DETAILS}/{recipeId}") { backStackEntry ->
                // Ensure this matches the route used in RecipeCard's clickable modifier
                RecipeDetailsScreen(recipeId = backStackEntry.arguments?.getString("recipeId"), navController)
            }

            composable("signup") { SignupScreen(navController) } // Add this line
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginStatus by remember { mutableStateOf("") }

    val auth = Firebase.auth

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Login to Recipe Finder", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // Email input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Password input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("LoginScreen", "Login successful")
                            navController.navigate(AppRoutes.RECIPES)
                        } else {
                            Log.e("LoginScreen", "Login failed", task.exception)
                            loginStatus = "Login failed: ${task.exception?.message}"
                        }
                    }
            }) {
                Text("Login")
            }


            if (loginStatus.isNotEmpty()) {
                Text(loginStatus)
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { navController.navigate("signup") }) {
                Text("Don't have an account? Sign up")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(navController: NavHostController)  {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var signupStatus by remember { mutableStateOf("") }

    val auth = Firebase.auth
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Sign Up for Recipe Finder", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            signupStatus = "Signup successful"
                        } else {
                            signupStatus = "Signup failed: ${task.exception?.message}"
                        }
                    }
            }) {
                Text("Sign Up")
            }
            if (signupStatus.isNotEmpty()) {
                Text(signupStatus)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(onSearchQueryChanged: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onSearchQueryChanged(it)
        },
        label = { Text("Search Recipes") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)

    )
}

@Composable
fun RecipeScreen(recipes: List<Recipe>, navController: NavHostController) {
    var searchQuery by remember { mutableStateOf("") }
    val recipesLiveData = getRecipes()
    val recipes by recipesLiveData.observeAsState(initial = emptyList())
    val filteredRecipes = recipes.filter {
        searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true)
    }

    Column {
        // Add some top padding to the SearchBar
        Spacer(modifier = Modifier.height(25.dp))
        SearchBar(onSearchQueryChanged = { query ->
            searchQuery = query
        })

        // Display recipes in a Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredRecipes) { recipe ->
                RecipeCard(recipe, navController)
            }
        }
    }
}




@Composable
fun RecipeCard(recipe: Recipe, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { navController.navigate("${AppRoutes.RECIPE_DETAILS}/${recipe.id}") },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp) // Rounded corners for the card
    ) {
        Column {
            Image(
                painter = rememberCoilPainter(request = recipe.imageUrl),
                contentDescription = "Recipe Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)) // Rounded corners for the image
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Time: ${recipe.estimatedTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Ingredients: ${recipe.ingredients.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}



@Composable
fun RecipeDetailsScreen(recipeId: String?, navController: NavHostController) {
    val recipesLiveData = getRecipes()
    val recipes by recipesLiveData.observeAsState(initial = emptyList())

    // Find the recipe with the specified recipeId
    val recipe = recipes.find { it.id == recipeId }

    if (recipe != null) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Image(
                painter = rememberCoilPainter(request = recipe.imageUrl),
                contentDescription = "Recipe Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = recipe.title, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Time: ${recipe.estimatedTime}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Ingredients", style = MaterialTheme.typography.titleMedium)
                recipe.ingredients.forEach { ingredient ->
                    Text(text = "• $ingredient", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Description", style = MaterialTheme.typography.titleMedium)
                Text(text = recipe.description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    } else {
        Text("Recipe not found", style = MaterialTheme.typography.bodyMedium)
    }
}




@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    ReciepeKotlinAppTheme {
        LoginScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSignupScreen() {
    ReciepeKotlinAppTheme {
        SignupScreen(rememberNavController())
    }
}

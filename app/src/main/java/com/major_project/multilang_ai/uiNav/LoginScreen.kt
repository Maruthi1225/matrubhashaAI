package com.major_project.multilang_ai.uiNav

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.major_project.multilang_ai.R
import com.major_project.multilang_ai.ui.theme.AppThemeColors

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dynamic Logo selection based on Theme
    val isDark = AppThemeColors.isDark()
    val logoResId = if (isDark) R.drawable.dark else R.drawable.light
    val backgroundGradient = AppThemeColors.backgroundGradient()

    // Google Sign-In Setup
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener { authResult ->
                    if (authResult.isSuccessful) {
                        onLoginSuccess()
                    } else {
                        errorMessage = authResult.exception?.message
                        isLoading = false
                    }
                }
            } catch (e: ApiException) {
                errorMessage = "Google sign in failed (Code ${e.statusCode}): ${e.message}"
                isLoading = false
            }
        } else {
            isLoading = false
            if (result.resultCode != Activity.RESULT_CANCELED) {
                errorMessage = "Sign in was not successful"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            // TOP SECTION
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Surface(
                    modifier = Modifier.clip(CircleShape),
                    color = Color.White.copy(alpha = 0.05f)
                ) {
                    Image(
                        painter = painterResource(id = logoResId),
                        contentDescription = "Matrubhasha AI Logo",
                        modifier = Modifier.size(130.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "MATRUBHASHA AI",
                    letterSpacing = 1.5.sp,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        brush = Brush.linearGradient(
                            AppThemeColors.accentGradient(true)
                        )
                    )
                )

                Text(
                    text = "The Indian Multilingual Voice Assistant.",
                    color = AppThemeColors.textColorPrimary().copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isSignUp)
                        "Create your futuristic account"
                    else
                        "Your voice, everywhere.",
                    color = AppThemeColors.textColorPrimary().copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            }


            // FORM SECTION
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = AppThemeColors.cardColor(),
                tonalElevation = 2.dp
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        onClick = {
                            // your login code
                        }
                    ) {

                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                if (isSignUp)
                                    "CREATE ACCOUNT"
                                else
                                    "CONTINUE",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(
                        onClick = { isSignUp = !isSignUp }
                    ) {
                        Text(
                            if (isSignUp)
                                "Back to Login"
                            else
                                "New here? Sign Up"
                        )
                    }
                }
            }

            // BOTTOM SECTION
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = AppThemeColors.textColorPrimary().copy(alpha = 0.1f)
                    )

                    Text(
                        " CONNECT WITH ",
                        modifier = Modifier.padding(horizontal = 10.dp),
                        color = AppThemeColors.textColorPrimary().copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )

                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = AppThemeColors.textColorPrimary().copy(alpha = 0.1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = {
                        isLoading = true
                        googleSignInClient.signOut().addOnCompleteListener {
                            launcher.launch(
                                googleSignInClient.signInIntent
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "Continue with Google",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = {
                        isLoading = true
                        auth.signInAnonymously()
                            .addOnSuccessListener {
                                onLoginSuccess()
                            }
                            .addOnFailureListener {
                                isLoading = false
                                errorMessage = it.localizedMessage
                            }
                    }
                ) {
                    Text(
                        "Skip Login",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppThemeColors.textColorPrimary()
                            .copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}
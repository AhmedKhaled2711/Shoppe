package com.lee.shoppe.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lee.shoppe.R
import com.lee.shoppe.ui.viewmodel.AuthenticationViewModel
import kotlinx.coroutines.delay

@Composable
fun SignupScreen(
    onCreateClick: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    viewModel: AuthenticationViewModel = hiltViewModel()
) {
    val uiState by viewModel.signupUiState.collectAsState()
    val context = LocalContext.current

    // Show Toast and navigate to login on signup success
    if (uiState.signupSuccess) {
        LaunchedEffect(uiState.signupSuccess) {
            Toast.makeText(
                context,
                context.getString(R.string.signup_success_message),
                Toast.LENGTH_SHORT
            ).show()
            delay(1000)
            onCreateClick()
            viewModel.clearSignupSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()
        .background(Color.White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg1),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            // Scrollable Form Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { /* Handle skip */ },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        stringResource(R.string.skip).uppercase(),
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                    }

                Box(
                    modifier = Modifier
                        .size(134.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.log),
                        contentDescription = "Logo",
                        modifier = Modifier.size(80.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.create_account),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(R.string.get_started_SignUp),
                    fontSize = 20.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                OutlinedTextField(
                    value = uiState.firstName,
                    onValueChange = { viewModel.onFirstNameChange(it) },
                    label = { Text(stringResource(R.string.f_name)) },
                    leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    isError = uiState.firstNameError
                )
                OutlinedTextField(
                    value = uiState.lastName,
                    onValueChange = { viewModel.onLastNameChange(it) },
                    label = { Text(stringResource(R.string.l_name)) },
                    leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    isError = uiState.lastNameError
                )

                AnimatedVisibility(visible = uiState.firstNameError || uiState.lastNameError) {
                    Text(
                        text = stringResource(R.string.fill_both),
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Start).fillMaxSize()
                    )
                }

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    label = { Text(stringResource(R.string.email)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_email),
                            contentDescription = stringResource(R.string.email)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                    isError = uiState.emailError
                )

                if (uiState.emailError) {
                    Text(
                        text = stringResource(R.string.invalid_email_address),
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                var passwordVisible by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    label = { Text(stringResource(R.string.password)) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_privacy),
                            contentDescription = stringResource(R.string.password)
                        )
                    },
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        val description = if (passwordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                        Icon(
                            imageVector = icon,
                            contentDescription = description,
                            modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    isError = uiState.passwordError
                )

                OutlinedTextField(
                    value = uiState.confirmPassword,
                    onValueChange = { viewModel.onConfirmPasswordChange(it) },
                    label = { Text(stringResource(R.string.confirmPassword)) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_privacy),
                            contentDescription = stringResource(R.string.password)
                        )
                    },
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        val description = if (passwordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                        Icon(
                            imageVector = icon,
                            contentDescription = description,
                            modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    isError = uiState.confirmPasswordError
                )

                AnimatedVisibility(visible = uiState.passwordError || uiState.confirmPasswordError) {
                    Text(
                        text = stringResource(R.string.password_error),
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
            }

            // Bottom Fixed Buttons
            Column(modifier = Modifier.padding(top = 14.dp)) {
                Button(
                    onClick = { viewModel.signup() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF004CFF),
                        contentColor = Color(0xFFF3F3F3)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text(
                        stringResource(R.string.create),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }

                TextButton(
                    onClick = onSignInClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.already_have_account),
                            color = Color.Gray,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.login).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF004CFF),
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}
package com.example.soavertriggertracker.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soavertriggertracker.R
import com.example.soavertriggertracker.ui.theme.SoaverTriggerTrackerTheme
import kotlin.String

object LoginView {

    @Composable
    fun LoginScreen(
        modifier: Modifier = Modifier,
        emailText: String,
        onEmailTextChange: (String) -> Unit,
        onSignInClick: (String) -> Unit,
        signInError: Boolean,
        onSignUpClick: () -> Unit
    ) {
        Surface(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier
                        .padding(10.dp)
                        .padding(vertical = 16.dp)
                        .background(MaterialTheme.colorScheme.primary),
                    text = stringResource(R.string.welcome_title),
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    modifier = Modifier
                        .padding(10.dp)
                        .padding(top = 16.dp)
                        .align(Alignment.Start),
                    text = stringResource(R.string.sign_in_to_continue),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
                if (signInError) {
                    Text(
                        modifier = Modifier
                            .padding(10.dp)
                            .padding(vertical = 6.dp),
                        text = stringResource(R.string.sign_in_fail_text),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                LogInFields(
                    emailText = emailText,
                    onEmailTextChange = onEmailTextChange,
                    onSignInClick = onSignInClick
                )
                Text(
                    modifier = Modifier.padding(top = 24.dp),
                    text = stringResource(R.string.dont_have_an_account),
                    color = MaterialTheme.colorScheme.secondary
                )
                FilledTonalButton(
                    modifier = Modifier
                        .padding(horizontal = 10.dp),
                    onClick = { onSignUpClick() }
                ) {
                    Text(text = stringResource(R.string.sign_up))
                }
            }
        }
    }

    @Composable
    fun LogInFields(
        modifier: Modifier = Modifier,
        emailText: String,
        onEmailTextChange: (String) -> Unit,
        onSignInClick: (String) -> Unit
    ) {
        //do not store password in viewmodel to limit persistence, do not put in Bundle with rememberSavable
        var password by remember { mutableStateOf("") }

        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = emailText,
                    onValueChange = onEmailTextChange,
                    label = { Text(stringResource(R.string.email)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), //enter is next
                    singleLine = true,
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password)) },
                    visualTransformation = PasswordVisualTransformation(), //mask user password
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password, //use password keyboard settings
                        imeAction = ImeAction.Done
                    ), //hide keyboard on enter
                    singleLine = true,
                    keyboardActions = KeyboardActions( //automatically trigger sign in on password done
                        onDone = {
                            onSignInClick(password) //pass password out on submition
                            password = "" //reset password field
                        }
                    )
                )
                Button(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth(),
                    onClick = {
                        onSignInClick(password)
                        password = ""
                    },
                ) { Text(text = stringResource(R.string.sign_in_button)) }
            }
        }
    }
}

@Composable
@Preview
fun LogInPreview() {
    SoaverTriggerTrackerTheme() {
        LoginView.LoginScreen(
            emailText = "",
            onEmailTextChange = {},
            onSignInClick = {},
            signInError = false,
            onSignUpClick = {}
        )
    }
}
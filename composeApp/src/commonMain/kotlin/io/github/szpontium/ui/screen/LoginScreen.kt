package io.github.szpontium.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.szpontium.navigation.CandidateStudent
import io.github.szpontium.viewmodel.LoginEvent
import io.github.szpontium.viewmodel.LoginViewModel
import org.koin.compose.viewmodel.koinViewModel

private val inputShape = RoundedCornerShape(16.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSelectStudents: (List<CandidateStudent>) -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.Success -> onLoginSuccess()
                is LoginEvent.SelectStudents -> onSelectStudents(event.candidates)
                is LoginEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            Text(
                text = "Szpontium",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "E-dziennik nowej generacji",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))

            var selectedTab by remember { mutableIntStateOf(0) }
            val tabTitles = listOf("EduVULCAN", "Dzienniczek", "Librus")

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                tabTitles.forEachIndexed { index, title ->
                    SegmentedButton(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = tabTitles.size),
                        label = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            when (selectedTab) {
                0 -> EduVulcanLoginTab(
                    isLoading = isLoading,
                    onLogin = { login, password -> viewModel.loginWithEduVulcan(login, password) }
                )
                1 -> DzienniczekLoginTab(
                    isLoading = isLoading,
                    onLogin = { token, pin, symbol -> viewModel.loginWithToken(token, pin, symbol) }
                )
                2 -> LibrusLoginTab(
                    isLoading = isLoading,
                    onLogin = { email, password -> viewModel.loginWithLibrus(email, password) }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun EduVulcanLoginTab(
    isLoading: Boolean,
    onLogin: (String, String) -> Unit
) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Login (e-mail)") },
            singleLine = true,
            shape = inputShape,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(inputShape)
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Hasło") },
            singleLine = true,
            shape = inputShape,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(inputShape)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onLogin(login, password) },
            enabled = !isLoading && login.isNotBlank() && password.isNotBlank(),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Zaloguj się",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DzienniczekLoginTab(
    isLoading: Boolean,
    onLogin: (String, String, String) -> Unit
) {
    var token by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var symbol by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = symbol,
            onValueChange = { symbol = it },
            label = { Text("Symbol szkoły") },
            singleLine = true,
            shape = inputShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier
                .fillMaxWidth()
                .clip(inputShape)
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("Token") },
            singleLine = true,
            shape = inputShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier
                .fillMaxWidth()
                .clip(inputShape)
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it },
            label = { Text("PIN") },
            singleLine = true,
            shape = inputShape,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(inputShape)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Token i PIN znajdziesz w zakładce Dostęp mobilny w module Uczeń.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onLogin(token, pin, symbol) },
            enabled = !isLoading && token.isNotBlank() && pin.isNotBlank() && symbol.isNotBlank(),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Zarejestruj urządzenie",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LibrusLoginTab(
    isLoading: Boolean,
    onLogin: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text(
                text = "Zaloguj się danymi do Konta LIBRUS (portal.librus.pl). Jeśli posiadasz login Synergia (np. 1234567u), powiąż go najpierw na stronie portalu.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp)
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail (Konto LIBRUS)") },
            singleLine = true,
            shape = inputShape,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(inputShape)
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Hasło") },
            singleLine = true,
            shape = inputShape,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(inputShape)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onLogin(email, password) },
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Zaloguj przez Librus",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

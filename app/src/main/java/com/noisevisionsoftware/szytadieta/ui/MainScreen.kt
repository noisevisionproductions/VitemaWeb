package com.noisevisionsoftware.szytadieta.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.ui.navigation.Screen
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.DashboardScreen
import com.noisevisionsoftware.szytadieta.ui.screens.loginAndRegister.AuthViewModel
import com.noisevisionsoftware.szytadieta.ui.screens.loginAndRegister.ForgotPassword
import com.noisevisionsoftware.szytadieta.ui.screens.loginAndRegister.LoginScreen
import com.noisevisionsoftware.szytadieta.ui.screens.loginAndRegister.RegisterScreen
import com.noisevisionsoftware.szytadieta.ui.screens.profile.CompleteProfileScreen
import com.noisevisionsoftware.szytadieta.ui.screens.profile.ProfileViewModel
import com.noisevisionsoftware.szytadieta.ui.screens.splash.SplashScreen
import com.noisevisionsoftware.szytadieta.ui.screens.weight.WeightScreen

@Composable
fun MainScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
    val authState by authViewModel.authState.collectAsState()
    val userSession by authViewModel.userSession.collectAsState(initial = null)
    val profileState by profileViewModel.profileState.collectAsState()

    LaunchedEffect(profileState) {
        if (profileState is ProfileViewModel.ProfileState.Success && !profileViewModel.profileUpdateMessageShown) {
            profileViewModel.profileUpdateMessageShown = true
            currentScreen = Screen.Dashboard
        }
    }

    BackHandler {
        when (currentScreen) {
            Screen.Dashboard -> {}
            Screen.CompleteProfile -> {
                currentScreen = Screen.Dashboard
            }

            Screen.Weight -> {
                currentScreen = Screen.Dashboard
            }

            else -> {
                if (userSession != null || authState is AuthViewModel.AuthState.Success) {
                    currentScreen = Screen.Dashboard
                } else if (currentScreen !is Screen.Login) {
                    currentScreen = Screen.Login
                }
            }
        }
    }

    LaunchedEffect(currentScreen) {
        authViewModel.resetUiEvent()
        profileViewModel.resetUiEvent()
    }

    LaunchedEffect(authState, userSession) {
        when {
            userSession != null || authState is AuthViewModel.AuthState.Success -> {
                profileViewModel.checkProfileCompletion().collect { isCompleted ->
                    currentScreen = if (!isCompleted) {
                        Screen.CompleteProfile
                    } else {
                        Screen.Dashboard
                    }
                }
            }

            authState is AuthViewModel.AuthState.LoggedOut -> {
                currentScreen = Screen.Login
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            authState is AuthViewModel.AuthState.Initial -> {
                SplashScreen()
            }

            else -> {
                when (currentScreen) {
                    Screen.Login -> {
                        LoginScreen(
                            onLoginClick = { email, password ->
                                authViewModel.login(email, password)
                            },
                            onRegistrationClick = { currentScreen = Screen.Register },
                            onForgotPasswordClick = { currentScreen = Screen.ForgotPassword }
                        )
                    }

                    Screen.Register -> {
                        RegisterScreen(
                            onRegisterClick = { nickname, email, password, confirmPassword ->
                                authViewModel.register(nickname, email, password, confirmPassword)
                            },
                            onLoginClick = { currentScreen = Screen.Login },
                            onRegulationsClick = { },
                            onPrivacyPolicyClick = { }
                        )
                    }

                    Screen.ForgotPassword -> {
                        ForgotPassword(
                            onBackToLogin = { currentScreen = Screen.Login }
                        )
                    }

                    Screen.CompleteProfile -> {
                        CompleteProfileScreen(
                            onSkip = { currentScreen = Screen.Dashboard },
                            isLoading = profileState is ProfileViewModel.ProfileState.Loading
                        )
                    }

                    Screen.Dashboard -> {
                        DashboardScreen(
                            onLogoutClick = { authViewModel.logout() },
                            onMealPlanClick = {},
                            onCaloriesTrackerClick = {},
                            onWaterTrackerClick = {},
                            onRecipesClick = {},
                            onProgressClick = { currentScreen = Screen.Weight },
                            onSettingsClick = {}
                        )
                    }

                    Screen.Weight -> {
                        WeightScreen(
                            onBackClick = { currentScreen = Screen.Dashboard }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = authState is AuthViewModel.AuthState.Loading ||
                    profileState is ProfileViewModel.ProfileState.Loading,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
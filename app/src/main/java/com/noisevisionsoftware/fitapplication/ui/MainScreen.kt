package com.noisevisionsoftware.fitapplication.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.noisevisionsoftware.fitapplication.ui.navigation.Screen
import com.noisevisionsoftware.fitapplication.ui.screens.dashboard.DashboardScreen
import com.noisevisionsoftware.fitapplication.ui.screens.loginAndRegister.LoginScreen
import com.noisevisionsoftware.fitapplication.ui.screens.loginAndRegister.RegisterScreen

@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

    when (currentScreen) {
        is Screen.Login -> {
            LoginScreen(
                onLoginClick = { email, password ->
                    currentScreen = Screen.Dashboard
                },
                onRegistrationClick = { currentScreen = Screen.Register },
                onForgotPasswordClick = { currentScreen = Screen.ForgotPassword },
            )
        }

        is Screen.Register -> {
            RegisterScreen(
                onRegisterClick = { nickname, email, password, confirmPassword ->
                    currentScreen = Screen.Dashboard
                },
                onLoginClick = { currentScreen = Screen.Login },
                onRegulationsClick = { },
                onPrivacyPolicyClick = { }
            )
        }

        is Screen.ForgotPassword -> {

        }

        is Screen.Dashboard -> {
            DashboardScreen(
                onMealPlanClick = {},
                onCaloriesTrackerClick = {},
                onWaterTrackerClick = {},
                onRecipesClick = {},
                onProgressClick = {},
                onSettingsClick = {}
            )
        }
    }
}
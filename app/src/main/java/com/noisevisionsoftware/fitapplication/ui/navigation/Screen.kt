package com.noisevisionsoftware.fitapplication.ui.navigation

sealed class Screen {
    data object Login : Screen()
    data object Register : Screen()
    data object ForgotPassword : Screen()
    data object Dashboard : Screen()
}
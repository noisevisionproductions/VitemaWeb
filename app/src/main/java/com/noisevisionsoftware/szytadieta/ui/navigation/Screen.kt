package com.noisevisionsoftware.szytadieta.ui.navigation

sealed class Screen {
    data object Login : Screen()
    data object Register : Screen()
    data object ForgotPassword : Screen()
    data object Dashboard : Screen()
    data object Weight : Screen()
    data object CompleteProfile : Screen()
}
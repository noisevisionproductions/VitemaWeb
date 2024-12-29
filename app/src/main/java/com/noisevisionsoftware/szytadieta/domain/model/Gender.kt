package com.noisevisionsoftware.szytadieta.domain.model

enum class Gender {
    MALE, FEMALE, OTHER;

    companion object {
        fun fromString(value: String): Gender {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                OTHER
            }
        }
    }
}
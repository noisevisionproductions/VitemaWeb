package com.noisevisionsoftware.szytadieta.domain.model

import java.util.UUID

data class Weight(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val weight: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val note: String = ""
)
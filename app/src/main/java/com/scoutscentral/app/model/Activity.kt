package com.scoutscentral.app.model

data class Activity(
    val id: String,
    var title: String,
    var date: String,
    var location: String,
    val materials: List<String> = emptyList(),
    var description: String,
    var imageUrl: String? = null
)

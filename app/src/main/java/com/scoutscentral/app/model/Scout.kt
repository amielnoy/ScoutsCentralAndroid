package com.scoutscentral.app.model

data class Scout(
    val id: String,
    var name: String,
    var avatarUrl: String? = null,
    var level: ScoutLevel,
    var contact: String,
    var interests: String = "",
    var skills: String = "",
    var activityHistory: List<String> = emptyList()
)

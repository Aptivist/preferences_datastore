package com.aptivist.preferences.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class AppPreferences(val name: String? = null)
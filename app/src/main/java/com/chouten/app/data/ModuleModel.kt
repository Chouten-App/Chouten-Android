package com.chouten.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModuleModel(
    val name: String,
    val author: String,
    val version: String,
    val js: String,
    val image: String?,
    @SerialName("callsApi") val usesExternalApi: Boolean?,
    val website: String,
    val backgroundColor: String?,
    val foregroundColor: String?
)

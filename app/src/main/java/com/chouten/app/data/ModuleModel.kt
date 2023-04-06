package com.chouten.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModuleModel(
    var id: Int?, // The ID will be set when we load a new module. It is not
    // something that needs to be defined within the JSON
    val type: String, // TODO: Better checking to only allow for Source/Meta module types
    val subtypes: List<String>,
    val name: String,
    val version: String,
    val updateUrl: String,
    @SerialName("metadata") val meta: ModuleMetaData,
    val code: Map<String, Map<String, ModuleCode>>
) {

    @Serializable
    data class ModuleMetaData(
        val author: String,
        val description: String,
        val icon: String,
        val lang: List<String>,
        @SerialName("baseURL") val baseUrl: String,
        @SerialName("bgColor") val backgroundColor: String,
        @SerialName("fgColor") val foregroundColor: String,
    )

    @Serializable
    data class ModuleCode(
        val url: String,
        val mediaUrl: String?,
        val usesApi: Boolean?,
        val allowExternalScripts: Boolean,
        val removeScripts: Boolean,
        val js: String,
        @SerialName("mediaJs") val mediaJS: String?
    )

    override fun hashCode(): Int {
        return (
            this.type.hashCode()
            + this.subtypes.hashCode()
            + this.name.hashCode()
            + this.version.hashCode()
            + this.updateUrl.hashCode()
            + this.meta.hashCode()
            + this.code.hashCode()
        )
    }
}

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
    val code: Map<String, ModuleCode>
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
        val search: List<ModuleCodeblock>,
        val info: List<ModuleCodeblock>
    ) {
        @Serializable
        data class ModuleCodeblock(
            val request: ModuleRequest,
            val javascript: ModuleJavascriptOpts
        ) {
            @Serializable
            data class ModuleRequest(
                val url: String, // Left blank if we wish to reuse the last url
                val type: String, // "POST", "GET", "PUT" and "DELETE"
                val headers: List<ModuleKVPair>,
                val body: String? // The body of the Request
            )

            @Serializable
            data class ModuleJavascriptOpts(
                val code: String,
                val removeScripts: Boolean,
                val allowExternalScripts: Boolean
            )

            @Serializable
            data class ModuleKVPair(
                val key: String,
                val value: String
            )
        }
    }

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

@Serializable
data class ModuleResponse<T>(
    val result: T, val nextUrl: String? = ""
)
@Serializable
data class SearchResult(
    val url: String,
    val img: String,
    val title: String,
    val indicatorText: String?,
    val currentCount: Int?,
    val totalCount: Int?,
)

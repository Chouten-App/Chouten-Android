package com.chouten.app.data

import com.chouten.app.Mapper
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.json.*

@Serializable
data class ModuleModel(
    var id: String, // The ID provided by the module
    // something that needs to be defined within the JSON
    val type: String,
    val subtypes: List<String>,
    var name: String,
    val version: String,
    val formatVersion: Int,
    val updateUrl: String,
    @SerialName("general") val meta: ModuleMetaData,
    var code: Map<String, ModuleCode>?
) {
    @Serializable
    data class ModuleMetaData(
        val author: String,
        val description: String,
        var icon: String?,
        val lang: List<String>,
        @SerialName("baseURL") val baseUrl: String,
        @SerialName("bgColor") val backgroundColor: String,
        @SerialName("fgColor") val foregroundColor: String,
    ) {
        override fun toString(): String {
            return "{\"author\": \"$author\", \"description\": \"$description\", \"icon\": \"$icon\", \"lang\": ${
                lang.map {
                    "\"$it\""
                }
            }, \"baseURL\": \"$baseUrl\", \"bgColor\": \"$backgroundColor\", \"fgColor\": \"$foregroundColor\"}"
        }
    }

    override fun toString(): String {
        return "{\"id\": \"$id\", \"type\": \"${type}\", \"subtypes\": ${
            subtypes.map {
                "\"${it}\""
            }
        }, \"name\": \"$name\", \"version\": \"$version\", \"formatVersion\": $formatVersion, \"updateUrl\": \"$updateUrl\", \"general\": $meta}"
    }

    @Serializable
    data class ModuleCode(
        val home: List<ModuleCodeblock> = listOf(),
        val search: List<ModuleCodeblock> = listOf(),
        val info: List<ModuleCodeblock> = listOf(),
        val mediaConsume: List<ModuleCodeblock> = listOf(),
    ) {
        @Serializable
        data class ModuleCodeblock(
            val imports: List<String>? = listOf(),
            // not set within the JSON
            var code: String,
        )

        override fun toString(): String {
            return "{\"home\": $home, \"search\": $search, \"info\": $info, \"mediaConsume\": $mediaConsume}"
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
                )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModuleModel

        if (id != other.id) return false
        if (type != other.type) return false
        if (subtypes != other.subtypes) return false
        if (name != other.name) return false
        if (version != other.version) return false
        if (updateUrl != other.updateUrl) return false
        return meta == other.meta
    }
}

@Serializable
data class ModuleResponse<T>(
    val result: T, val action: String? = ""
)

// TODO: make the action field static (should be action = "error")
// it doesn't stringify correctly when that's the case
@Serializable
data class ErrorAction(
    val action: String,
    val result: String
)

@Serializable
data class HTTPAction(
    val reqId: String,
    val responseText: String
)

@Serializable
data class ModuleAction(
    val action: String? = ""
)

@Serializable
data class HomeResult(
    val type: String, // "Carousel", "list", "grid_2x", "..."
    val title: String, // "Spotlight", "Recently released", "..."
    val data: List<HomeItem>
) {
    @Serializable
    data class HomeItem(
        val url: String,
        val image: String,
        val titles: Map<String, String>,
        val indicator: String?,
        val current: Int?,
        val total: Int?,
        val subtitle: String?,
        val subtitleValue: List<String>,
        val buttonText: String?,
        val showIcon: Boolean? = false,
        val iconText: String?,
    )
}

@Serializable
data class SearchResult(
    val url: String,
    val img: String,
    val title: String,
    val indicatorText: String?,
    val currentCount: Int?,
    val totalCount: Int?,
)

@Serializable
data class WebviewPayload(
    val query: String,
    val action: String
)

@Serializable
data class BasePayload(
    val reqId: String?,
    val action: String,
    val payload: String
)

@Serializable
data class HomepagePayload(
    val action: String,
)

@Serializable
data class InfoResult(
    val id: String?,
    val titles: Titles,
    val epListURLs: List<String>,
    val altTitles: List<String>?,
    val description: String,
    val poster: String,
    val banner: String?,
    val status: String?,
    val totalMediaCount: Int?,
    val mediaType: String,
    val seasons: List<Season>?,
    val mediaList: List<MediaListItem>?,
) {
    @Serializable
    data class MediaListItem(
        val title: String,
        val list: List<MediaItem>
    )

    @Serializable
    data class Titles(
        val primary: String,
        val secondary: String?
    )

    @Serializable
    data class MediaItem(
        val url: String,
        val number: Float?,
        val title: String?,
        val description: String?,
        val image: String?,
    ) {
        override fun toString(): String{
            // TODO: refactor
            val map = MediaItem(
                url = url,
                number = number,
                title = title,
                description = description,
                image = image
            )

            return Mapper.json.encodeToString(MediaItem.serializer(), map);
        }
    }

    @Serializable
    data class Season(
        val name: String,
        val url: String,
    )
}

@Serializable
data class WatchResult(
    val sources: List<Source>,
    val subtitles: List<Subtitles>,
    val skips: List<SkipTimes>,
    val headers: Map<String, String>,
) {

    @Serializable
    data class ServerData(
        val title: String,
        val list: List<Server>
    )

    @Serializable
    data class Server(
        val name: String,
        val url: String,
    )

    @Serializable
    data class Source(
        val file: String,
        val type: String,
    )

    @Serializable
    data class Subtitles(
        val url: String,
        val language: String,
    )

    @Serializable
    data class SkipTimes(
        val start: Double,
        val end: Double,
        val type: String
    )
}
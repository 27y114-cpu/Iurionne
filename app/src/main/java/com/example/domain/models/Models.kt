package com.example.domain.models

import com.squareup.moshi.JsonClass

enum class WorkType { SERIES, FILMES, ANIMES, LIVROS, HQS }

enum class WorkStatus { 
    EM_PRODUCAO, 
    EM_ANDAMENTO, 
    HIATO, 
    CANCELADA, 
    CONCLUIDA 
}

enum class UserStatus { 
    PLANEJANDO, 
    LENDO, 
    ASSISTINDO, 
    PAUSADO, 
    ABANDONADO, 
    CONCLUIDO, 
    RELENDO, 
    REASSISTINDO 
}

@JsonClass(generateAdapter = true)
data class Title(
    val language: String,
    val title: String,
    val pinned: Boolean
)

@JsonClass(generateAdapter = true)
data class Link(
    val language: String,
    val label: String,
    val url: String,
    val type: String // Leitura, Streaming, Compra, Informação, Outro
)

// Series & Animes
@JsonClass(generateAdapter = true)
data class SeriesData(
    val seasons: List<Season> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Season(
    val number: Int,
    val title: String,
    val description: String,
    val episodes: List<Episode> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Episode(
    val number: Int,
    val title: String,
    val watched: Boolean = false,
    val watchCount: Int = 0,
    val rating: Int = 0, // 1..5, 0 = unrated
    val notes: String = ""
)

// Movies
@JsonClass(generateAdapter = true)
data class MovieData(
    val franchise: String = "",
    val movies: List<MovieItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MovieItem(
    val number: Int,
    val title: String = "",
    val watched: Boolean = false,
    val watchCount: Int = 0,
    val rating: Int = 0,
    val notes: String = ""
)

// Books
@JsonClass(generateAdapter = true)
data class BookData(
    val seriesName: String = "",
    val volumes: List<BookVolume> = emptyList()
)

@JsonClass(generateAdapter = true)
data class BookVolume(
    val volumeNumber: Int,
    val title: String = "",
    val partName: String = "",
    val chapters: List<BookChapter> = emptyList()
)

@JsonClass(generateAdapter = true)
data class BookChapter(
    val number: Int,
    val title: String = "",
    val read: Boolean = false,
    val readCount: Int = 0,
    val rating: Int = 0,
    val notes: String = ""
)

// HQs (Mangás, Manhwas, Comics, etc.)
@JsonClass(generateAdapter = true)
data class HqData(
    val seriesName: String = "",
    val subtype: String = "Mangá", // Mangá, Manhwa, Manhua, Comic, HQ
    val volumes: List<HqVolume> = emptyList()
)

@JsonClass(generateAdapter = true)
data class HqVolume(
    val volumeNumber: Int,
    val title: String = "",
    val chapters: List<HqChapter> = emptyList()
)

@JsonClass(generateAdapter = true)
data class HqChapter(
    val number: Int,
    val title: String = "",
    val read: Boolean = false,
    val readCount: Int = 0,
    val rating: Int = 0,
    val notes: String = ""
)

@JsonClass(generateAdapter = true)
data class Work(
    val id: String,
    val type: WorkType,
    val coverPath: String? = null,
    val description: String = "",
    val publicationDate: String = "",
    val status: WorkStatus = WorkStatus.EM_ANDAMENTO,
    val userStatus: UserStatus = UserStatus.PLANEJANDO,
    val userRating: Int = 0, // 0..5
    val favorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val titles: List<Title> = emptyList(),
    val links: List<Link> = emptyList(),
    val seriesData: SeriesData? = null,
    val movieData: MovieData? = null,
    val bookData: BookData? = null,
    val hqData: HqData? = null
) {
    val calculatedRating: Double
        get() {
            val ratings = mutableListOf<Int>()
            when (type) {
                WorkType.SERIES, WorkType.ANIMES -> {
                    seriesData?.seasons?.flatMap { it.episodes }?.forEach { ep ->
                        if (ep.rating > 0) ratings.add(ep.rating)
                    }
                }
                WorkType.FILMES -> {
                    movieData?.movies?.forEach { movie ->
                        if (movie.rating > 0) ratings.add(movie.rating)
                    }
                }
                WorkType.LIVROS -> {
                    bookData?.volumes?.flatMap { it.chapters }?.forEach { ch ->
                        if (ch.rating > 0) ratings.add(ch.rating)
                    }
                }
                WorkType.HQS -> {
                    hqData?.volumes?.flatMap { it.chapters }?.forEach { ch ->
                        if (ch.rating > 0) ratings.add(ch.rating)
                    }
                }
            }
            return if (ratings.isEmpty()) 0.0 else ratings.average()
        }

    fun getPinnedTitle(): String {
        return titles.find { it.pinned }?.title ?: titles.firstOrNull()?.title ?: "Sem Título"
    }
}

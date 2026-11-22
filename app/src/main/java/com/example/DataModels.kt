package com.example

import com.google.gson.annotations.SerializedName

// --- Bible API Data Models ---

// Model for web library like bible-api.com (KJV)
data class KjvResponse(
    @SerializedName("reference") val reference: String = "",
    @SerializedName("verses") val verses: List<KjvVerse> = emptyList(),
    @SerializedName("text") val text: String = "",
    @SerializedName("translation_id") val translationId: String = "",
    @SerializedName("translation_name") val translationName: String = ""
)

data class KjvVerse(
    @SerializedName("book_id") val bookId: String = "",
    @SerializedName("book_name") val bookName: String = "",
    @SerializedName("chapter") val chapter: Int = 0,
    @SerializedName("verse") val verse: Int = 0,
    @SerializedName("text") val text: String = ""
)

// Model for Indonesian Alkitab api (alkitab-api.vercel.app or similar)
data class TbResponse(
    @SerializedName("passage") val passage: String? = null,
    @SerializedName("verses") val verses: List<TbVerse>? = null,
    @SerializedName("book") val book: TbBookInfo? = null,
    @SerializedName("chapter") val chapter: TbChapterInfo? = null
)

data class TbBookInfo(
    @SerializedName("no") val no: Int = 0,
    @SerializedName("name") val name: String = ""
)

data class TbChapterInfo(
    @SerializedName("no") val no: Int = 0
)

data class TbVerse(
    @SerializedName("verse") val verse: String = "",
    @SerializedName("text") val text: String = ""
)


// --- Gemini Deep-Dive Theology Analysis Data Models ---

data class AnalysisResult(
    @SerializedName("timeline") val timeline: String = "",
    @SerializedName("komparasi_manuskrip") val komparasi_manuskrip: String = "",
    @SerializedName("sejarah_latar_belakang") val sejarah_latar_belakang: String = "",
    @SerializedName("analogi_modern") val analogi_modern: String = "",
    @SerializedName("bahasa_asli_data") val bahasa_asli_data: List<OriginalLanguageWord> = emptyList(),
    @SerializedName("kristosentris") val kristosentris: String = "",
    @SerializedName("cross_reference") val cross_reference: String = "",
    @SerializedName("apologetika_corner") val apologetika_corner: String = "",
    @SerializedName("renungan_praktis") val renungan_praktis: String = "",
    @SerializedName("teks_tb_alkitab") val teksTbAlkitab: String = "",
    @SerializedName("teks_kjv_alkitab") val teksKjvAlkitab: String = ""
)

data class OriginalLanguageWord(
    @SerializedName("kata_tb") val kataTb: String = "",
    @SerializedName("kata_kjv") val kataKjv: String = "",
    @SerializedName("kata_asli") val kataAsli: String = "",
    @SerializedName("strong_id") val strongId: String = "",
    @SerializedName("makna") val makna: String = ""
)


// --- Google Sheets Post Request Payload ---

data class SheetPostPayload(
    @SerializedName("sheetUrl") val sheetUrl: String = "",
    @SerializedName("timestamp") val timestamp: String = "",
    @SerializedName("reference") val reference: String = "",
    @SerializedName("textTb") val textTb: String = "",
    @SerializedName("textKjv") val textKjv: String = "",
    @SerializedName("timeline") val timeline: String = "",
    @SerializedName("komparasi_manuskrip") val komparasi_manuskrip: String = "",
    @SerializedName("sejarah_latar_belakang") val sejarah_latar_belakang: String = "",
    @SerializedName("analogi_modern") val analogi_modern: String = "",
    @SerializedName("bahasa_asli") val bahasaAsliJson: String = "",
    @SerializedName("kristosentris") val kristosentris: String = "",
    @SerializedName("cross_reference") val cross_reference: String = "",
    @SerializedName("apologetika_corner") val apologetika_corner: String = "",
    @SerializedName("renungan_praktis") val renungan_praktis: String = ""
)

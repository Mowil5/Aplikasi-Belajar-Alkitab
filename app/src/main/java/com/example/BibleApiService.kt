package com.example

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

// Interface for KJV bible verses (bible-api.com)
interface BibleApiService {
    @GET("{passage}")
    suspend fun getKjvVerse(
        @Path("passage") passage: String
    ): KjvResponse
}

// Interface for Indonesian Alkitab (alkitab-api.vercel.app or alternative)
interface AlkitabApiService {
    @GET("api/passage")
    suspend fun getTbVerse(
        @Query("passage") passage: String
    ): TbResponse
}

// Interface for bolls.life (Bolls API)
interface BollsBibleService {
    @GET("get-text/{translation}/{book}/{chapter}/")
    suspend fun getChapter(
        @Path("translation") translation: String,
        @Path("book") book: Int,
        @Path("chapter") chapter: Int
    ): List<BollsVerse>
}

data class BollsVerse(
    val pk: Int,
    val verse: Int,
    val text: String
)

// Interface for rest.api.bible
interface ApiBibleService {
    @GET("v1/bibles/{bibleId}/passages/{passageId}")
    suspend fun getPassage(
        @Path("bibleId") bibleId: String,
        @Path("passageId") passageId: String,
        @Query("content-type") contentType: String = "text",
        @Query("include-notes") includeNotes: Boolean = false,
        @Query("include-titles") includeTitles: Boolean = false,
        @Query("include-chapter-numbers") includeChapterNumbers: Boolean = false,
        @Query("include-verse-numbers") includeVerseNumbers: Boolean = true,
        @retrofit2.http.Header("api-key") apiKey: String
    ): ApiBibleResponse
}

data class ApiBibleResponse(
    val data: ApiBibleData?
)

data class ApiBibleData(
    val id: String,
    val bibleId: String,
    val content: String,
    val reference: String
)

// Interface for sending parsed theology results to Google Sheet Web App (Google Apps Script)
interface GoogleSheetApiService {
    @POST
    suspend fun postToSheet(
        @Url url: String,
        @Body payload: SheetPostPayload
    ): Response<ResponseBody>
}

// Direct Gemini REST API models
data class GeminiRestRequest(
    val contents: List<GeminiRestContent>,
    val generationConfig: GeminiRestGenerationConfig? = null,
    val systemInstruction: GeminiRestContent? = null
)

data class GeminiRestContent(
    val parts: List<GeminiRestPart>
)

data class GeminiRestPart(
    val text: String
)

data class GeminiRestGenerationConfig(
    val responseMimeType: String? = null
)

data class GeminiRestResponse(
    val candidates: List<GeminiRestCandidate>?
)

data class GeminiRestCandidate(
    val content: GeminiRestContent?
)

interface GeminiRestApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRestRequest
    ): GeminiRestResponse
}

// Retrofit Client Orchestrator
object NetworkClient {
    private const val BIBLE_API_BASE_URL = "https://bible-api.com/"
    private const val ALKITAB_API_BASE_URL = "https://alkitab-api.vercel.app/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val bibleService: BibleApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BIBLE_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BibleApiService::class.java)
    }

    val alkitabService: AlkitabApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ALKITAB_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AlkitabApiService::class.java)
    }

    val googleSheetService: GoogleSheetApiService by lazy {
        Retrofit.Builder()
            // Placeholder base url as we use @Url with full path for Google Sheet call
            .baseUrl("https://script.google.com/") 
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleSheetApiService::class.java)
    }

    val geminiRestService: GeminiRestApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/") 
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiRestApiService::class.java)
    }

    val apiBibleService: ApiBibleService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.scripture.api.bible/") 
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiBibleService::class.java)
    }

    val bollsBibleService: BollsBibleService by lazy {
        Retrofit.Builder()
            .baseUrl("https://bolls.life/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BollsBibleService::class.java)
    }

    fun bookToUsfm(bookName: String): String {
        val b = bookName.lowercase().replace(" ", "").replace("-", "").trim()
        return when (b) {
            "kejadian", "kej", "genesis", "gen" -> "GEN"
            "keluaran", "kel", "exodus", "exo", "exd" -> "EXO"
            "imamat", "im", "leviticus", "lev" -> "LEV"
            "bilangan", "bil", "numbers", "num" -> "NUM"
            "ulangan", "ul", "deuteronomy", "deu" -> "DEU"
            "yosua", "yos", "joshua", "jos" -> "JOS"
            "hakim-hakim", "hakim", "judges", "jdg" -> "JDG"
            "rut", "ruth" -> "RUT"
            "1samuel", "1sam", "1sa" -> "1SA"
            "2samuel", "2sam", "2sa" -> "2SA"
            "1raja-raja", "1raja", "1raj", "1kings", "1ki" -> "1KI"
            "2raja-raja", "2raja", "2raj", "2kings", "2ki" -> "2KI"
            "1tawarikh", "1taw", "1chronicles", "1ch" -> "1CH"
            "2tawarikh", "2taw", "2chronicles", "2ch" -> "2CH"
            "ezra", "ezr" -> "EZR"
            "nehemia", "neh", "nehemiah" -> "NEH"
            "ester", "est", "esther" -> "EST"
            "ayub", "job", "ayb" -> "JOB"
            "mazmur", "maz", "psalms", "ps", "psa" -> "PSA"
            "amsal", "ams", "proverbs", "pro" -> "PRO"
            "pengkhotbah", "peng", "ecclesiastes", "ecc" -> "ECC"
            "kidungagung", "kidung", "songofsolomon", "sng", "sos" -> "SNG"
            "yesaya", "yes", "isaiah", "isa" -> "ISA"
            "yeremia", "yer", "jeremiah", "jer" -> "JER"
            "ratapan", "rat", "lamentations", "lam" -> "LAM"
            "yehezkiel", "yeh", "ezekiel", "ezk" -> "EZK"
            "daniel", "dan" -> "DAN"
            "hosea", "hos" -> "HOS"
            "yoel", "joel", "jol" -> "JOL"
            "amos", "amo" -> "AMO"
            "obaja", "ob", "obadiah", "oba" -> "OBA"
            "yunus", "jonah", "jon", "yun" -> "JON"
            "mikha", "micah", "mic", "mik" -> "MIC"
            "nahum", "nah", "nam" -> "NAM"
            "habakuk", "habakkuk", "hab" -> "HAB"
            "zefanya", "zef", "zephaniah", "zep" -> "ZEP"
            "hagai", "haggai", "hag" -> "HAG"
            "zakharia", "zechariah", "zak", "zec" -> "ZEC"
            "maleakhi", "malachi", "mal" -> "MAL"
            "matius", "mat", "matthew" -> "MAT"
            "markus", "mar", "mark", "mrk" -> "MRK"
            "lukas", "luk", "luke" -> "LUK"
            "yohanes", "yoh", "john", "jhn" -> "JHN"
            "kisahpararasul", "kisah", "kpr", "acts", "act" -> "ACT"
            "roma", "rom", "romans" -> "ROM"
            "1korintus", "1kor", "1corinthians", "1co" -> "1CO"
            "2korintus", "2kor", "2corinthians", "2co" -> "2CO"
            "galatia", "gal", "galatians" -> "GAL"
            "efesus", "efe", "ephesians", "eph" -> "EPH"
            "filipi", "fil", "philippians", "php" -> "PHP"
            "kolose", "kol", "colossians", "col" -> "COL"
            "1tesalonika", "1tes", "1thessalonians", "1th" -> "1TH"
            "2tesalonika", "2tes", "2thessalonians", "2th" -> "2TH"
            "1timotius", "1tim", "1timothy", "1ti" -> "1TI"
            "2timotius", "2tim", "2timothy", "2ti" -> "2TI"
            "titus", "tit" -> "TIT"
            "filemon", "film", "philemon", "phm" -> "PHM"
            "ibrani", "ibr", "hebrews", "heb" -> "HEB"
            "yakobus", "yak", "james", "jas" -> "JAS"
            "1petrus", "1pet", "1peter", "1pe" -> "1PE"
            "2petrus", "2pet", "2peter", "2pe" -> "2PE"
            "1yohanes", "1yoh", "1john", "1jn" -> "1JN"
            "2yohanes", "2yoh", "2john", "2jn" -> "2JN"
            "3yohanes", "3yoh", "3john", "3jn" -> "3JN"
            "yudas", "yud", "jude", "jud" -> "JUD"
            "wahyu", "wah", "revelation", "rev" -> "REV"
            else -> bookName.uppercase().take(3)
        }
    }

    fun toApiBiblePassageId(inputPassage: String): String {
        val trimmed = inputPassage.trim()
        val m = Regex("^(\\d*\\s*[a-zA-Z]+)\\s*(\\d+)[\\s*:\\s*](\\d+)(?:\\s*-\\s*(\\d+))?.*$").find(trimmed)
        if (m != null) {
            val usfmBook = bookToUsfm(m.groupValues[1])
            val chapter = m.groupValues[2]
            val startVerse = m.groupValues[3]
            val endVerse = m.groupValues[4]
            return if (endVerse.isNotEmpty()) {
                "$usfmBook.$chapter.$startVerse-$usfmBook.$chapter.$endVerse"
            } else {
                "$usfmBook.$chapter.$startVerse"
            }
        }
        val chapterOnlyMatch = Regex("^(\\d*\\s*[a-zA-Z]+)\\s*(\\d+).*$").find(trimmed)
        if (chapterOnlyMatch != null) {
            val usfmBook = bookToUsfm(chapterOnlyMatch.groupValues[1])
            val chapter = chapterOnlyMatch.groupValues[2]
            return "$usfmBook.$chapter"
        }
        return trimmed
    }

    // Book Naming Normalizer: maps common Indonesian biblical books to their standard English name
    // used to communicate with the english-only bible-api.com
    fun normalizeBookToEnglish(inputPassage: String): String {
        val trimmed = inputPassage.trim()
        val m = Regex("^(\\d*\\s*[a-zA-Z]+)\\s*(\\d+.*)$").find(trimmed)
        if (m != null) {
            val bookPart = m.groupValues[1].lowercase().replace(" ", "")
            val restPart = m.groupValues[2]
            val englishBook = when (bookPart) {
                "kejadian", "kej" -> "Genesis"
                "keluaran", "kel" -> "Exodus"
                "imamat", "im" -> "Leviticus"
                "bilangan", "bil" -> "Numbers"
                "ulangan", "ul" -> "Deuteronomy"
                "yosua", "yos" -> "Joshua"
                "hakim-hakim", "hakim" -> "Judges"
                "rut" -> "Ruth"
                "1samuel", "1sam" -> "1 Samuel"
                "2samuel", "2sam" -> "2 Samuel"
                "1raja-raja", "1raja", "1raj" -> "1 Kings"
                "2raja-raja", "2raja", "2raj" -> "2 Kings"
                "1tawarikh", "1taw" -> "1 Chronicles"
                "2tawarikh", "2taw" -> "2 Chronicles"
                "ezra" -> "Ezra"
                "nehemia", "neh" -> "Nehemiah"
                "ester", "est" -> "Esther"
                "ayub" -> "Job"
                "mazmur", "maz", "ps" -> "Psalms"
                "amsal", "ams" -> "Proverbs"
                "pengkhotbah", "peng" -> "Ecclesiastes"
                "kidungagung", "kidung" -> "Song of Solomon"
                "yesaya", "yes" -> "Isaiah"
                "yeremia", "yer" -> "Jeremiah"
                "ratapan", "rat" -> "Lamentations"
                "yehezkiel", "yeh" -> "Ezekiel"
                "daniel", "dan" -> "Daniel"
                "hosea", "hos" -> "Hosea"
                "yoel" -> "Joel"
                "amos" -> "Amos"
                "obaja", "ob" -> "Obadiah"
                "yunus", "yun" -> "Jonah"
                "mikha", "mik" -> "Micah"
                "nahum", "nah" -> "Nahum"
                "habakuk", "hab" -> "Habakkuk"
                "zefanya", "zef" -> "Zephaniah"
                "hagai", "hag" -> "Haggai"
                "zakharia", "zak" -> "Zechariah"
                "maleakhi", "mal" -> "Malachi"
                "matius", "mat" -> "Matthew"
                "markus", "mar" -> "Mark"
                "lukas", "luk" -> "Luke"
                "yohanes", "yoh" -> "John"
                "kisahpararasul", "kisah", "kpr" -> "Acts"
                "roma", "rom" -> "Romans"
                "1korintus", "1kor" -> "1 Corinthians"
                "2korintus", "2kor" -> "2 Corinthians"
                "galatia", "gal" -> "Galatians"
                "efesus", "efe" -> "Ephesians"
                "filipi", "fil" -> "Philippians"
                "kolose", "kol" -> "Colossians"
                "1tesalonika", "1tes" -> "1 Thessalonians"
                "2tesalonika", "2tes" -> "2 Thessalonians"
                "1timotius", "1tim" -> "1 Timothy"
                "2timotius", "2tim" -> "2 Timothy"
                "titus" -> "Titus"
                "filemon", "film" -> "Philemon"
                "ibrani", "ibr" -> "Hebrews"
                "yakobus", "yak" -> "James"
                "1petrus", "1pet" -> "1 Peter"
                "2petrus", "2pet" -> "2 Peter"
                "1yohanes", "1yoh" -> "1 John"
                "2yohanes", "2yoh" -> "2 John"
                "3yohanes", "3yoh" -> "3 John"
                "yudas", "yud" -> "Jude"
                "wahyu", "wah" -> "Revelation"
                else -> bookPart.replaceFirstChar { it.uppercase() }
            }
            return "$englishBook $restPart"
        }
        return inputPassage
    }

    data class ParsedPassage(
        val bookId: Int,
        val bookName: String,
        val chapter: Int,
        val startVerse: Int?,
        val endVerse: Int?
    )

    fun bookNameToId(bookName: String): Int {
        val b = bookName.lowercase().replace(" ", "").replace("-", "").trim()
        return when (b) {
            "kejadian", "kej", "genesis", "gen" -> 1
            "keluaran", "kel", "exodus", "exo", "exd" -> 2
            "imamat", "im", "leviticus", "lev" -> 3
            "bilangan", "bil", "numbers", "num" -> 4
            "ulangan", "ul", "deuteronomy", "deu" -> 5
            "yosua", "yos", "joshua", "jos" -> 6
            "hakim-hakim", "hakim", "judges", "jdg" -> 7
            "rut", "ruth" -> 8
            "1samuel", "1sam", "1sa" -> 9
            "2samuel", "2sam", "2sa" -> 10
            "1raja-raja", "1raja", "1raj", "1kings", "1ki" -> 11
            "2raja-raja", "2raja", "2raj", "2kings", "2ki" -> 12
            "1tawarikh", "1taw", "1chronicles", "1ch" -> 13
            "2tawarikh", "2taw", "2chronicles", "2ch" -> 14
            "ezra", "ezr" -> 15
            "nehemia", "neh", "nehemiah" -> 16
            "ester", "est", "esther" -> 17
            "ayub", "job", "ayb" -> 18
            "mazmur", "maz", "psalms", "ps", "psa" -> 19
            "amsal", "ams", "proverbs", "pro" -> 20
            "pengkhotbah", "peng", "ecclesiastes", "ecc" -> 21
            "kidungagung", "kidung", "songofsolomon", "sng", "sos" -> 22
            "yesaya", "yes", "isaiah", "isa" -> 23
            "yeremia", "yer", "jeremiah", "jer" -> 24
            "ratapan", "rat", "lamentations", "lam" -> 25
            "yehezkiel", "yeh", "ezekiel", "ezk" -> 26
            "daniel", "dan" -> 27
            "hosea", "hos" -> 28
            "yoel", "joel", "jol" -> 29
            "amos", "amo" -> 30
            "obaja", "ob", "obadiah", "oba" -> 31
            "yunus", "jonah", "jon", "yun" -> 32
            "mikha", "micah", "mic", "mik" -> 33
            "nahum", "nah", "nam" -> 34
            "habakuk", "habakkuk", "hab" -> 35
            "zefanya", "zef", "zephaniah", "zep" -> 36
            "hagai", "haggai", "hag" -> 37
            "zakharia", "zechariah", "zak", "zec" -> 38
            "maleakhi", "malachi", "mal" -> 39
            "matius", "mat", "matthew" -> 40
            "markus", "mar", "mark", "mrk" -> 41
            "lukas", "luk", "luke" -> 42
            "yohanes", "yoh", "john", "jhn" -> 43
            "kisahpararasul", "kisah", "kpr", "acts", "act" -> 44
            "roma", "rom", "romans" -> 45
            "1korintus", "1kor", "1corinthians", "1co" -> 46
            "2korintus", "2kor", "2corinthians", "2co" -> 47
            "galatia", "gal", "galatians" -> 48
            "efesus", "efe", "ephesians", "eph" -> 49
            "filipi", "fil", "philippians", "php" -> 50
            "kolose", "kol", "colossians", "col" -> 51
            "1tesalonika", "1tes", "1thessalonians", "1th" -> 52
            "2tesalonika", "2tes", "2thessalonians", "2th" -> 53
            "1timotius", "1tim", "1timothy", "1ti" -> 54
            "2timotius", "2tim", "2timothy", "2ti" -> 55
            "titus" -> 56
            "filemon", "film", "philemon", "phm" -> 57
            "ibrani", "ibr", "hebrews", "heb" -> 58
            "yakobus", "yak", "james", "jas" -> 59
            "1petrus", "1pet", "1peter", "1pe" -> 60
            "2petrus", "2pet", "2peter", "2pe" -> 61
            "1yohanes", "1yoh", "1john", "1jn" -> 62
            "2yohanes", "2yoh", "2john", "2jn" -> 63
            "3yohanes", "3yoh", "3john", "3jn" -> 64
            "yudas", "yud", "jude", "jud" -> 65
            "wahyu", "wah", "revelation", "rev" -> 66
            else -> 0
        }
    }

    fun parsePassage(inputPassage: String): ParsedPassage? {
        val trimmed = inputPassage.trim()
        val m = Regex("^(\\d*\\s*[a-zA-Z\\s-]+)\\s*(\\d+)[\\s*:\\s*](\\d+)(?:\\s*-\\s*(\\d+))?.*$").find(trimmed)
        if (m != null) {
            val rawBookName = m.groupValues[1].trim()
            val bookId = bookNameToId(rawBookName)
            if (bookId == 0) return null
            val chapter = m.groupValues[2].toIntOrNull() ?: return null
            val startVerse = m.groupValues[3].toIntOrNull()
            val endVerse = m.groupValues[4].toIntOrNull()
            return ParsedPassage(bookId, rawBookName, chapter, startVerse, endVerse)
        }
        val chapterOnlyMatch = Regex("^(\\d*\\s*[a-zA-Z\\s-]+)\\s*(\\d+).*$").find(trimmed)
        if (chapterOnlyMatch != null) {
            val rawBookName = chapterOnlyMatch.groupValues[1].trim()
            val bookId = bookNameToId(rawBookName)
            if (bookId == 0) return null
            val chapter = chapterOnlyMatch.groupValues[2].toIntOrNull() ?: return null
            return ParsedPassage(bookId, rawBookName, chapter, null, null)
        }
        return null
    }
}

package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class CountryInfo(
    val name: String,
    val flagEmoji: String,
    val code: String
)

object CountryHelper {

    val COUNTRIES = listOf(
        CountryInfo("United States", "🇺🇸", "US"),
        CountryInfo("United Kingdom", "🇬🇧", "GB"),
        CountryInfo("United Arab Emirates", "🇦🇪", "AE"),
        CountryInfo("Saudi Arabia", "🇸🇦", "SA"),
        CountryInfo("India", "🇮🇳", "IN"),
        CountryInfo("Canada", "🇨🇦", "CA"),
        CountryInfo("Germany", "🇩🇪", "DE"),
        CountryInfo("France", "🇫🇷", "FR"),
        CountryInfo("Egypt", "🇪🇬", "EG"),
        CountryInfo("Turkey", "🇹🇷", "TR"),
        CountryInfo("Pakistan", "🇵🇰", "PK"),
        CountryInfo("Bangladesh", "🇧🇩", "BD"),
        CountryInfo("Indonesia", "🇮🇩", "ID"),
        CountryInfo("Philippines", "🇵🇭", "PH"),
        CountryInfo("Japan", "🇯🇵", "JP"),
        CountryInfo("South Korea", "🇰🇷", "KR"),
        CountryInfo("Brazil", "🇧🇷", "BR"),
        CountryInfo("Australia", "🇦🇺", "AU"),
        CountryInfo("Kuwait", "🇰🇼", "KW"),
        CountryInfo("Qatar", "🇶🇦", "QA"),
        CountryInfo("Global", "🌐", "GL")
    )

    fun getFlag(countryName: String): String {
        val found = COUNTRIES.find { it.name.equals(countryName.trim(), ignoreCase = true) }
        if (found != null) return found.flagEmoji
        // Check partial match
        val partial = COUNTRIES.find { countryName.contains(it.name, ignoreCase = true) || it.name.contains(countryName, ignoreCase = true) }
        return partial?.flagEmoji ?: "🌐"
    }

    /**
     * Compresses bitmap to high-quality JPEG and saves to internal app cache, returning the Uri.
     */
    fun saveAndCompressBitmap(context: Context, bitmap: Bitmap): Uri {
        val file = File(context.cacheDir, "star_voice_${UUID.randomUUID()}.jpg")
        val outStream = FileOutputStream(file)
        // High quality 85% JPEG compression
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream)
        outStream.flush()
        outStream.close()
        return Uri.fromFile(file)
    }
}

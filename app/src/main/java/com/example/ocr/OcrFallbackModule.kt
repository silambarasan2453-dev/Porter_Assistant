package com.example.ocr

import android.graphics.Bitmap
import com.example.parser.BookingCardParser
import com.example.parser.ParsedBooking

interface ScreenTextRecognizer {
    suspend fun recognizeTextFromBitmap(bitmap: Bitmap): String?
}

class OcrFallbackModule(
    private val recognizer: ScreenTextRecognizer? = null
) {
    /**
     * Determines whether OCR fallback is warranted based on accessibility extraction quality.
     */
    fun shouldTriggerFallback(accessibilityParsed: ParsedBooking?): Boolean {
        if (accessibilityParsed == null) return true
        if (accessibilityParsed.destinationArea.isBlank()) return true
        if (accessibilityParsed.confidence < 0.4f) return true
        return false
    }

    /**
     * Processes OCR recognized text into a ParsedBooking using BookingCardParser.
     */
    fun parseOcrOutput(recognizedText: String): ParsedBooking? {
        if (recognizedText.isBlank()) return null
        return BookingCardParser.parseFromRawText(recognizedText)
    }

    /**
     * Executes recognition if a recognizer is configured.
     */
    suspend fun extractFromScreen(bitmap: Bitmap): ParsedBooking? {
        val text = recognizer?.recognizeTextFromBitmap(bitmap) ?: return null
        return parseOcrOutput(text)
    }
}

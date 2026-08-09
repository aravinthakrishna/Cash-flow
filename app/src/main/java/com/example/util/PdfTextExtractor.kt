package com.example.util

import android.content.Context
import android.net.Uri
import java.io.InputStream
import java.util.regex.Pattern

object PdfTextExtractor {

    fun extractTextFromPdfUri(context: Context, uri: Uri): String {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return ""
            inputStream.close()
            extractTextFromPdfBytes(bytes)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun extractTextFromPdfBytes(bytes: ByteArray): String {
        val rawContent = String(bytes, Charsets.ISO_8859_1)
        val extractedLines = mutableListOf<String>()

        // Look for PDF text objects enclosed in Tj or TJ commands
        // E.g. (Text) Tj or [(Text) 10 (Text2)] TJ
        val tjPattern = Pattern.compile("\\((.*?)\\)\\s*Tj", Pattern.DOTALL)
        val matcher = tjPattern.matcher(rawContent)

        while (matcher.find()) {
            val text = matcher.group(1)
            if (!text.isNull_or_empty()) {
                val unescaped = unescapePdfString(text)
                if (unescaped.isNotBlank()) {
                    extractedLines.add(unescaped)
                }
            }
        }

        if (extractedLines.isNotEmpty()) {
            return extractedLines.joinToString("\n")
        }

        // Fallback: look for lines containing numbers or keywords in raw PDF content
        val lines = rawContent.lines()
        val fallbackLines = mutableListOf<String>()
        for (line in lines) {
            val clean = line.replace(Regex("[^a-zA-Z0-9\\s,.-/₹]"), " ").trim()
            if (clean.length > 5 && (clean.contains(",") || clean.contains("-") || clean.contains("/") || clean.contains("Food") || clean.contains("Transport"))) {
                fallbackLines.add(clean)
            }
        }

        return fallbackLines.joinToString("\n")
    }

    private fun unescapePdfString(input: String): String {
        return input.replace("\\(", "(")
            .replace("\\)", ")")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\\\", "\\")
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.isEmpty()
}

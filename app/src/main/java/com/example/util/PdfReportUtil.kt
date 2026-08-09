package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.data.model.Expense
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportUtil {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun generatePdfReport(context: Context, expenses: List<Expense>, monthlyBudget: Double): File {
        val pdfDocument = PdfDocument()

        // Page dimensions A4: 595 x 842 points
        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        val headerPaint = Paint().apply { isAntiAlias = true }

        val totalSpent = expenses.sumOf { it.amount }
        val remaining = monthlyBudget - totalSpent
        val totalCount = expenses.size

        // Colors
        val primaryGreen = Color.parseColor("#0B5A39")
        val darkGreen = Color.parseColor("#063C26")
        val lightBg = Color.parseColor("#F4F7F5")
        val textColor = Color.parseColor("#1F2937")
        val mutedText = Color.parseColor("#6B7280")
        val borderLineColor = Color.parseColor("#E5E7EB")

        var yPos = 40f

        // Draw Top Header Banner
        headerPaint.color = primaryGreen
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 100f, headerPaint)

        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("PERSONAL MONEY MANAGER", 30f, 45f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("EXPENSE REPORT", 30f, 65f, paint)

        val reportDateStr = "Generated: ${dateFormat.format(Date())}"
        paint.textSize = 10f
        val dateWidth = paint.measureText(reportDateStr)
        canvas.drawText(reportDateStr, pageWidth - 30f - dateWidth, 65f, paint)

        yPos = 120f

        // Summary Card Box
        paint.color = lightBg
        val summaryRect = RectF(30f, yPos, pageWidth - 30f, yPos + 85f)
        canvas.drawRoundRect(summaryRect, 10f, 10f, paint)

        paint.color = primaryGreen
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("PERIOD SUMMARY", 45f, yPos + 22f, paint)

        paint.color = textColor
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("Total Expenses Logged: $totalCount", 45f, yPos + 42f, paint)
        canvas.drawText("Total Amount Spent: ₹${String.format(Locale.US, "%.2f", totalSpent)}", 45f, yPos + 60f, paint)

        val budgetStr = "Monthly Budget: ₹${String.format(Locale.US, "%.2f", monthlyBudget)}"
        val remStr = "Budget Remaining: ₹${String.format(Locale.US, "%.2f", remaining)}"
        canvas.drawText(budgetStr, 300f, yPos + 42f, paint)
        canvas.drawText(remStr, 300f, yPos + 60f, paint)

        yPos += 105f

        // Table Header
        fun drawTableHeader(c: Canvas, currentY: Float) {
            paint.color = primaryGreen
            c.drawRect(30f, currentY, pageWidth - 30f, currentY + 26f, paint)

            paint.color = Color.WHITE
            paint.textSize = 10f
            paint.isFakeBoldText = true

            c.drawText("DATE", 40f, currentY + 17f, paint)
            c.drawText("CATEGORY", 130f, currentY + 17f, paint)
            c.drawText("NAME / DESCRIPTION", 230f, currentY + 17f, paint)
            c.drawText("AMOUNT (₹)", 420f, currentY + 17f, paint)
            c.drawText("RUNNING TOTAL", 500f, currentY + 17f, paint)
        }

        drawTableHeader(canvas, yPos)
        yPos += 26f

        paint.isFakeBoldText = false
        var runningTotal = 0.0

        for ((index, exp) in expenses.withIndex()) {
            runningTotal += exp.amount

            // Check if page overflow
            if (yPos > pageHeight - 50f) {
                pdfDocument.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas

                yPos = 40f
                drawTableHeader(canvas, yPos)
                yPos += 26f
            }

            // Alternating row background
            if (index % 2 == 1) {
                paint.color = lightBg
                canvas.drawRect(30f, yPos, pageWidth - 30f, yPos + 22f, paint)
            }

            paint.color = textColor
            paint.textSize = 9.5f

            val dateStr = dateOnlyFormat.format(Date(exp.date))
            val catStr = if (exp.category.length > 14) exp.category.substring(0, 12) + ".." else exp.category
            val nameStr = if (exp.name.length > 24) exp.name.substring(0, 22) + ".." else exp.name
            val amtStr = String.format(Locale.US, "₹%.2f", exp.amount)
            val runTotalStr = String.format(Locale.US, "₹%.2f", runningTotal)

            canvas.drawText(dateStr, 40f, yPos + 15f, paint)
            canvas.drawText(catStr, 130f, yPos + 15f, paint)
            canvas.drawText(nameStr, 230f, yPos + 15f, paint)
            canvas.drawText(amtStr, 420f, yPos + 15f, paint)
            canvas.drawText(runTotalStr, 500f, yPos + 15f, paint)

            // Bottom border line
            paint.color = borderLineColor
            canvas.drawLine(30f, yPos + 22f, pageWidth - 30f, yPos + 22f, paint)

            yPos += 22f
        }

        // Draw Footer on last page
        yPos += 15f
        if (yPos < pageHeight - 30f) {
            paint.color = mutedText
            paint.textSize = 8.5f
            paint.isFakeBoldText = false
            canvas.drawText("Generated via Personal Money Manager · DomainoTech", 30f, yPos, paint)
        }

        pdfDocument.finishPage(page)

        // Write PDF to file
        val pdfFile = File(context.cacheDir, "Expense_Report_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(pdfFile)
        pdfDocument.writeTo(outputStream)
        outputStream.close()
        pdfDocument.close()

        return pdfFile
    }
}

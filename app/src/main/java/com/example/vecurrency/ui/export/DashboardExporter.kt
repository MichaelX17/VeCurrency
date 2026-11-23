package com.example.vecurrency.ui.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.example.vecurrency.domain.model.ExchangeRate
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DashboardExporter {

    private val caracasZone = ZoneId.of("America/Caracas")

    fun generateAndShareSnapshot(context: Context, rates: List<ExchangeRate>): Boolean {
        var bitmap: Bitmap? = null
        try {
            bitmap = createDashboardBitmap(rates) ?: return false
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            val imageFile = File(exportDir, "vecurrency_dashboard.png")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, imageFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Tasas de Cambio Venezuela — VeCurrency")
                putExtra(Intent.EXTRA_TEXT, "Resumen de tasas de cambio generado por VeCurrency.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Compartir resumen de tasas")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            bitmap?.recycle()
        }
    }

    fun createDashboardBitmap(rates: List<ExchangeRate>): Bitmap? {
        val width = 1080
        val height = 1440
        val bitmap = try {
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        } catch (_: Throwable) {
            null
        } ?: return null
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.parseColor("#121212")) // Dark Material theme background

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Header Background Banner
        paint.color = Color.parseColor("#1F2937")
        canvas.drawRect(0f, 0f, width.toFloat(), 240f, paint)

        // App Title
        paint.color = Color.parseColor("#60A5FA") // Accent Blue
        paint.textSize = 54f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("VeCurrency", 60f, 100f, paint)

        // Header Subtitle
        paint.color = Color.WHITE
        paint.textSize = 36f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Resumen Oficial de Tasas de Cambio", 60f, 160f, paint)

        val formattedDate = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")
            .format(Instant.now().atZone(caracasZone))
        paint.color = Color.parseColor("#9CA3AF")
        paint.textSize = 28f
        canvas.drawText("Generado el: $formattedDate (VET)", 60f, 205f, paint)

        // Draw Rate Cards
        var startY = 280f
        val cardHeight = 320f
        val cardMargin = 40f

        for (rate in rates) {
            val rect = RectF(50f, startY, width - 50f, startY + cardHeight)
            paint.color = Color.parseColor("#1E1E1E")
            canvas.drawRoundRect(rect, 24f, 24f, paint)

            // Card Border
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = Color.parseColor("#374151")
            canvas.drawRoundRect(rect, 24f, 24f, paint)

            paint.style = Paint.Style.FILL

            // Currency Title & Badge
            paint.color = Color.WHITE
            paint.textSize = 38f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("${rate.currency.symbol} ${rate.currency.code}", 90f, startY + 70f, paint)

            paint.color = Color.parseColor("#10B981") // Green status
            paint.textSize = 26f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(rate.status.name, width - 260f, startY + 70f, paint)

            // Rate Value
            paint.color = Color.parseColor("#38BDF8")
            paint.textSize = 68f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Bs. ${rate.vesRate.toPlainString()}", 90f, startY + 170f, paint)

            // Source & Details
            paint.color = Color.parseColor("#9CA3AF")
            paint.textSize = 28f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Fuente: ${rate.source.displayName}", 90f, startY + 230f, paint)

            val rateTimeStr = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")
                .format(rate.fetchTimestamp.atZone(caracasZone))
            canvas.drawText("Actualizado: $rateTimeStr (VET)", 90f, startY + 275f, paint)

            startY += cardHeight + cardMargin
        }

        // Footer
        paint.color = Color.parseColor("#6B7280")
        paint.textSize = 26f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("VeCurrency — Consulta directa desde el dispositivo sin servidores intermedios", width / 2f, height - 60f, paint)

        return bitmap
    }
}

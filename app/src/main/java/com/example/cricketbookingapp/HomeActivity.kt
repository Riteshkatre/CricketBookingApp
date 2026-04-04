package com.example.cricketbookingapp

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {
    private val allBookings = listOf(
        BookingItem("Morning Turf Match", "10 Apr 2026", "07:00 AM", "Rs. 1200"),
        BookingItem("Weekend Practice Nets", "12 Apr 2026", "05:30 PM", "Rs. 850"),
        BookingItem("Corporate Cricket League", "15 Apr 2026", "08:15 PM", "Rs. 2000"),
        BookingItem("Night Knockout Booking", "18 Apr 2026", "09:00 PM", "Rs. 1500")
    )
    private lateinit var bookingAdapter: BookingAdapter
    private var visibleBookings: List<BookingItem> = allBookings
    private var selectedSingleDate: String? = null
    private var selectedRangeStartDate: String? = null
    private var selectedRangeEndDate: String? = null
    private var searchQuery: String = ""
    private val bookingDateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homeRoot)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.bookingRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        bookingAdapter = BookingAdapter(allBookings)
        recyclerView.adapter = bookingAdapter

        val dateFilterButton = findViewById<MaterialButton>(R.id.dateFilterButton)
        val clearDateFilterButton = findViewById<AppCompatImageButton>(R.id.clearDateFilterButton)
        val printPdfButton = findViewById<MaterialButton>(R.id.printPdfButton)
        val bookingSearchView = findViewById<SearchView>(R.id.bookingSearchView)

        dateFilterButton.setOnClickListener {
            openDateFilterChoiceDialog(dateFilterButton, clearDateFilterButton)
        }

        clearDateFilterButton.setOnClickListener {
            selectedSingleDate = null
            selectedRangeStartDate = null
            selectedRangeEndDate = null
            dateFilterButton.text = getString(R.string.filter_date_label)
            clearDateFilterButton.visibility = View.GONE
            applyFilters()
        }

        printPdfButton.setOnClickListener {
            exportBookingsToPdf()
        }

        bookingSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchQuery = query.orEmpty()
                applyFilters()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText.orEmpty()
                applyFilters()
                return true
            }
        })

        findViewById<FloatingActionButton>(R.id.addBookingButton).setOnClickListener {
            startActivity(Intent(this, AddBookingActivity::class.java))
        }
    }

    private fun openDateFilterChoiceDialog(
        dateFilterButton: MaterialButton,
        clearDateFilterButton: AppCompatImageButton
    ) {
        val options = arrayOf(
            getString(R.string.filter_option_single_date),
            getString(R.string.filter_option_date_range)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.filter_date_dialog_title)
            .setItems(options) { _: DialogInterface, which: Int ->
                when (which) {
                    0 -> openSingleDateFilterDialog(dateFilterButton, clearDateFilterButton)
                    1 -> openRangeStartDialog(dateFilterButton, clearDateFilterButton)
                }
            }
            .show()
    }

    private fun openSingleDateFilterDialog(
        dateFilterButton: MaterialButton,
        clearDateFilterButton: AppCompatImageButton
    ) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedSingleDate = bookingDateFormatter.format(calendar.time)
                selectedRangeStartDate = null
                selectedRangeEndDate = null
                dateFilterButton.text = selectedSingleDate
                clearDateFilterButton.visibility = View.VISIBLE
                applyFilters()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun openRangeStartDialog(
        dateFilterButton: MaterialButton,
        clearDateFilterButton: AppCompatImageButton
    ) {
        val startCalendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                startCalendar.set(year, month, dayOfMonth)
                val startDate = bookingDateFormatter.format(startCalendar.time)
                openRangeEndDialog(startDate, dateFilterButton, clearDateFilterButton)
            },
            startCalendar.get(Calendar.YEAR),
            startCalendar.get(Calendar.MONTH),
            startCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun openRangeEndDialog(
        startDate: String,
        dateFilterButton: MaterialButton,
        clearDateFilterButton: AppCompatImageButton
    ) {
        val endCalendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                endCalendar.set(year, month, dayOfMonth)
                val endDate = bookingDateFormatter.format(endCalendar.time)
                val orderedDates = orderDates(startDate, endDate)
                selectedSingleDate = null
                selectedRangeStartDate = orderedDates.first
                selectedRangeEndDate = orderedDates.second
                dateFilterButton.text = getString(
                    R.string.filter_range_value,
                    selectedRangeStartDate,
                    selectedRangeEndDate
                )
                clearDateFilterButton.visibility = View.VISIBLE
                applyFilters()
            },
            endCalendar.get(Calendar.YEAR),
            endCalendar.get(Calendar.MONTH),
            endCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun applyFilters() {
        val filteredBookings = allBookings.filter { booking ->
            val matchesDate = matchesSelectedDate(booking.date)
            val matchesSearch = searchQuery.isBlank() ||
                booking.name.contains(searchQuery, ignoreCase = true) ||
                booking.amount.contains(searchQuery, ignoreCase = true) ||
                booking.time.contains(searchQuery, ignoreCase = true)
            matchesDate && matchesSearch
        }
        visibleBookings = filteredBookings
        bookingAdapter.updateBookings(filteredBookings)
    }

    private fun exportBookingsToPdf() {
        val bookingsForPdf = getBookingsForPdf()

        if (bookingsForPdf.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_bookings_to_print), Toast.LENGTH_SHORT).show()
            return
        }

        val pdfDocument = PdfDocument()
        val titlePaint = Paint().apply {
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = android.graphics.Color.BLACK
        }
        val bodyPaint = Paint().apply {
            textSize = 14f
            color = android.graphics.Color.DKGRAY
        }
        val amountPaint = Paint().apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = android.graphics.Color.BLACK
        }

        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var y = 50

        fun drawHeader() {
            canvas.drawText(getString(R.string.pdf_title), 40f, y.toFloat(), titlePaint)
            y += 30
            canvas.drawText(getString(R.string.home_user_name), 40f, y.toFloat(), bodyPaint)
            y += 30
        }

        drawHeader()

        bookingsForPdf.forEachIndexed { index, booking ->
            if (y > pageHeight - 120) {
                pdfDocument.finishPage(page)
                pageNumber += 1
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50
                drawHeader()
            }

            canvas.drawText("${index + 1}. ${booking.name}", 40f, y.toFloat(), amountPaint)
            y += 22
            canvas.drawText("${booking.date}  |  ${booking.time}", 55f, y.toFloat(), bodyPaint)
            y += 20
            canvas.drawText(booking.amount, 55f, y.toFloat(), bodyPaint)
            y += 28
        }

        pdfDocument.finishPage(page)

        try {
            val pdfFile = File(cacheDir, "booking_list.pdf")
            FileOutputStream(pdfFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()

            val pdfUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                pdfFile
            )
            val openPdfIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(pdfUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(openPdfIntent, getString(R.string.open_pdf_chooser)))
        } catch (exception: Exception) {
            pdfDocument.close()
            Toast.makeText(this, getString(R.string.pdf_generation_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getBookingsForPdf(): List<BookingItem> {
        return allBookings.filter { booking -> matchesSelectedDate(booking.date) }
    }

    private fun matchesSelectedDate(bookingDate: String): Boolean {
        selectedSingleDate?.let { return bookingDate == it }

        val startDate = selectedRangeStartDate
        val endDate = selectedRangeEndDate
        if (startDate != null && endDate != null) {
            val booking = parseBookingDate(bookingDate) ?: return false
            val start = parseBookingDate(startDate) ?: return false
            val end = parseBookingDate(endDate) ?: return false
            return !booking.before(start) && !booking.after(end)
        }

        return true
    }

    private fun parseBookingDate(value: String): Date? {
        return try {
            bookingDateFormatter.parse(value)
        } catch (_: ParseException) {
            null
        }
    }

    private fun orderDates(firstDate: String, secondDate: String): Pair<String, String> {
        val first = parseBookingDate(firstDate)
        val second = parseBookingDate(secondDate)
        return if (first != null && second != null && first.after(second)) {
            secondDate to firstDate
        } else {
            firstDate to secondDate
        }
    }
}

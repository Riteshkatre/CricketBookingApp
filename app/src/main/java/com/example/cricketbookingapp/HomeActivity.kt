package com.example.cricketbookingapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.ListenerRegistration
import java.io.File
import java.io.FileOutputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {
    private var allBookings: List<BookingItem> = emptyList()
    private var visibleRevenueBookings: List<BookingItem> = emptyList()
    private var showAllUpcomingBookings = false
    private var bookingListener: ListenerRegistration? = null

    private lateinit var revenueAdapter: BookingAdapter
    private lateinit var todayAdapter: DashboardBookingAdapter
    private lateinit var upcomingAdapter: DashboardBookingAdapter
    private lateinit var availabilityDateAdapter: AvailabilityDateAdapter
    private lateinit var availabilitySlotAdapter: AvailabilitySlotAdapter

    private lateinit var homeSection: View
    private lateinit var availabilitySection: View
    private lateinit var bookingSection: View
    private lateinit var revenueSection: View

    private lateinit var dateFilterButton: MaterialButton
    private lateinit var clearDateFilterButton: AppCompatImageButton
    private lateinit var noDataText: TextView
    private lateinit var revenueSubTitleText: TextView
    private lateinit var todayBookingCountText: TextView
    private lateinit var lastRevenueText: TextView
    private lateinit var todayDateText: TextView
    private lateinit var noTodayBookingsText: TextView
    private lateinit var allBookingsButtonText: TextView
    private lateinit var selectedAvailabilityDateText: TextView
    private lateinit var noAvailabilitySlotsText: TextView

    private lateinit var bookingNameInput: TextInputEditText
    private lateinit var customerNameInput: TextInputEditText
    private lateinit var customerPhoneInput: TextInputEditText
    private lateinit var startDateTimeInput: TextInputEditText
    private lateinit var endDateTimeInput: TextInputEditText
    private lateinit var pricePerHourInput: TextInputEditText
    private lateinit var advancePaymentInput: TextInputEditText
    private lateinit var discountInput: TextInputEditText
    private lateinit var submitBookingButton: MaterialButton
    private lateinit var addBookingLoader: CircularProgressIndicator

    private lateinit var bookingNameLayout: TextInputLayout
    private lateinit var customerNameLayout: TextInputLayout
    private lateinit var customerPhoneLayout: TextInputLayout
    private lateinit var startDateTimeLayout: TextInputLayout
    private lateinit var endDateTimeLayout: TextInputLayout
    private lateinit var pricePerHourLayout: TextInputLayout
    private lateinit var advancePaymentLayout: TextInputLayout
    private lateinit var discountLayout: TextInputLayout

    private lateinit var summaryDateText: TextView
    private lateinit var summaryCustomerText: TextView
    private lateinit var summaryBoxText: TextView
    private lateinit var summarySubtotalText: TextView
    private lateinit var summaryAdvanceText: TextView
    private lateinit var summaryDiscountText: TextView
    private lateinit var summaryTotalText: TextView

    private lateinit var box1Button: MaterialButton
    private lateinit var box2Button: MaterialButton
    private lateinit var formBox1Button: MaterialButton
    private lateinit var formBox2Button: MaterialButton

    private var selectedRevenueSingleDate: String? = null
    private var selectedRevenueRangeStartDate: String? = null
    private var selectedRevenueRangeEndDate: String? = null
    private var selectedAvailabilityDateMillis: Long = startOfDay(System.currentTimeMillis())
    private var selectedAvailabilityBoxName = "Box 1"
    private var selectedFormBoxName = "Box 1"

    private val bookingDateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val longDateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    private val shortDateFormatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    private val dateTimeFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val rootView = findViewById<View>(R.id.homeRoot)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            bottomNavigation.setPadding(
                bottomNavigation.paddingLeft,
                bottomNavigation.paddingTop,
                bottomNavigation.paddingRight,
                systemBars.bottom.coerceAtMost(8)
            )
            insets
        }

        bindViews()
        setupAdapters()
        setupBottomNavigation()
        setupRevenueControls()
        setupAvailabilityControls()
        setupBookingForm()
        updateSectionVisibility(R.id.menu_home)
        updateHomeHeader()
    }

    override fun onStart() {
        super.onStart()
        bookingListener = FirebaseRepository.listenToBookings(
            onUpdate = { bookings ->
                allBookings = bookings.sortedBy { it.startDateTimeMillis }
                refreshAllSections()
            },
            onError = { error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onStop() {
        bookingListener?.remove()
        bookingListener = null
        super.onStop()
    }

    private fun bindViews() {
        homeSection = findViewById(R.id.homeSection)
        availabilitySection = findViewById(R.id.availabilitySection)
        bookingSection = findViewById(R.id.bookingSection)
        revenueSection = findViewById(R.id.revenueSection)

        dateFilterButton = findViewById(R.id.dateFilterButton)
        clearDateFilterButton = findViewById(R.id.clearDateFilterButton)
        noDataText = findViewById(R.id.noDataText)
        revenueSubTitleText = findViewById(R.id.revenueSubTitleText)
        todayBookingCountText = findViewById(R.id.todayBookingCountText)
        lastRevenueText = findViewById(R.id.lastRevenueText)
        todayDateText = findViewById(R.id.todayDateText)
        noTodayBookingsText = findViewById(R.id.noTodayBookingsText)
        allBookingsButtonText = findViewById(R.id.allBookingsButtonText)
        selectedAvailabilityDateText = findViewById(R.id.selectedAvailabilityDateText)
        noAvailabilitySlotsText = findViewById(R.id.noAvailabilitySlotsText)

        bookingNameInput = findViewById(R.id.bookingNameInput)
        customerNameInput = findViewById(R.id.customerNameInput)
        customerPhoneInput = findViewById(R.id.customerPhoneInput)
        startDateTimeInput = findViewById(R.id.startDateTimeInput)
        endDateTimeInput = findViewById(R.id.endDateTimeInput)
        pricePerHourInput = findViewById(R.id.pricePerHourInput)
        advancePaymentInput = findViewById(R.id.advancePaymentInput)
        discountInput = findViewById(R.id.discountInput)
        submitBookingButton = findViewById(R.id.submitBookingButton)
        addBookingLoader = findViewById(R.id.addBookingLoader)

        bookingNameLayout = findViewById(R.id.bookingNameLayout)
        customerNameLayout = findViewById(R.id.customerNameLayout)
        customerPhoneLayout = findViewById(R.id.customerPhoneLayout)
        startDateTimeLayout = findViewById(R.id.startDateTimeLayout)
        endDateTimeLayout = findViewById(R.id.endDateTimeLayout)
        pricePerHourLayout = findViewById(R.id.pricePerHourLayout)
        advancePaymentLayout = findViewById(R.id.advancePaymentLayout)
        discountLayout = findViewById(R.id.discountLayout)

        summaryDateText = findViewById(R.id.summaryDateText)
        summaryCustomerText = findViewById(R.id.summaryCustomerText)
        summaryBoxText = findViewById(R.id.summaryBoxText)
        summarySubtotalText = findViewById(R.id.summarySubtotalText)
        summaryAdvanceText = findViewById(R.id.summaryAdvanceText)
        summaryDiscountText = findViewById(R.id.summaryDiscountText)
        summaryTotalText = findViewById(R.id.summaryTotalText)

        box1Button = findViewById(R.id.box1Button)
        box2Button = findViewById(R.id.box2Button)
        formBox1Button = findViewById(R.id.formBox1Button)
        formBox2Button = findViewById(R.id.formBox2Button)
    }

    private fun setupAdapters() {
        revenueAdapter = BookingAdapter(emptyList()) { openBookingDetails(it) }
        todayAdapter = DashboardBookingAdapter(emptyList()) { openBookingDetails(it) }
        upcomingAdapter = DashboardBookingAdapter(emptyList()) { openBookingDetails(it) }
        availabilityDateAdapter = AvailabilityDateAdapter(emptyList()) { dateMillis ->
            selectedAvailabilityDateMillis = startOfDay(dateMillis)
            renderAvailability()
        }
        availabilitySlotAdapter = AvailabilitySlotAdapter(emptyList())

        findViewById<RecyclerView>(R.id.bookingRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = revenueAdapter
        }
        findViewById<RecyclerView>(R.id.todayBookingsRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = todayAdapter
        }
        findViewById<RecyclerView>(R.id.upcomingBookingsRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = upcomingAdapter
        }
        findViewById<RecyclerView>(R.id.dateSelectorRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = availabilityDateAdapter
        }
        findViewById<RecyclerView>(R.id.availabilitySlotsRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = availabilitySlotAdapter
        }
    }

    private fun setupBottomNavigation() {
        findViewById<BottomNavigationView>(R.id.bottomNavigation).apply {
            selectedItemId = R.id.menu_home
            setOnItemSelectedListener { item ->
                updateSectionVisibility(item.itemId)
                true
            }
        }
    }

    private fun updateSectionVisibility(menuId: Int) {
        homeSection.visibility = if (menuId == R.id.menu_home) View.VISIBLE else View.GONE
        availabilitySection.visibility = if (menuId == R.id.menu_availability) View.VISIBLE else View.GONE
        bookingSection.visibility = if (menuId == R.id.menu_booking) View.VISIBLE else View.GONE
        revenueSection.visibility = if (menuId == R.id.menu_revenue) View.VISIBLE else View.GONE
    }

    private fun setupRevenueControls() {
        dateFilterButton.text = getString(R.string.last_days_bookings_default)
        dateFilterButton.setOnClickListener { openDateFilterChoiceDialog() }
        clearDateFilterButton.setOnClickListener {
            selectedRevenueSingleDate = null
            selectedRevenueRangeStartDate = null
            selectedRevenueRangeEndDate = null
            dateFilterButton.text = getString(R.string.last_days_bookings_default)
            clearDateFilterButton.visibility = View.GONE
            renderRevenue()
        }
        findViewById<MaterialButton>(R.id.printPdfButton).setOnClickListener {
            exportBookingsToPdf()
        }
    }

    private fun setupAvailabilityControls() {
        box1Button.setOnClickListener {
            selectedAvailabilityBoxName = "Box 1"
            updateAvailabilityBoxButtons()
            renderAvailability()
        }
        box2Button.setOnClickListener {
            selectedAvailabilityBoxName = "Box 2"
            updateAvailabilityBoxButtons()
            renderAvailability()
        }
        updateAvailabilityBoxButtons()
        availabilityDateAdapter.updateDates(buildDateOptions(), selectedAvailabilityDateMillis)
    }

    private fun setupBookingForm() {
        val updateSummary = {
            val startMillis = startDateTimeInput.tag as? Long
            val endMillis = endDateTimeInput.tag as? Long
            val customerName = customerNameInput.text?.toString()?.trim().orEmpty()
            val customerPhone = customerPhoneInput.text?.toString()?.trim().orEmpty()
            val pricePerHour = pricePerHourInput.text?.toString()?.trim().orEmpty().toDoubleOrNull() ?: 0.0
            val advancePayment = advancePaymentInput.text?.toString()?.trim().orEmpty().toDoubleOrNull() ?: 0.0
            val discount = discountInput.text?.toString()?.trim().orEmpty().toDoubleOrNull() ?: 0.0
            val totalHours = if (startMillis != null && endMillis != null && endMillis > startMillis) {
                (endMillis - startMillis) / (60.0 * 60.0 * 1000.0)
            } else {
                0.0
            }
            val subTotal = pricePerHour * totalHours
            val finalTotal = (subTotal - discount).coerceAtLeast(0.0)

            summaryDateText.text = if (startMillis != null && endMillis != null) {
                "${bookingDateFormatter.format(Date(startMillis))}\n${timeFormatter.format(Date(startMillis))} - ${timeFormatter.format(Date(endMillis))}"
            } else {
                getString(R.string.summary_empty_date)
            }
            summaryCustomerText.text = listOf(
                customerName.ifBlank { getString(R.string.summary_empty_customer) },
                customerPhone.ifBlank { getString(R.string.summary_empty_phone) }
            ).joinToString("\n")
            summaryBoxText.text = selectedFormBoxName
            summarySubtotalText.text = getString(R.string.currency_value, formatAmount(subTotal))
            summaryAdvanceText.text = getString(R.string.currency_value, formatAmount(advancePayment))
            summaryDiscountText.text = getString(R.string.currency_value, formatAmount(discount))
            summaryTotalText.text = getString(R.string.currency_value, formatAmount(finalTotal))
        }

        configureDateTimeField(startDateTimeInput, updateSummary)
        configureDateTimeField(endDateTimeInput, updateSummary)

        listOf(
            bookingNameInput,
            customerNameInput,
            customerPhoneInput,
            pricePerHourInput,
            advancePaymentInput,
            discountInput
        ).forEach { input -> input.doAfterTextChanged { updateSummary() } }

        formBox1Button.setOnClickListener {
            selectedFormBoxName = "Box 1"
            updateFormBoxButtons()
            updateSummary()
        }
        formBox2Button.setOnClickListener {
            selectedFormBoxName = "Box 2"
            updateFormBoxButtons()
            updateSummary()
        }
        updateFormBoxButtons()
        updateSummary()

        submitBookingButton.setOnClickListener { submitBooking() }
    }

    private fun submitBooking() {
        listOf(
            bookingNameLayout,
            customerNameLayout,
            customerPhoneLayout,
            startDateTimeLayout,
            endDateTimeLayout,
            pricePerHourLayout,
            advancePaymentLayout,
            discountLayout
        ).forEach { it.error = null }

        val bookingName = bookingNameInput.text?.toString()?.trim().orEmpty()
        val customerName = customerNameInput.text?.toString()?.trim().orEmpty()
        val customerPhone = customerPhoneInput.text?.toString()?.trim().orEmpty()
        val pricePerHourText = pricePerHourInput.text?.toString()?.trim().orEmpty()
        val advancePaymentText = advancePaymentInput.text?.toString()?.trim().orEmpty().ifBlank { "0" }
        val discountText = discountInput.text?.toString()?.trim().orEmpty().ifBlank { "0" }
        val startMillis = startDateTimeInput.tag as? Long
        val endMillis = endDateTimeInput.tag as? Long

        when {
            bookingName.isBlank() -> bookingNameLayout.error = getString(R.string.booking_name_required)
            customerName.isBlank() -> customerNameLayout.error = getString(R.string.customer_name_required)
            customerPhone.length != 10 || customerPhone.any { !it.isDigit() } ->
                customerPhoneLayout.error = getString(R.string.invalid_mobile_number)
            startMillis == null -> startDateTimeLayout.error = getString(R.string.start_date_required)
            endMillis == null -> endDateTimeLayout.error = getString(R.string.end_date_required)
            pricePerHourText.toDoubleOrNull() == null ->
                pricePerHourLayout.error = getString(R.string.invalid_booking_amount)
            advancePaymentText.toDoubleOrNull() == null ->
                advancePaymentLayout.error = getString(R.string.invalid_booking_amount)
            discountText.toDoubleOrNull() == null ->
                discountLayout.error = getString(R.string.invalid_booking_amount)
            startMillis != null && endMillis != null && endMillis <= startMillis ->
                endDateTimeLayout.error = getString(R.string.end_must_be_after_start)
        }

        if (listOf(
                bookingNameLayout,
                customerNameLayout,
                customerPhoneLayout,
                startDateTimeLayout,
                endDateTimeLayout,
                pricePerHourLayout,
                advancePaymentLayout,
                discountLayout
            ).any { it.error != null }
        ) return

        val confirmedStartMillis = startMillis ?: return
        val confirmedEndMillis = endMillis ?: return
        val totalHours = (confirmedEndMillis - confirmedStartMillis) / (60.0 * 60.0 * 1000.0)
        val finalAmount = ((pricePerHourText.toDoubleOrNull() ?: 0.0) * totalHours -
            (discountText.toDoubleOrNull() ?: 0.0)).coerceAtLeast(0.0)

        submitBookingButton.isEnabled = false
        addBookingLoader.visibility = View.VISIBLE

        FirebaseRepository.addBooking(
            bookingName = bookingName,
            customerName = customerName,
            customerPhone = customerPhone,
            boxName = selectedFormBoxName,
            startDateTimeMillis = confirmedStartMillis,
            endDateTimeMillis = confirmedEndMillis,
            pricePerHour = pricePerHourText,
            advancePayment = advancePaymentText,
            discount = discountText,
            amount = formatAmount(finalAmount),
            onSuccess = {
                submitBookingButton.isEnabled = true
                addBookingLoader.visibility = View.GONE
                Toast.makeText(this, getString(R.string.booking_added_message), Toast.LENGTH_SHORT).show()
                clearBookingForm()
            },
            onError = { error ->
                submitBookingButton.isEnabled = true
                addBookingLoader.visibility = View.GONE
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun clearBookingForm() {
        bookingNameInput.text = null
        customerNameInput.text = null
        customerPhoneInput.text = null
        startDateTimeInput.text = null
        endDateTimeInput.text = null
        pricePerHourInput.text = null
        advancePaymentInput.text = null
        discountInput.text = null
        startDateTimeInput.tag = null
        endDateTimeInput.tag = null
        selectedFormBoxName = "Box 1"
        updateFormBoxButtons()
        summaryDateText.text = getString(R.string.summary_empty_date)
        summaryCustomerText.text = "${getString(R.string.summary_empty_customer)}\n${getString(R.string.summary_empty_phone)}"
        summaryBoxText.text = selectedFormBoxName
        summarySubtotalText.text = getString(R.string.currency_zero)
        summaryAdvanceText.text = getString(R.string.currency_zero)
        summaryDiscountText.text = getString(R.string.currency_zero)
        summaryTotalText.text = getString(R.string.currency_zero)
    }

    private fun refreshAllSections() {
        renderHome()
        renderAvailability()
        renderRevenue()
    }

    private fun renderHome() {
        val todayStart = startOfDay(System.currentTimeMillis())
        val tomorrowStart = todayStart + ONE_DAY_MILLIS
        val sevenDaysAgo = todayStart - (6 * ONE_DAY_MILLIS)

        val todayBookings = allBookings.filter { it.startDateTimeMillis in todayStart until tomorrowStart }
        val upcomingBookings = allBookings.filter { it.startDateTimeMillis >= tomorrowStart }
        val last7DaysRevenue = allBookings
            .filter { it.startDateTimeMillis in sevenDaysAgo until tomorrowStart }
            .sumOf { it.amount.toDoubleOrNull() ?: 0.0 }

        todayBookingCountText.text = todayBookings.size.toString()
        lastRevenueText.text = getString(R.string.currency_value, formatAmount(last7DaysRevenue))
        todayDateText.text = shortDateFormatter.format(Date(todayStart))
        todayAdapter.updateItems(todayBookings)
        noTodayBookingsText.visibility = if (todayBookings.isEmpty()) View.VISIBLE else View.GONE

        val upcomingDisplay = if (showAllUpcomingBookings) upcomingBookings else upcomingBookings.take(3)
        upcomingAdapter.updateItems(upcomingDisplay)
        allBookingsButtonText.text = if (showAllUpcomingBookings) getString(R.string.show_less) else getString(R.string.all_bookings)
        allBookingsButtonText.visibility = if (upcomingBookings.size > 3) View.VISIBLE else View.GONE
        allBookingsButtonText.setOnClickListener {
            showAllUpcomingBookings = !showAllUpcomingBookings
            renderHome()
        }
    }

    private fun renderAvailability() {
        availabilityDateAdapter.updateDates(buildDateOptions(), selectedAvailabilityDateMillis)
        selectedAvailabilityDateText.text = longDateFormatter.format(Date(selectedAvailabilityDateMillis))

        val bookedSlots = allBookings
            .filter {
                it.boxName.equals(selectedAvailabilityBoxName, ignoreCase = true) &&
                    isSameDay(it.startDateTimeMillis, selectedAvailabilityDateMillis)
            }
            .sortedBy { it.startDateTimeMillis }
            .map { booking ->
                AvailabilitySlotItem(
                    timeLabel = booking.timeRange,
                    title = booking.name,
                    subtitle = "${booking.displayCustomerName} | ${booking.boxName}",
                    statusLabel = getString(R.string.booked),
                    isBooked = true
                )
            }

        availabilitySlotAdapter.updateItems(bookedSlots)
        noAvailabilitySlotsText.visibility = if (bookedSlots.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun renderRevenue() {
        val filtered = getRevenueBookings()
        visibleRevenueBookings = filtered
        revenueAdapter.updateBookings(filtered)
        noDataText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        revenueSubTitleText.text = if (selectedRevenueSingleDate == null &&
            selectedRevenueRangeStartDate == null &&
            selectedRevenueRangeEndDate == null
        ) {
            getString(R.string.last_days_bookings_default)
        } else {
            getString(R.string.last_days_bookings, filtered.size)
        }
    }

    private fun getRevenueBookings(): List<BookingItem> {
        if (selectedRevenueSingleDate == null &&
            selectedRevenueRangeStartDate == null &&
            selectedRevenueRangeEndDate == null
        ) {
            val todayStart = startOfDay(System.currentTimeMillis())
            val sevenDaysAgo = todayStart - (6 * ONE_DAY_MILLIS)
            val tomorrowStart = todayStart + ONE_DAY_MILLIS
            return allBookings.filter { it.startDateTimeMillis in sevenDaysAgo until tomorrowStart }
        }

        return allBookings.filter { booking -> matchesRevenueDate(booking.date) }
    }

    private fun openBookingDetails(booking: BookingItem) {
        startActivity(BookingDetailsActivity.createIntent(this, booking))
    }

    private fun openDateFilterChoiceDialog() {
        val options = arrayOf(
            getString(R.string.filter_option_single_date),
            getString(R.string.filter_option_date_range)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.filter_date_dialog_title)
            .setItems(options) { _: DialogInterface, which: Int ->
                when (which) {
                    0 -> openSingleDateFilterDialog()
                    1 -> openRangeStartDialog()
                }
            }
            .show()
    }

    private fun openSingleDateFilterDialog() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedRevenueSingleDate = bookingDateFormatter.format(calendar.time)
                selectedRevenueRangeStartDate = null
                selectedRevenueRangeEndDate = null
                dateFilterButton.text = selectedRevenueSingleDate
                clearDateFilterButton.visibility = View.VISIBLE
                renderRevenue()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun openRangeStartDialog() {
        val startCalendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                startCalendar.set(year, month, dayOfMonth)
                openRangeEndDialog(bookingDateFormatter.format(startCalendar.time))
            },
            startCalendar.get(Calendar.YEAR),
            startCalendar.get(Calendar.MONTH),
            startCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun openRangeEndDialog(startDate: String) {
        val endCalendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                endCalendar.set(year, month, dayOfMonth)
                val ordered = orderDates(startDate, bookingDateFormatter.format(endCalendar.time))
                selectedRevenueSingleDate = null
                selectedRevenueRangeStartDate = ordered.first
                selectedRevenueRangeEndDate = ordered.second
                dateFilterButton.text = getString(R.string.filter_range_value, ordered.first, ordered.second)
                clearDateFilterButton.visibility = View.VISIBLE
                renderRevenue()
            },
            endCalendar.get(Calendar.YEAR),
            endCalendar.get(Calendar.MONTH),
            endCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun matchesRevenueDate(bookingDate: String): Boolean {
        selectedRevenueSingleDate?.let { return bookingDate == it }
        val startDate = selectedRevenueRangeStartDate
        val endDate = selectedRevenueRangeEndDate
        if (startDate != null && endDate != null) {
            val booking = parseBookingDate(bookingDate) ?: return false
            val start = parseBookingDate(startDate) ?: return false
            val end = parseBookingDate(endDate) ?: return false
            return !booking.before(start) && !booking.after(end)
        }
        return true
    }

    private fun exportBookingsToPdf() {
        if (visibleRevenueBookings.isEmpty()) {
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
        }

        drawHeader()
        visibleRevenueBookings.forEachIndexed { index, booking ->
            if (y > pageHeight - 140) {
                pdfDocument.finishPage(page)
                pageNumber += 1
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50
                drawHeader()
            }
            canvas.drawText("${index + 1}. ${booking.name}", 40f, y.toFloat(), bodyPaint)
            y += 20
            canvas.drawText("${booking.date} | ${booking.timeRange} | ${booking.boxName}", 50f, y.toFloat(), bodyPaint)
            y += 20
            canvas.drawText("${booking.displayCustomerName} | ${booking.displayAmount}", 50f, y.toFloat(), bodyPaint)
            y += 24
        }
        pdfDocument.finishPage(page)

        try {
            val pdfFile = File(cacheDir, "booking_list.pdf")
            FileOutputStream(pdfFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()

            val pdfUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", pdfFile)
            startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(pdfUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, getString(R.string.open_pdf_chooser)))
        } catch (_: Exception) {
            pdfDocument.close()
            Toast.makeText(this, getString(R.string.pdf_generation_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun configureDateTimeField(input: TextInputEditText, onValueChanged: () -> Unit) {
        input.keyListener = null
        input.setOnClickListener { openDateThenTimePicker(input, onValueChanged) }
        input.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) openDateThenTimePicker(input, onValueChanged)
        }
    }

    private fun openDateThenTimePicker(input: TextInputEditText, onValueChanged: () -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        input.setText(dateTimeFormatter.format(calendar.time))
                        input.tag = calendar.timeInMillis
                        onValueChanged()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateAvailabilityBoxButtons() {
        setButtonSelected(box1Button, selectedAvailabilityBoxName == "Box 1")
        setButtonSelected(box2Button, selectedAvailabilityBoxName == "Box 2")
    }

    private fun updateFormBoxButtons() {
        setButtonSelected(formBox1Button, selectedFormBoxName == "Box 1")
        setButtonSelected(formBox2Button, selectedFormBoxName == "Box 2")
    }

    private fun setButtonSelected(button: MaterialButton, isSelected: Boolean) {
        if (isSelected) {
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.brand_accent))
            button.setTextColor(ContextCompat.getColor(this, R.color.brand_primary_dark))
        } else {
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.surface_card))
            button.setTextColor(ContextCompat.getColor(this, R.color.brand_primary))
        }
    }

    private fun updateHomeHeader() {
        todayDateText.text = shortDateFormatter.format(Date())
    }

    private fun buildDateOptions(): List<Long> {
        val start = startOfDay(System.currentTimeMillis())
        return (-1..4).map { offset -> start + (offset * ONE_DAY_MILLIS) }
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
        return if (first != null && second != null && first.after(second)) secondDate to firstDate
        else firstDate to secondDate
    }

    private fun isSameDay(firstMillis: Long, secondMillis: Long): Boolean {
        return startOfDay(firstMillis) == startOfDay(secondMillis)
    }

    private fun formatAmount(value: Double): String {
        return if (value % 1.0 == 0.0) value.toInt().toString()
        else String.format(Locale.getDefault(), "%.2f", value)
    }

    companion object {
        private const val ONE_HOUR_MILLIS = 60L * 60L * 1000L
        private const val ONE_DAY_MILLIS = 24L * ONE_HOUR_MILLIS

        private fun startOfDay(timeMillis: Long): Long {
            return Calendar.getInstance().apply {
                timeInMillis = timeMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
    }
}

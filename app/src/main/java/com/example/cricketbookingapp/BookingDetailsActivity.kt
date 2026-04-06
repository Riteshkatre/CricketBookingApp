package com.example.cricketbookingapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar

class BookingDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allowScreenSharing()
        enableEdgeToEdge()
        setContentView(R.layout.activity_booking_details)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailsRoot)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<MaterialToolbar>(R.id.detailsToolbar).setNavigationOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.detailsBookingTitleText).text = intent.getStringExtra(EXTRA_NAME).orEmpty()
        findViewById<TextView>(R.id.detailsCustomerNameText).text = intent.getStringExtra(EXTRA_CUSTOMER_NAME).orEmpty()
        findViewById<TextView>(R.id.detailsCustomerPhoneText).text = intent.getStringExtra(EXTRA_CUSTOMER_PHONE).orEmpty()
        findViewById<TextView>(R.id.detailsDateText).text = intent.getStringExtra(EXTRA_DATE).orEmpty()
        findViewById<TextView>(R.id.detailsTimeText).text = intent.getStringExtra(EXTRA_TIME_RANGE).orEmpty()
        findViewById<TextView>(R.id.detailsBoxText).text = intent.getStringExtra(EXTRA_BOX_NAME).orEmpty()
        findViewById<TextView>(R.id.detailsPricePerHourText).text =
            getString(R.string.currency_value, intent.getStringExtra(EXTRA_PRICE_PER_HOUR).orEmpty().ifBlank { "0" })
        findViewById<TextView>(R.id.detailsAdvanceText).text =
            getString(R.string.currency_value, intent.getStringExtra(EXTRA_ADVANCE_PAYMENT).orEmpty().ifBlank { "0" })
        findViewById<TextView>(R.id.detailsDiscountText).text =
            getString(R.string.currency_value, intent.getStringExtra(EXTRA_DISCOUNT).orEmpty().ifBlank { "0" })
        findViewById<TextView>(R.id.detailsTotalAmountText).text =
            getString(R.string.currency_value, intent.getStringExtra(EXTRA_AMOUNT).orEmpty().ifBlank { "0" })
        findViewById<TextView>(R.id.detailsCreatedByText).text = intent.getStringExtra(EXTRA_CREATED_BY).orEmpty()
    }

    companion object {
        private const val EXTRA_NAME = "extra_name"
        private const val EXTRA_CUSTOMER_NAME = "extra_customer_name"
        private const val EXTRA_CUSTOMER_PHONE = "extra_customer_phone"
        private const val EXTRA_DATE = "extra_date"
        private const val EXTRA_TIME_RANGE = "extra_time_range"
        private const val EXTRA_BOX_NAME = "extra_box_name"
        private const val EXTRA_PRICE_PER_HOUR = "extra_price_per_hour"
        private const val EXTRA_ADVANCE_PAYMENT = "extra_advance_payment"
        private const val EXTRA_DISCOUNT = "extra_discount"
        private const val EXTRA_AMOUNT = "extra_amount"
        private const val EXTRA_CREATED_BY = "extra_created_by"

        fun createIntent(context: Context, booking: BookingItem): Intent {
            return Intent(context, BookingDetailsActivity::class.java).apply {
                putExtra(EXTRA_NAME, booking.name)
                putExtra(EXTRA_CUSTOMER_NAME, booking.displayCustomerName)
                putExtra(EXTRA_CUSTOMER_PHONE, booking.displayCustomerPhone)
                putExtra(EXTRA_DATE, booking.date)
                putExtra(EXTRA_TIME_RANGE, booking.timeRange)
                putExtra(EXTRA_BOX_NAME, booking.boxName)
                putExtra(EXTRA_PRICE_PER_HOUR, booking.pricePerHour)
                putExtra(EXTRA_ADVANCE_PAYMENT, booking.advancePayment)
                putExtra(EXTRA_DISCOUNT, booking.discount)
                putExtra(EXTRA_AMOUNT, booking.amount)
                putExtra(EXTRA_CREATED_BY, booking.createdByName)
            }
        }
    }
}

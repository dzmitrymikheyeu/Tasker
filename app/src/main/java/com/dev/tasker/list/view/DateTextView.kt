package com.dev.tasker.list.view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import java.text.SimpleDateFormat
import java.util.*

class DateTextView : AppCompatTextView,
        View.OnClickListener,
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    companion object {
        private const val DATE_PATTERN = "yyyy-MM-dd hh:mm"
    }

    var calendar: Calendar? = null

    constructor(context: Context) : super(context) {
        setOnClickListener(this)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setOnClickListener(this)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setOnClickListener(this)
    }

    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        calendar = Calendar.getInstance()
        calendar?.set(Calendar.YEAR, year)
        calendar?.set(Calendar.MONTH, monthOfYear)
        calendar?.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        showTimeDialog()
    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        calendar?.set(Calendar.HOUR_OF_DAY, p1)
        calendar?.set(Calendar.MINUTE, p2)

        val formatter = SimpleDateFormat(DATE_PATTERN)
        text = formatter.format(calendar?.time)

    }

    private fun showTimeDialog() {
        val dialog = TimePickerDialog(
                context,
                this,
                calendar?.get(Calendar.HOUR_OF_DAY)!!,
                calendar?.get(Calendar.MINUTE)!!,
                true)
        dialog.show()
    }

    override fun onClick(v: View) {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val dialog = DatePickerDialog(context, this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
        dialog.datePicker.minDate = System.currentTimeMillis()
        dialog.show()
    }
}

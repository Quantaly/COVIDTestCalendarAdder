package net.quantaly.covidtestcalendaradder

import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.SimpleCursorAdapter
import android.widget.Spinner
import androidx.core.content.ContextCompat

val PERMISSIONS = arrayOf(
    "android.permission.RECEIVE_SMS",
    "android.permission.READ_CALENDAR",
    "android.permission.WRITE_CALENDAR",
)

var nextPermissionRequestCode = 1;

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissionButton = findViewById<Button>(R.id.permission_button)
        permissionButton.setOnClickListener {
            requestPermissions(PERMISSIONS.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
                .toTypedArray(), nextPermissionRequestCode++)
        }


    }

    override fun onStart() {
        super.onStart()

        refreshButton()
        loadSpinner()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        refreshButton()
        loadSpinner()
    }

    fun refreshButton() {
        val permissionButton = findViewById<Button>(R.id.permission_button)
        if (PERMISSIONS.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            permissionButton.isEnabled = false
            permissionButton.text = getString(R.string.permissions_granted)
        } else {
            permissionButton.isEnabled = true
            permissionButton.text = getString(R.string.grant_permissions)
        }
    }

    fun loadSpinner() {
        val calendarSpinner = findViewById<Spinner>(R.id.calendar_spinner)
        if (ContextCompat.checkSelfPermission(
                this,
                "android.permission.READ_CALENDAR"
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val EVENT_PROJECTION = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            )

            val uri = CalendarContract.Calendars.CONTENT_URI
            val cur = contentResolver.query(uri, EVENT_PROJECTION, null, null, null)
            val adapter = SimpleCursorAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                cur,
                arrayOf(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME),
                intArrayOf(android.R.id.text1),
                0,
            ).also(calendarSpinner::setAdapter)

            val pref =
                getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            val selectedId = pref.getLong(getString(R.string.preference_calendar_id), -1)

            var found = false
            for (i in 0..adapter.count) {
                if (adapter.getItemId(i) == selectedId) {
                    calendarSpinner.setSelection(i)
                    found = true
                    break
                }
            }
            if (!found) {
                with(pref.edit()) {
                    putLong(
                        getString(R.string.preference_calendar_id),
                        calendarSpinner.selectedItemId
                    )
                    apply()
                }
            }

            calendarSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    with(pref.edit()) {
                        putLong(getString(R.string.preference_calendar_id), id)
                        apply()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // i don't think this will ever be called...
                }
            }
        }
    }
}
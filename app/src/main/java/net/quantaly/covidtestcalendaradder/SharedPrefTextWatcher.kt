package net.quantaly.covidtestcalendaradder

import android.content.Context
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextWatcher

class SharedPrefTextWatcher(context: Context, private val key: String) : TextWatcher {
    private val pref =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        );

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        with(pref.edit()) {
            putString(key, s.toString())
            apply()
        }
    }

    override fun afterTextChanged(s: Editable?) {

    }
}
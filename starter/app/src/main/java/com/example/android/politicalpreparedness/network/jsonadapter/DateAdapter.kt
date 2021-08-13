package com.example.android.politicalpreparedness.network.jsonadapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateAdapter {

    companion object {
        private const val DATE_PATER_yyyy_MM_dd = "yyyy-MM-dd"
    }

    @FromJson
    fun dateFromJson(electionDay: String): Date {
        val simpleDateFormat = SimpleDateFormat(DATE_PATER_yyyy_MM_dd, Locale.getDefault())
        return simpleDateFormat.parse(electionDay)!!
    }

    @ToJson
    fun dateToJson(electionDay: Date): String {
        val simpleDateFormat = SimpleDateFormat(DATE_PATER_yyyy_MM_dd, Locale.getDefault())
        return simpleDateFormat.format(electionDay)
    }
}
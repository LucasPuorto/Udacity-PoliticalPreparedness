package com.example.android.politicalpreparedness

import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val DATE_PATERN_EEE_MMM_dd_yyyy = "EEE MMM dd yyyy"

@BindingAdapter("date")
fun dateToString(textView: TextView, date: Date?) {
    if (date != null) {
        textView.text = SimpleDateFormat(DATE_PATERN_EEE_MMM_dd_yyyy, Locale.US).format(date)
    }
}

@BindingAdapter("profilePhoto")
fun fetchImageProfilePhoto(view: ImageView, src: String?) {
    src?.let {
        val uri = src.toUri().buildUpon().scheme("https").build()
        Glide.with(view.context)
            .load(uri)
            .placeholder(R.drawable.ic_profile)
            .error(R.drawable.ic_broken_image)
            .circleCrop()
            .into(view)
    }
}

@BindingAdapter("state")
fun Spinner.addressStateValue(value: String?) {
    val adapter = toTypedAdapter<String>(this.adapter as ArrayAdapter<*>)
    val position = when (adapter.getItem(0)) {
        is String -> adapter.getPosition(value)
        else -> this.selectedItemPosition
    }
    if (position >= 0) {
        setSelection(position)
    }
}

inline fun <reified T> toTypedAdapter(adapter: ArrayAdapter<*>): ArrayAdapter<T> {
    return adapter as ArrayAdapter<T>
}
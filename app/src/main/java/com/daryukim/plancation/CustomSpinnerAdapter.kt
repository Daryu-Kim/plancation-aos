package com.daryukim.plancation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class CustomSpinnerAdapter(val context: Context, private val items: Array<String>) : BaseAdapter() {
  var selectedPosition: Int = 0
  override fun getCount(): Int {
    return items.size
  }

  override fun getItem(position: Int): Any {
    return items[position]
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_spinner, parent, false)
    val textView = view.findViewById<TextView>(R.id.spinner_text)
    textView.text = items[position]

    return view
  }

  override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
    val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_spinner, parent, false)
    val textView = view.findViewById<TextView>(R.id.spinner_text)
    textView.text = items[position]

    val checkMark = view.findViewById<ImageView>(R.id.spinner_check_mark)
    checkMark.visibility = if (position == selectedPosition) View.VISIBLE else View.INVISIBLE

    val divider = view.findViewById<View>(R.id.spinner_divider)
    divider.visibility = if (position < items.size - 1) View.VISIBLE else View.GONE

    return view
  }

}

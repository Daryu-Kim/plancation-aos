package com.daryukim.plancation

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager

class NoScrollGridLayoutManager(
  context: Context,
  private val spanCount: Int
): GridLayoutManager(context, spanCount) {
  override fun canScrollVertically(): Boolean {
    return false
  }
}
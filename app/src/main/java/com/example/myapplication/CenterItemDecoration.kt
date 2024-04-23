package com.example.myapplication

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class CenterItemDecoration(private val offset: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.left = offset
        }
    }
}





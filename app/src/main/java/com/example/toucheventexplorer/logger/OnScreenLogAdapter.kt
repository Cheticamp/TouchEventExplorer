package com.example.toucheventexplorer.logger

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import javax.annotation.Nonnull

internal class OnScreenLogAdapter(@param:Nonnull private val mlog: List<EventLogEntry>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val line = AppCompatTextView(parent.context)
        line.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return LogViewHolder(line)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as LogViewHolder
        viewHolder.mTextView.text = mlog[position].spannableString
    }

    override fun getItemCount() = mlog.size

    override fun getItemId(position: Int) = position.toLong()

    internal class LogViewHolder(val mTextView: AppCompatTextView) :
        RecyclerView.ViewHolder(mTextView)
}
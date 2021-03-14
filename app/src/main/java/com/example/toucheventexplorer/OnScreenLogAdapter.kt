package com.example.toucheventexplorer;

import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

class OnScreenLogAdapter extends RecyclerView.Adapter {
    private final List<EventLogEntry> mlog;

    public OnScreenLogAdapter(@Nonnull List<EventLogEntry> log) {
        mlog = log;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AppCompatTextView line = (AppCompatTextView) LayoutInflater.from(parent.getContext())
            .inflate(R.layout.log_entry, parent, false);
        return new LogViewHolder(line);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        LogViewHolder viewHolder = (LogViewHolder) holder;
        SpannableString ss = mlog.get(position).getSpannableString();
        viewHolder.mTextView.setText(ss);
    }

    @Override
    public int getItemCount() {
        return mlog.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        final AppCompatTextView mTextView;

        LogViewHolder(AppCompatTextView view) {
            super(view);
            mTextView = view;
        }
    }
}

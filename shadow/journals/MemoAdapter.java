package com.yahya.shadow.journals;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yahya.shadow.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.MemoViewHolder> {

    private List<Memo> memos;
    private Context context;
    private OnItemClickListener itemClickListener;

    public MemoAdapter(Context context, List<Memo> memos) {
        this.context = context;
        this.memos = memos;
    }

    public interface OnItemClickListener {
        void onItemClick(Memo memo);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public MemoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.memo_item, parent, false);
        return new MemoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoViewHolder holder, int position) {
        Memo memo = memos.get(position);
        holder.titleTextView.setText(memo.getTitle());
        holder.bodyTextView.setText(truncateBody(memo.getBody()));
        holder.timestampTextView.setText(formatTimestamp(memo.getTimestamp()));

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(memo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return memos.size();
    }

    private String truncateBody(String body) {
        if (body.length() > 100) {
            return body.substring(0, 100) + "...";
        }
        return body;
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static class MemoViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView bodyTextView;
        TextView timestampTextView;

        public MemoViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            bodyTextView = itemView.findViewById(R.id.bodyTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}
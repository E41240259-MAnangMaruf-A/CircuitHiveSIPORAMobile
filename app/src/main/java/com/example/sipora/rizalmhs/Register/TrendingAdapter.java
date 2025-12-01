package com.example.sipora.rizalmhs.Register;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sipora.R;

import java.util.List;

public class TrendingAdapter extends RecyclerView.Adapter<TrendingAdapter.VH> {

    private List<TrendingItem> list;
    private OnTrendingClickListener listener;

    public interface OnTrendingClickListener {
        void onClick(String title);
    }

    public void setOnTrendingClickListener(OnTrendingClickListener l) {
        this.listener = l;
    }

    public TrendingAdapter(List<TrendingItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trending, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TrendingItem item = list.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvPill.setText(String.valueOf(item.getCount())); // angka trending

        // klik seluruh card
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item.getTitle());
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView tvTitle, tvPill;

        VH(@NonNull View itemView) {
            super(itemView);

            // sesuai layout kamu
            tvTitle = itemView.findViewById(R.id.tv_trend_title);
            tvPill = itemView.findViewById(R.id.tv_pill);
        }
    }
}

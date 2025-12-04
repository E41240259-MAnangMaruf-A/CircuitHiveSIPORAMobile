package com.example.sipora.rizalmhs.Register;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sipora.R;

import java.util.ArrayList;

public class NotifAdapter extends RecyclerView.Adapter<NotifAdapter.Holder> {

    private Context ctx;
    private ArrayList<NotifModel> list;

    public NotifAdapter(Context ctx, ArrayList<NotifModel> list) {
        this.ctx = ctx;
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx)
                .inflate(R.layout.item_notifikasi, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        NotifModel n = list.get(position);

        h.tvJudul.setText(n.getJudul());
        h.tvIsi.setText(n.getIsi());
        h.tvWaktu.setText(n.getWaktu());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        TextView tvJudul, tvIsi, tvWaktu;

        public Holder(@NonNull View v) {
            super(v);
            tvJudul = v.findViewById(R.id.tvJudul);
            tvIsi = v.findViewById(R.id.tvIsi);
            tvWaktu = v.findViewById(R.id.tvWaktu);
        }
    }
}

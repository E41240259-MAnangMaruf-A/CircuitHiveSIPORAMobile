package com.example.sipora.rizalmhs.Register;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sipora.R;
import java.util.List;

public class NotifAdapter extends RecyclerView.Adapter<NotifAdapter.Holder> {

    List<NotifModel> list;

    public NotifAdapter(List<NotifModel> list) {
        this.list = list;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notifikasi, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(Holder h, int pos) {
        NotifModel n = list.get(pos);
        h.tvJudul.setText(n.getJudul());
        h.tvIsi.setText(n.getIsi());
        h.tvWaktu.setText(n.getWaktu());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        TextView tvJudul, tvIsi, tvWaktu;
        Holder(View v) {
            super(v);
            tvJudul = v.findViewById(R.id.tvJudul);
            tvIsi = v.findViewById(R.id.tvIsi);
            tvWaktu = v.findViewById(R.id.tvWaktu);
        }
    }
}

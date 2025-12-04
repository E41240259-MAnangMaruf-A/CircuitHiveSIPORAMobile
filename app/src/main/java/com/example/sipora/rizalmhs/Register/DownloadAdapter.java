package com.example.sipora.rizalmhs.Register;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sipora.R;

import java.util.List;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.VH> {

    private final List<DownloadModel> list;
    private final OnActionListener listener;

    public interface OnActionListener {
        void onOpen(DownloadModel m);
        void onDelete(DownloadModel m);
    }

    public DownloadAdapter(List<DownloadModel> list, OnActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_download, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH h, int pos) {

        DownloadModel d = list.get(pos);

        h.tvJudul.setText(d.getJudul());
        h.tvDeskripsi.setText(d.getDeskripsi());
        h.tvAuthor.setText(d.getAuthor());
        h.tvTanggal.setText(d.getTanggal());
        h.tvJenis.setText(d.getTipe());
        h.tvKategori.setText("Dokumen");
        h.tvStatus.setText("Selesai");
        h.btnAksi.setOnClickListener(v -> listener.onOpen(d));
        h.btnHapus.setOnClickListener(v -> listener.onDelete(d));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class VH extends RecyclerView.ViewHolder {

        TextView tvJudul, tvDeskripsi, tvAuthor, tvTanggal, tvJenis, tvKategori, tvStatus;
        AppCompatButton btnAksi;
        ImageView btnHapus;

        public VH(View v) {
            super(v);

            tvJudul = v.findViewById(R.id.tvJudul);
            tvDeskripsi = v.findViewById(R.id.tvDeskripsi);
            tvAuthor = v.findViewById(R.id.tvAuthor);
            tvTanggal = v.findViewById(R.id.tvTanggal);
            tvJenis = v.findViewById(R.id.tvJenis);
            tvKategori = v.findViewById(R.id.tvKategori);
            tvStatus = v.findViewById(R.id.tvStatus);

            btnAksi = v.findViewById(R.id.btnAksi);
            btnHapus = v.findViewById(R.id.btnHapus);
        }
    }
}
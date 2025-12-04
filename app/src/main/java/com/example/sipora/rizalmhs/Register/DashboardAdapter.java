package com.example.sipora.rizalmhs.Register;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sipora.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {
    private List<DokumenModel> dokumenList;
    private final Context context;
    private RequestQueue requestQueue;

    public DashboardAdapter(List<DokumenModel> dokumenList, Context context) {
        this.dokumenList = dokumenList;
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dokumen_dashboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DokumenModel doc = dokumenList.get(position);

        holder.tvJudul.setText(doc.getJudul());
        holder.tvDeskripsi.setText(doc.getAbstrak() != null && !doc.getAbstrak().isEmpty() ?
                doc.getAbstrak() : doc.getDeskripsi());
        holder.tvTanggal.setText(formatTanggal(doc.getTanggal()));
        holder.tvStatus.setText(doc.getStatus());
        if (doc.getUploaderName() != null && !doc.getUploaderName().isEmpty()) {
            holder.tvUploader.setText(doc.getUploaderName());
        } else {
            holder.tvUploader.setText("Unknown");
        }

        if (doc.getJurusan() != null && !doc.getJurusan().isEmpty() && !doc.getJurusan().equals("-")) {
            holder.tvJurusan.setText(doc.getJurusan());
        } else {
            holder.tvJurusan.setText("Teknologi Informasi");
        }

        String tahun = doc.getTahun() != null ? doc.getTahun() : extractYear(doc.getTanggal());
        holder.tvTahun.setText(tahun);

        holder.tvDownloadCount.setText(formatNumber(doc.getDownloadCount()) + " Download");
        setStatusColor(holder.tvStatus, doc.getStatus());
        holder.btnDownload.setOnClickListener(v -> {
            if (doc.getFileUrl() != null && !doc.getFileUrl().isEmpty()) {
                try {
                    logDownload(doc.getId(), UserSession.getUserId(context));
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(doc.getFileUrl()));
                    context.startActivity(intent);

                    Toast.makeText(context, "Membuka: " + doc.getJudul(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(context, "Tidak bisa membuka file", Toast.LENGTH_SHORT).show();
                    Log.e("FILE_ERROR", "Error opening file: " + e.getMessage());
                }
            } else {
                Toast.makeText(context, "File tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        });
        holder.btnLihat.setOnClickListener(v -> {
            if (doc.getFileUrl() != null && !doc.getFileUrl().isEmpty()) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(doc.getFileUrl()));
                    context.startActivity(intent);
                    Toast.makeText(context, "Melihat: " + doc.getJudul(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(context, "Tidak bisa membuka file", Toast.LENGTH_SHORT).show();
                    Log.e("FILE_ERROR", "Error opening file: " + e.getMessage());
                }
            } else {
                Toast.makeText(context, "File tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return dokumenList != null ? dokumenList.size() : 0;
    }

    public void updateList(List<DokumenModel> newList) {
        dokumenList = newList;
        notifyDataSetChanged();
    }

    private String formatTanggal(String tanggal) {
        if (tanggal == null || tanggal.isEmpty()) {
            return "-";
        }
        try {
            String[] parts = tanggal.split(" ")[0].split("-");
            if (parts.length == 3) {
                String[] bulan = {"Januari", "Februari", "Maret", "April", "Mei", "Juni",
                        "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
                int bulanIndex = Integer.parseInt(parts[1]) - 1;
                if (bulanIndex >= 0 && bulanIndex < bulan.length) {
                    return parts[2] + " " + bulan[bulanIndex] + " " + parts[0];
                }
            }
            return tanggal;
        } catch (Exception e) {
            return tanggal;
        }
    }

    private String extractYear(String tanggal) {
        if (tanggal == null || tanggal.isEmpty()) {
            return "2025";
        }
        try {
            String[] parts = tanggal.split(" ")[0].split("-");
            if (parts.length >= 1) {
                return parts[0];
            }
            return "2025";
        } catch (Exception e) {
            return "2025";
        }
    }

    private String formatNumber(int number) {
        return String.format("%,d", number);
    }

    private void setStatusColor(TextView tvStatus, String status) {
        int backgroundColor;
        int textColor = ContextCompat.getColor(context, android.R.color.white);

        switch (status.toLowerCase()) {
            case "diterbitkan":
            case "publikasi":
            case "disetujui":
                backgroundColor = ContextCompat.getColor(context, android.R.color.holo_green_dark);
                break;
            case "review":
            case "diperiksa":
            case "menunggu review":
                backgroundColor = ContextCompat.getColor(context, android.R.color.holo_orange_dark);
                break;
            case "ditolak":
                backgroundColor = ContextCompat.getColor(context, android.R.color.holo_red_dark);
                break;
            default:
                backgroundColor = ContextCompat.getColor(context, android.R.color.darker_gray);
                break;
        }

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(backgroundColor);
        drawable.setCornerRadius(dpToPx(20)); // 20dp corner radius

        tvStatus.setBackground(drawable);
        tvStatus.setTextColor(textColor);
    }

    private float dpToPx(float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    private void logDownload(int dokumenId, int userId) {
        String url = "http://192.168.0.180/SIPORAWEB/frontend/log_download.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("DOWNLOAD_LOG", "Download tercatat: " + response);
                },
                error -> {
                    Log.e("DOWNLOAD_LOG", "Gagal mencatat download: " + error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("dokumen_id", String.valueOf(dokumenId));
                params.put("user_id", String.valueOf(userId));
                return params;
            }
        };

        requestQueue.add(request);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvJudul, tvDeskripsi, tvTanggal, tvStatus;
        TextView tvUploader, tvJurusan, tvTahun, tvDownloadCount;
        Button btnDownload, btnLihat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJudul = itemView.findViewById(R.id.tvJudul);
            tvDeskripsi = itemView.findViewById(R.id.tvDeskripsi);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnDownload = itemView.findViewById(R.id.btnDownload);
            btnLihat = itemView.findViewById(R.id.btnLihat);

            // TextView tambahan sesuai prototype
            tvUploader = itemView.findViewById(R.id.tvUploader);
            tvJurusan = itemView.findViewById(R.id.tvJurusan);
            tvTahun = itemView.findViewById(R.id.tvTahun);
            tvDownloadCount = itemView.findViewById(R.id.tvDownloadCount);
        }
    }
}

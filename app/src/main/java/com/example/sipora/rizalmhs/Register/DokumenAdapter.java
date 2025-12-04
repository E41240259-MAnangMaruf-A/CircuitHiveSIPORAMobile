package com.example.sipora.rizalmhs.Register;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sipora.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DokumenAdapter extends RecyclerView.Adapter<DokumenAdapter.ViewHolder> {

    private final Context context;
    private List<DokumenModel> dokumenList;
    private boolean isGridMode = false;
    private RequestQueue requestQueue;

    public interface OnItemClickListener {
        void onDownloadClick(DokumenModel dokumen);
        void onViewClick(DokumenModel dokumen);
    }

    private OnItemClickListener listener;

    public DokumenAdapter(Context context, List<DokumenModel> dokumenList, OnItemClickListener listener) {
        this.context = context;
        this.dokumenList = dokumenList;
        this.listener = listener;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public DokumenAdapter(Context context, List<DokumenModel> dokumenList) {
        this(context, dokumenList, null);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setGridMode(boolean gridMode) {
        this.isGridMode = gridMode;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return isGridMode ? 1 : 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == 1) {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.item_dokumen_grid, parent, false);
        } else {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.item_dokumen, parent, false);
        }

        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DokumenModel doc = dokumenList.get(position);

        if (holder.viewType == 0) { // List mode
            holder.tvJudul.setText(doc.getJudul());
            holder.tvDeskripsi.setText(doc.getAbstrak());
            holder.tvUploader.setText(doc.getUploaderName());
            holder.tvJurusan.setText(doc.getJurusan());
            holder.tvTahun.setText(doc.getTahun());
            holder.tvTema.setText(doc.getTema());
            holder.tvDownloadCount.setText(doc.getDownloadCount() + " Download");
            holder.tvTanggal.setText(doc.getTanggal());

        } else {
            holder.tvTemaGrid.setText(doc.getTema());
            holder.tvJudulGrid.setText(doc.getJudul());
            holder.tvInfoGrid.setText(doc.getUploaderName() + " • " + doc.getJurusan());
            holder.tvTahunGrid.setText(doc.getTahun() + " • " + doc.getDownloadCount() + " download");
        }
        setupClickListeners(holder, doc);
    }

    private void setupClickListeners(ViewHolder holder, DokumenModel doc) {
        if (holder.btnLihat != null) {
            holder.btnLihat.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewClick(doc);
                } else {
                    openFile(doc);
                }
            });
        }

        if (holder.btnDownload != null) {
            holder.btnDownload.setOnClickListener(v -> {
                Log.d("DOKUMEN_ADAPTER", "Tombol Download ditekan: " + doc.getJudul());

                if (listener != null) {
                    listener.onDownloadClick(doc);
                } else {
                    downloadDokumen(doc);
                }
            });
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewClick(doc); // Tanpa notifikasi
            }
        });
    }
    private void downloadDokumen(DokumenModel doc) {
        Log.d("DOKUMEN_ADAPTER", "Download dengan notifikasi: " + doc.getJudul());
        NotificationUtils.sendDownloadNotification(context, doc.getJudul());
        logDownload(doc.getId(), UserSession.getUserId(context));
        openFile(doc);
        Toast.makeText(context, "Mengunduh: " + doc.getJudul(), Toast.LENGTH_SHORT).show();
    }

    private void openFile(DokumenModel doc) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(doc.getFileUrl()));
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Gagal membuka file", Toast.LENGTH_SHORT).show();
        }
    }

    private void logDownload(int dokumenId, int userId) {
        String url = "http://192.168.0.180/SIPORAWEB/frontend/log_download.php";

        Log.d("DOWNLOAD_LOG", "Kirim log: dokumen_id=" + dokumenId + ", user_id=" + userId);

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> Log.d("DOWNLOAD_LOG", "BERHASIL -> " + response),
                error -> Log.e("DOWNLOAD_LOG", "GAGAL -> " + error.toString())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("dokumen_id", String.valueOf(dokumenId));
                p.put("user_id", String.valueOf(userId));
                return p;
            }
        };

        requestQueue.add(req);
    }

    @Override
    public int getItemCount() {
        return dokumenList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTema, tvJudul, tvDeskripsi, tvUploader, tvJurusan, tvTahun, tvTanggal, tvDownloadCount;
        Button btnDownload, btnLihat;

        TextView tvTemaGrid, tvJudulGrid, tvInfoGrid, tvTahunGrid;
        int viewType;

        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;

            if (viewType == 0) {
                tvTema = itemView.findViewById(R.id.tvTema);
                tvJudul = itemView.findViewById(R.id.tvJudul);
                tvDeskripsi = itemView.findViewById(R.id.tvDeskripsi);
                tvUploader = itemView.findViewById(R.id.tvUploader);
                tvJurusan = itemView.findViewById(R.id.tvJurusan);
                tvTahun = itemView.findViewById(R.id.tvTahun);
                tvTanggal = itemView.findViewById(R.id.tvTanggal);
                tvDownloadCount = itemView.findViewById(R.id.tvDownloadCount);
                btnDownload = itemView.findViewById(R.id.btnDownload);
                btnLihat = itemView.findViewById(R.id.btnLihat);

            } else {
                tvTemaGrid = itemView.findViewById(R.id.tvTemaGrid);
                tvJudulGrid = itemView.findViewById(R.id.tvJudulGrid);
                tvInfoGrid = itemView.findViewById(R.id.tvInfoGrid);
                tvTahunGrid = itemView.findViewById(R.id.tvTahunGrid);
            }
        }
    }
}
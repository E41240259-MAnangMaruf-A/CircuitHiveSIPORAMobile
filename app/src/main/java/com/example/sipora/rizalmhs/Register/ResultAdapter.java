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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sipora.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.Holder> {

    private final Context context;
    private List<SearchResult> list;

    public ResultAdapter(Context ctx, List<SearchResult> list) {
        this.context = ctx;
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_document_result, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {

        SearchResult d = list.get(pos);

        // ==========================
        // SET TEXT
        // ==========================
        h.tvTitle.setText(d.judul);
        h.tvDesc.setText(d.abstrak != null ? d.abstrak : "-");
        h.tvUploader.setText(d.uploader != null ? d.uploader : "Unknown");
        h.tvTema.setText(d.nama_tema != null ? d.nama_tema : "-");
        h.tvJurusan.setText(d.nama_jurusan != null ? d.nama_jurusan : "-");
        h.tvProdi.setText(d.nama_prodi != null ? d.nama_prodi : "-");
        h.tvTahun.setText(d.tahun != null ? d.tahun : "-");
        h.tvDownloadCount.setText(d.download_count + " Download");

        // ==========================
        // TAMPILKAN JENIS FILE
        // ==========================
        h.tvFileType.setText(
                d.file_type != null ? d.file_type.toUpperCase() : "-"
        );

        // ==========================
        // BUTTON LIHAT
        // ==========================
        h.btnLihat.setOnClickListener(v -> openFile(d.fileUrl));

        // ==========================
        // BUTTON DOWNLOAD
        // ==========================
        h.btnDownload.setOnClickListener(v -> {
            openFile(d.fileUrl);
            logDownload(d.id, UserSession.getUserId(context));
        });

        // ==========================
        // KLIK ITEM = LIHAT
        // ==========================
        h.itemView.setOnClickListener(v -> openFile(d.fileUrl));
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }


    // ==========================================================
    // OPEN FILE
    // ==========================================================
    private void openFile(String url) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(context, "File tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            context.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(context, "Gagal membuka file", Toast.LENGTH_SHORT).show();
            Log.e("OPEN_FILE", "Error: " + e.getMessage());
        }
    }

    // ==========================================================
    // LOG DOWNLOAD
    // ==========================================================
    private void logDownload(int dokumenId, int userId) {

        String url = "http://10.10.180.226/SIPORAWEB/backend/sipora_api/log_download.php";

        StringRequest req = new StringRequest(
                Request.Method.POST,
                url,
                r -> Log.d("DOWNLOAD_LOG", "Sukses: " + r),
                e -> Log.e("DOWNLOAD_LOG", "Gagal: " + e.toString())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("dokumen_id", String.valueOf(dokumenId));
                p.put("user_id", String.valueOf(userId));
                return p;
            }
        };

        Volley.newRequestQueue(context).add(req);
    }


    // ==========================================================
    // HOLDER
    // ==========================================================
    public static class Holder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvDesc, tvUploader, tvTema, tvJurusan,
                tvProdi, tvTahun, tvDownloadCount, tvFileType;

        Button btnDownload, btnLihat;

        public Holder(@NonNull View v) {
            super(v);

            tvTitle = v.findViewById(R.id.tv_title);
            tvDesc = v.findViewById(R.id.tv_desc);
            tvUploader = v.findViewById(R.id.tv_uploader);
            tvTema = v.findViewById(R.id.tv_tema);
            tvJurusan = v.findViewById(R.id.tv_jurusan);
            tvProdi = v.findViewById(R.id.tv_prodi);
            tvTahun = v.findViewById(R.id.tv_tahun);
            tvDownloadCount = v.findViewById(R.id.tv_download_count);

            tvFileType = v.findViewById(R.id.tv_file_type);

            btnDownload = v.findViewById(R.id.btnDownload);
            btnLihat = v.findViewById(R.id.btnLihat);
        }
    }
}

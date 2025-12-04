package com.example.sipora.rizalmhs.Register;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sipora.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BrowseActivity extends AppCompatActivity {

    private RecyclerView recyclerViewDokumen;
    private DokumenAdapter dokumenAdapter;
    private List<DokumenModel> dokumenList;

    private boolean isGrid = false;

    private TextView textJumlahDokumen;

    private static final String URL_BROWSE = "http://192.168.0.180/SIPORAWEB/frontend/browse.php";
    private static final String URL_TEMA = "http://192.168.0.180/SIPORAWEB/frontend/get_tema.php";
    private static final String URL_TAHUN = "http://192.168.0.180/SIPORAWEB/frontend/get_tahun.php";
    private static final String URL_JURUSAN = "http://192.168.0.180/SIPORAWEB/frontend/get_jurusan.php";

    String currentTema = "";
    String currentTahun = "";
    String currentJurusan = "";
    String currentSort = "TERBARU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 50);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomNavigation), (view, insets2) -> {
            view.setPadding(0, 0, 0, 0);
            return insets2;
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            startActivity(new Intent(this, UploadActivity.class));
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        recyclerViewDokumen = findViewById(R.id.listViewDokumen);
        textJumlahDokumen = findViewById(R.id.textJumlahDokumen); // <<< DISINI FIX 100%

        ImageView btnGrid = findViewById(R.id.btnGrid);
        ImageView btnFilter = findViewById(R.id.btnFilter);
        LinearLayout btnSort = findViewById(R.id.btnSort);

        dokumenList = new ArrayList<>();
        dokumenAdapter = new DokumenAdapter(this, dokumenList);

        recyclerViewDokumen.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDokumen.setAdapter(dokumenAdapter);

        loadDocuments();

        btnGrid.setOnClickListener(v -> {
            isGrid = !isGrid;
            dokumenAdapter.setGridMode(isGrid);

            if (isGrid) {
                recyclerViewDokumen.setLayoutManager(new GridLayoutManager(this, 2));
                Toast.makeText(this, "Grid View", Toast.LENGTH_SHORT).show();
            } else {
                recyclerViewDokumen.setLayoutManager(new LinearLayoutManager(this));
                Toast.makeText(this, "List View", Toast.LENGTH_SHORT).show();
            }
        });


        btnFilter.setOnClickListener(v -> showFilterSheet());

        btnSort.setOnClickListener(v -> showSortSheet());

        setupBottomNav();
    }

    private void loadDocuments() {
        String url = URL_BROWSE +
                "?tema=" + currentTema +
                "&tahun=" + currentTahun +
                "&jurusan=" + currentJurusan +
                "&sort=" + currentSort;

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);

                        if (!obj.getString("status").equals("success")) {
                            Toast.makeText(this, "Gagal memuat dokumen", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // ====== AMBIL TOTAL DOKUMEN ======
                        int total = obj.optInt("total", 0);
                        textJumlahDokumen.setText(total + " Dokumen");

                        // ====== PARSE LIST DOKUMEN ======
                        JSONArray arr = obj.getJSONArray("documents");
                        dokumenList.clear();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject item = arr.getJSONObject(i);

                            dokumenList.add(new DokumenModel(
                                    item.getInt("id"),
                                    item.getString("judul"),
                                    item.getString("deskripsi"),
                                    item.getString("tanggal"),
                                    item.getString("file_type"),
                                    item.getString("status"),
                                    item.getString("file_url"),
                                    item.getString("uploader_name"),
                                    item.getString("nama_tema"),
                                    item.getString("nama_jurusan"),
                                    item.getString("nama_prodi"),
                                    item.getInt("download_count"),
                                    item.getString("abstrak"),
                                    item.getString("tahun")
                            ));
                        }

                        dokumenAdapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Toast.makeText(this, "JSON Error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void showSortSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        sheet.setContentView(R.layout.bottom_sheet_sort);

        sheet.findViewById(R.id.sortTerbaru).setOnClickListener(v -> {
            currentSort = "TERBARU";
            loadDocuments();
            sheet.dismiss();
        });

        sheet.findViewById(R.id.sortTerlama).setOnClickListener(v -> {
            currentSort = "TERLAMA";
            loadDocuments();
            sheet.dismiss();
        });

        sheet.findViewById(R.id.sortAZ).setOnClickListener(v -> {
            currentSort = "AZ";
            loadDocuments();
            sheet.dismiss();
        });

        sheet.findViewById(R.id.sortDownload).setOnClickListener(v -> {
            currentSort = "DOWNLOAD";
            loadDocuments();
            sheet.dismiss();
        });

        sheet.show();
    }

    private void showFilterSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        sheet.setContentView(R.layout.bottom_sheet_filter);

        Spinner spTema = sheet.findViewById(R.id.spTema);
        Spinner spTahun = sheet.findViewById(R.id.spTahun);
        Spinner spJurusan = sheet.findViewById(R.id.spJurusan);
        Button btnTerapkan = sheet.findViewById(R.id.btnTerapkan);
        Button btnReset = sheet.findViewById(R.id.btnReset);

        loadSpinner(URL_TEMA, spTema, "Semua Tema");
        loadSpinner(URL_TAHUN, spTahun, "Semua Tahun");
        loadSpinner(URL_JURUSAN, spJurusan, "Semua Jurusan");

        btnTerapkan.setOnClickListener(v -> {
            currentTema = spTema.getSelectedItemPosition() == 0 ? "" : spTema.getSelectedItem().toString();
            currentTahun = spTahun.getSelectedItemPosition() == 0 ? "" : spTahun.getSelectedItem().toString();
            currentJurusan = spJurusan.getSelectedItemPosition() == 0 ? "" : spJurusan.getSelectedItem().toString();
            loadDocuments();
            sheet.dismiss();
        });

        btnReset.setOnClickListener(v -> {
            currentTema = "";
            currentTahun = "";
            currentJurusan = "";
            loadDocuments();
            sheet.dismiss();
        });

        sheet.show();
    }

    private void loadSpinner(String url, Spinner spinner, String firstOption) {
        StringRequest req = new StringRequest(url, response -> {
            try {
                JSONArray arr = new JSONArray(response);
                List<String> list = new ArrayList<>();
                list.add(firstOption);

                for (int i = 0; i < arr.length(); i++) {
                    list.add(arr.getJSONObject(i).getString("nama"));
                }

                spinner.setAdapter(new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item, list));

            } catch (Exception e) {
                Toast.makeText(this, "Gagal memuat data filter", Toast.LENGTH_SHORT).show();
            }
        }, error -> {});
        Volley.newRequestQueue(this).add(req);
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_browse);

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            if (id == R.id.nav_upload) {
                startActivity(new Intent(this, UploadActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            if (id == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            if (id == R.id.nav_download) {
                startActivity(new Intent(this, DownloadActivity.class));
                overridePendingTransition(0, 0);
                 finish();
                return true;
            }

            return id == R.id.nav_browse;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDocuments();
    }
    public void sendNotif(int userId, String judul, String isi) {
        String url = "http://10.10.4.51/SIPORAWEB/frontend/insert_notifikasi.php";

        StringRequest req = new StringRequest(Request.Method.POST, url,
                r -> Log.d("NOTIF","Sent"),
                e -> Log.e("NOTIF","Error "+e.toString())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("user_id", String.valueOf(userId));
                p.put("judul", judul);
                p.put("isi", isi);
                return p;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

}

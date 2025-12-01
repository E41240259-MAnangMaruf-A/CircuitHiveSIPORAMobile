package com.example.sipora.rizalmhs.Register;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sipora.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;

    private TextView tvTotalDokumen, tvDownloadBulanIni, tvPenggunaAktif, tvUploadBaru;

    private RecyclerView recyclerDokumen;
    private DokumenAdapter adapter;

    private EditText etSearch;
    private TextView tvLihatSemua;   // <<< TAMBAHAN DI SINI

    private ArrayList<DokumenModel> originalList = new ArrayList<>();

    private Button btnUpload;

    private static final String URL_DASHBOARD =
            "http://192.168.1.45/SIPORAWEB/backend/sipora_api/dashboard.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        setupBottomNavigation();
        setupHeader();
        setupUploadButton();
        setupStatistikCards();
        setupRecyclerView();
        setupSearchBar();
        setupLihatSemuaButton();

        loadDashboard();
    }
    private void setupLihatSemuaButton() {
        tvLihatSemua = findViewById(R.id.tvLihatSemua);

        tvLihatSemua.setOnClickListener(v -> {
            // Arahkan ke BrowseActivity
            Intent intent = new Intent(DashboardActivity.this, BrowseActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_home);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_upload) startActivity(new Intent(this, UploadActivity.class));
            else if (id == R.id.nav_browse) startActivity(new Intent(this, BrowseActivity.class));
            else if (id == R.id.nav_search) startActivity(new Intent(this, SearchActivity.class));
            else if (id == R.id.nav_download) startActivity(new Intent(this, DownloadActivity.class));

            return true;
        });
    }

    private void setupHeader() {
        tvWelcome = findViewById(R.id.tvWelcome);
        String name = UserSession.getUserName(this);

        if (name == null || name.trim().isEmpty())
            name = "Pengguna";

        tvWelcome.setText("Selamat Datang, " + name + "!");
    }

    private void setupUploadButton() {
        btnUpload = findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, UploadActivity.class))
        );
    }

    private void setupStatistikCards() {
        tvTotalDokumen = findViewById(R.id.statTotalDokumen).findViewById(R.id.tvValue);
        tvDownloadBulanIni = findViewById(R.id.statDownload).findViewById(R.id.tvValue);
        tvPenggunaAktif = findViewById(R.id.statPengguna).findViewById(R.id.tvValue);
        tvUploadBaru = findViewById(R.id.statUpload).findViewById(R.id.tvValue);
    }

    private void setupRecyclerView() {
        recyclerDokumen = findViewById(R.id.recyclerDokumen);
        recyclerDokumen.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DokumenAdapter(this, new ArrayList<>());
        recyclerDokumen.setAdapter(adapter);
    }

    private void setupSearchBar() {
        etSearch = findViewById(R.id.etSearch);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDashboard(s.toString());
            }
        });
    }

    private void filterDashboard(String query) {
        query = query.toLowerCase().trim();
        ArrayList<DokumenModel> filtered = new ArrayList<>();

        for (DokumenModel d : originalList) {
            if (
                    d.getJudul().toLowerCase().contains(query) ||
                            d.getAbstrak().toLowerCase().contains(query) ||
                            d.getUploaderName().toLowerCase().contains(query) ||
                            d.getJurusan().toLowerCase().contains(query) ||
                            d.getTema().toLowerCase().contains(query) ||
                            d.getTahun().toLowerCase().contains(query)
            ) {
                filtered.add(d);
            }
        }

        adapter = new DokumenAdapter(this, filtered);
        recyclerDokumen.setAdapter(adapter);
    }

    private void loadDashboard() {
        int userId = UserSession.getUserId(this);
        String url = URL_DASHBOARD + "?user_id=" + userId;

        StringRequest req = new StringRequest(Request.Method.GET, url,
                resp -> {
                    try {
                        JSONObject obj = new JSONObject(resp);

                        if (!"success".equals(obj.optString("status"))) {
                            Toast.makeText(this, "Gagal memuat dashboard", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        updateStatistik(obj.getJSONObject("summary"));
                        updateDokumen(obj.getJSONArray("recent_documents"));

                    } catch (JSONException e) {
                        Log.e("DASHBOARD_JSON", e.toString());
                    }
                },
                err -> Log.e("DASHBOARD_ERR", err.toString())
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void updateStatistik(JSONObject s) throws JSONException {
        tvTotalDokumen.setText(String.valueOf(s.getJSONObject("totalDokumen").getInt("value")));
        tvDownloadBulanIni.setText(String.valueOf(s.getJSONObject("downloadBulanIni").getInt("value")));
        tvPenggunaAktif.setText(String.valueOf(s.getJSONObject("penggunaAktif").getInt("value")));
        tvUploadBaru.setText(String.valueOf(s.getJSONObject("uploadBaru").getInt("value")));
    }

    private void updateDokumen(JSONArray arr) throws JSONException {
        originalList.clear();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject it = arr.getJSONObject(i);

            originalList.add(new DokumenModel(
                    it.optInt("id"),
                    it.optString("judul"),
                    it.optString("deskripsi"),
                    it.optString("tanggal"),
                    it.optString("file_type"),
                    it.optString("status"),
                    it.optString("file_url"),
                    it.optString("uploader_name"),
                    it.optString("nama_tema"),
                    it.optString("nama_jurusan"),
                    it.optString("nama_prodi"),
                    it.optInt("download_count"),
                    it.optString("abstrak"),
                    it.optString("tahun")
            ));
        }

        adapter = new DokumenAdapter(this, originalList);
        recyclerDokumen.setAdapter(adapter);
    }
}

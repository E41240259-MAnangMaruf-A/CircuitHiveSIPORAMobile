package com.example.sipora.rizalmhs.Register;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sipora.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    RecyclerView rvRecent, rvTrending, rvResult;
    RecentAdapter recentAdapter;
    TrendingAdapter trendingAdapter;
    ResultAdapter resultAdapter;

    List<SearchResult> resultList = new ArrayList<>();

    EditText etSearch;
    Button btnCari;
    ImageView btnClearRecent, btnClearSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 50);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottom_nav), (view, insets2) -> {
            view.setPadding(0, 0, 0, 0);
            return insets2;
        });

        etSearch = findViewById(R.id.et_search);
        btnCari = findViewById(R.id.btn_cari);
        rvRecent = findViewById(R.id.rv_recent);
        rvTrending = findViewById(R.id.rv_trending);
        rvResult = findViewById(R.id.rv_result);
        btnClearRecent = findViewById(R.id.btn_clear_recent);
        btnClearSearch = findViewById(R.id.btn_clear_search);
        ImageView btnBack = findViewById(R.id.btnBack);

        rvRecent.setLayoutManager(new LinearLayoutManager(this));
        rvTrending.setLayoutManager(new LinearLayoutManager(this));
        rvResult.setLayoutManager(new LinearLayoutManager(this));

        rvResult.setVisibility(View.GONE);
        btnClearSearch.setVisibility(View.GONE);

        loadRecentTrending();

        resultAdapter = new ResultAdapter(this, resultList);
        rvResult.setAdapter(resultAdapter);

        btnCari.setOnClickListener(v -> {
            String q = etSearch.getText().toString().trim();
            if (!q.isEmpty()) {

                rvRecent.setVisibility(View.GONE);
                rvTrending.setVisibility(View.GONE);

                rvResult.setVisibility(View.VISIBLE);

                saveRecent(q);
                searchDocument(q);
            }
        });


        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 0) {
                    btnClearSearch.setVisibility(View.VISIBLE);
                    rvRecent.setVisibility(View.GONE);
                    rvTrending.setVisibility(View.GONE);
                } else {
                    btnClearSearch.setVisibility(View.GONE);

                    rvResult.setVisibility(View.GONE);
                    rvRecent.setVisibility(View.VISIBLE);
                    rvTrending.setVisibility(View.VISIBLE);
                }
            }
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");

            resultList.clear();
            resultAdapter.notifyDataSetChanged();

            rvResult.setVisibility(View.GONE);

            loadRecentTrending();

            btnClearSearch.setVisibility(View.GONE);
        });

        btnClearRecent.setOnClickListener(v -> {
            if (recentAdapter != null) {
                recentAdapter.clearAll();
            }
            clearRecentFromServer();
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            startActivity(new Intent(this, BrowseActivity.class));
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_nav);
        bottomNavigation.setSelectedItemId(R.id.nav_search);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

            if (id == R.id.nav_home) intent = new Intent(this, DashboardActivity.class);
            else if (id == R.id.nav_upload) intent = new Intent(this, UploadActivity.class);
            else if (id == R.id.nav_browse) intent = new Intent(this, BrowseActivity.class);
            else if (id == R.id.nav_download) intent = new Intent(this, DownloadActivity.class);
            else if (id == R.id.nav_search) return true;

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
            return true;
        });
    }

    private void loadRecentTrending() {

        String url = "http://192.168.0.180/SIPORAWEB/frontend/recent_search.php?user_id="
                + UserSession.getUserId(this);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (!response.getString("status").equals("success")) return;

                        // RECENT
                        JSONArray recentArr = response.getJSONArray("recent");
                        List<String> recentList = new ArrayList<>();
                        for (int i = 0; i < Math.min(5, recentArr.length()); i++) {
                            recentList.add(recentArr.getString(i));
                        }

                        recentAdapter = new RecentAdapter(recentList);
                        rvRecent.setAdapter(recentAdapter);

                        rvRecent.setVisibility(View.VISIBLE);
                        recentAdapter.setOnRecentClickListener(new RecentAdapter.OnRecentClickListener() {
                            @Override
                            public void onItemClick(String q) {
                                etSearch.setText(q);
                                rvRecent.setVisibility(View.GONE);
                                rvTrending.setVisibility(View.GONE);
                                rvResult.setVisibility(View.VISIBLE);
                                searchDocument(q);
                            }

                            @Override
                            public void onDelete(String keyword, int position) {
                                deleteRecentItem(keyword); // Soft delete ke server
                                recentAdapter.deleteItem(position); // Hapus di UI
                            }
                        });

                        JSONArray trendArr = response.getJSONArray("trending");
                        List<TrendingItem> trendList = new ArrayList<>();

                        for (int i = 0; i < Math.min(5, trendArr.length()); i++) {
                            JSONObject o = trendArr.getJSONObject(i);

                            trendList.add(new TrendingItem(
                                    o.getString("keyword"),
                                    o.getInt("total")
                            ));
                        }

                        trendingAdapter = new TrendingAdapter(trendList);
                        rvTrending.setAdapter(trendingAdapter);

                        rvTrending.setVisibility(View.VISIBLE);

                        trendingAdapter.setOnTrendingClickListener(t -> {
                            etSearch.setText(t);
                            rvRecent.setVisibility(View.GONE);
                            rvTrending.setVisibility(View.GONE);
                            rvResult.setVisibility(View.VISIBLE);
                            searchDocument(t);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        );

        queue.add(req);
    }

    private void deleteRecentItem(String keyword) {
        String url = "http://192.168.0.180/SIPORAWEB/frontend/delete_recent_item.php";

        StringRequest req = new StringRequest(
                Request.Method.POST,
                url,
                r -> Log.d("DELETE_RECENT", "Success: " + r),
                e -> Log.e("DELETE_RECENT", "Error: " + e.toString())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("user_id", String.valueOf(UserSession.getUserId(SearchActivity.this)));
                p.put("keyword", keyword);
                return p;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    private void saveRecent(String keyword) {
        String url = "http://192.168.0.180/SIPORAWEB/frontend/save_recent.php";

        Log.d("DEBUG_SEARCH", "Saving keyword: " + keyword);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                response -> Log.d("DEBUG_SEARCH", "Response: " + response),
                error -> Log.e("DEBUG_SEARCH", "Error: " + error.toString())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(UserSession.getUserId(SearchActivity.this)));
                params.put("keyword", keyword);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void clearRecentFromServer() {
        String url = "http://192.168.0.180/SIPORAWEB/frontend/clear_recent.php"
                + "?user_id=" + UserSession.getUserId(this);

        Volley.newRequestQueue(this).add(new JsonObjectRequest(Request.Method.GET, url, null,
                r -> {}, e -> {}));
    }

    private void searchDocument(String query) {

        String url = "http://192.168.0.180/SIPORAWEB/frontend/search_mobile.php?q=" + query;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    resultList.clear();

                    try {
                        if (!response.getString("status").equals("success")) {

                            sendNotif(
                                    "Pencarian Dokumen",
                                    "Pencarian '" + query + "' tidak ditemukan."
                            );

                            rvResult.setVisibility(View.GONE);
                            return;
                        }
                        JSONArray arr = response.getJSONArray("documents");

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);

                            SearchResult item = new SearchResult();
                            item.id = o.getInt("id");
                            item.judul = o.getString("judul");
                            item.deskripsi = o.getString("deskripsi");
                            item.tanggal = o.getString("tanggal");
                            item.file_type = o.getString("file_type");
                            item.status = o.getString("status");
                            item.fileUrl = o.getString("file_url");
                            item.abstrak = o.getString("abstrak");
                            item.tahun = o.getString("tahun");
                            item.uploader = o.getString("uploader_name");
                            item.nama_tema = o.getString("nama_tema");
                            item.nama_jurusan = o.getString("nama_jurusan");
                            item.nama_prodi = o.getString("nama_prodi");
                            item.download_count = o.getInt("download_count");

                            resultList.add(item);
                        }

                        rvResult.setVisibility(View.VISIBLE);
                        resultAdapter.notifyDataSetChanged();

                        sendNotif(
                                "Pencarian Dokumen",
                                "Anda mencari dokumen dengan kata kunci: '" + query + "'"
                        );

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        );

        queue.add(req);
    }


    private void sendNotif(String judul, String isi) {
        String url = "http://192.168.0.180/SIPORAWEB/frontend/insert_notifikasi.php";

        StringRequest req = new StringRequest(
                Request.Method.POST,
                url,
                r -> Log.d("NOTIF", "Terkirim: " + r),
                e -> Log.e("NOTIF", "Error: " + e.toString())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("user_id", String.valueOf(UserSession.getUserId(SearchActivity.this)));
                p.put("judul", judul);
                p.put("isi", isi);
                return p;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

}

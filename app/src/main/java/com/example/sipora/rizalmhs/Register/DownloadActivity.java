package com.example.sipora.rizalmhs.Register;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sipora.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DownloadActivity extends AppCompatActivity {

    private static final String TAG = "DOWNLOAD_ACTIVITY";
    RecyclerView recycler;
    TextView txtEmpty;

    Button tabAll, tabDone, tabNewest, tabOldest;

    List<DownloadModel> listAll = new ArrayList<>();
    List<DownloadModel> listDone = new ArrayList<>();
    List<DownloadModel> listNewest = new ArrayList<>();
    List<DownloadModel> listOldest = new ArrayList<>();

    String currentTab = "all";

    private static final String BASE_URL = "http://10.46.104.1/SIPORAWEB/frontend/get_download.php?user_id=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
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
        UserSession.debugSession(this);

        int userId = UserSession.getUserId(this);
        Log.d(TAG, "User ID from session: " + userId);

        if (userId <= 0) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        txtEmpty = findViewById(R.id.txtEmpty);

        tabAll = findViewById(R.id.tabAll);
        tabDone = findViewById(R.id.tabDone);
        tabNewest = findViewById(R.id.tabNewest);
        tabOldest = findViewById(R.id.tabOldest);

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        setupTabs();
        loadDownloads();
        setupBottomNav();
    }

    private void setupTabs() {
        tabAll.setOnClickListener(v -> show("all"));
        tabDone.setOnClickListener(v -> show("done"));
        tabNewest.setOnClickListener(v -> show("newest"));
        tabOldest.setOnClickListener(v -> show("oldest"));
    }

    private void loadDownloads() {
        int userId = UserSession.getUserId(this);
        Log.d(TAG, "Loading downloads for user: " + userId);

        if (userId <= 0) {
            txtEmpty.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Sesi login tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + userId;

        StringRequest req = new StringRequest(Request.Method.GET, url, res -> {
            try {
                listAll.clear();
                listDone.clear();
                listNewest.clear();
                listOldest.clear();

                Log.d(TAG, "Response: " + res);
                JSONObject obj = new JSONObject(res);

                if (!obj.getString("status").equals("success")) {
                    txtEmpty.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Server returned error: " + obj.getString("message"));
                    return;
                }

                JSONArray arr = obj.getJSONArray("downloads");
                Log.d(TAG, "Total downloads: " + arr.length());

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);

                    DownloadModel d = new DownloadModel(
                            o.getInt("id"),
                            o.getString("judul"),
                            o.getString("abstrak"),
                            o.getString("uploader"),
                            o.getString("tipe"),
                            o.getString("tanggal"),
                            "selesai",
                            o.getString("file_url")
                    );

                    listAll.add(d);
                    if ("selesai".equalsIgnoreCase(d.getStatus())) {
                        listDone.add(d);
                    }
                }

                listNewest.addAll(listAll);
                Collections.sort(listNewest, (d1, d2) -> d2.getTanggal().compareTo(d1.getTanggal()));
                listOldest.addAll(listAll);
                Collections.sort(listOldest, Comparator.comparing(DownloadModel::getTanggal));

                show(currentTab);
                Log.d(TAG, "Data loaded successfully. Total: " + listAll.size());

            } catch (Exception e) {
                Log.e(TAG, "Error parsing data: " + e.getMessage());
                e.printStackTrace();
                txtEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        }, err -> {
            Log.e(TAG, "Network error: " + err.toString());
            txtEmpty.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show();
        });

        Volley.newRequestQueue(this).add(req);
    }

    private void show(String tab) {
        currentTab = tab;
        Log.d(TAG, "Showing tab: " + tab);

        List<DownloadModel> data;

        switch (tab) {
            case "done":
                data = listDone;
                break;
            case "newest":
                data = listNewest;
                break;
            case "oldest":
                data = listOldest;
                break;
            case "all":
            default:
                data = listAll;
        }

        txtEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
        updateTabUI();

        recycler.setAdapter(new DownloadAdapter(data, new DownloadAdapter.OnActionListener() {
            @Override
            public void onOpen(DownloadModel m) {
                openFileWithoutNotification(m);
            }

            @Override
            public void onDelete(DownloadModel m) {
                showDeleteDialog(m);
            }
        }));
    }

    private void updateTabUI() {
        int inactiveBg = getResources().getColor(R.color.tab_inactive_bg);
        int activeBg = getResources().getColor(R.color.tab_active_bg);
        int inactiveText = getResources().getColor(R.color.tab_inactive_text);
        int activeText = getResources().getColor(R.color.tab_active_text);

        tabAll.setBackgroundColor(inactiveBg);
        tabDone.setBackgroundColor(inactiveBg);
        tabNewest.setBackgroundColor(inactiveBg);
        tabOldest.setBackgroundColor(inactiveBg);

        tabAll.setTextColor(inactiveText);
        tabDone.setTextColor(inactiveText);
        tabNewest.setTextColor(inactiveText);
        tabOldest.setTextColor(inactiveText);

        switch (currentTab) {
            case "all":
                tabAll.setBackgroundColor(activeBg);
                tabAll.setTextColor(activeText);
                break;
            case "done":
                tabDone.setBackgroundColor(activeBg);
                tabDone.setTextColor(activeText);
                break;
            case "newest":
                tabNewest.setBackgroundColor(activeBg);
                tabNewest.setTextColor(activeText);
                break;
            case "oldest":
                tabOldest.setBackgroundColor(activeBg);
                tabOldest.setTextColor(activeText);
                break;
        }
    }

    private void openFileWithoutNotification(DownloadModel m) {
        String fileName = m.getJudul();
        String fileUrl = m.getFileUrl();

        Log.d(TAG, "Opening file (NO NOTIFICATION): " + fileName);
        Log.d(TAG, "File URL: " + fileUrl);

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(fileUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Toast.makeText(this, "Membuka " + fileName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error opening file: " + e.getMessage());
            Toast.makeText(this, "Gagal membuka file", Toast.LENGTH_SHORT).show();
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
                startActivity(browserIntent);
            } catch (Exception e2) {
                Log.e(TAG, "Also failed in browser: " + e2.getMessage());
                Toast.makeText(this, "Aplikasi untuk membuka file tidak ditemukan", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDeleteDialog(DownloadModel m) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Dokumen")
                .setMessage("Yakin ingin menghapus '" + m.getJudul() + "' dari riwayat download?")
                .setPositiveButton("Hapus", (d, w) -> deleteItemWithNotification(m))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteItemWithNotification(DownloadModel m) {
        String fileName = m.getJudul();
        int fileId = m.getId();

        Log.d(TAG, "Starting delete with notification - ID: " + fileId + ", Name: " + fileName);
        sendDeleteNotification(fileName);
        deleteFromServer(fileId, fileName);
    }

    private void sendDeleteNotification(String fileName) {
        int userId = UserSession.getUserId(this);

        if (userId <= 0) {
            Log.e(TAG, "Cannot send notification: User ID invalid");
            return;
        }

        Log.d(TAG, "Sending DELETE notification for: " + fileName);
        NotificationUtils.sendDeleteNotification(this, fileName);
    }

    private void deleteFromServer(int fileId, String fileName) {
        String deleteUrl = "http://10.46.104.1/SIPORAWEB/frontend/delete_download.php?id=" + fileId;

        Log.d(TAG, "Delete URL: " + deleteUrl);

        StringRequest deleteRequest = new StringRequest(Request.Method.GET, deleteUrl,
                response -> {
                    Log.d(TAG, "Delete response: " + response);
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("status").equals("success")) {
                            Toast.makeText(this, "✓ " + fileName + " dihapus", Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMsg = json.optString("message", "Gagal menghapus");
                            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing delete response: " + e.getMessage());
                        Toast.makeText(this, "Dokumen dihapus", Toast.LENGTH_SHORT).show();
                    }

                    loadDownloads();
                },
                error -> {
                    Log.e(TAG, "Delete request error: " + error.toString());
                    Toast.makeText(this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show();
                    loadDownloads();
                });

        Volley.newRequestQueue(this).add(deleteRequest);
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_download);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home)
                startActivity(new Intent(this, DashboardActivity.class));
            else if (id == R.id.nav_upload)
                startActivity(new Intent(this, UploadActivity.class));
            else if (id == R.id.nav_browse)
                startActivity(new Intent(this, BrowseActivity.class));
            else if (id == R.id.nav_search)
                startActivity(new Intent(this, SearchActivity.class));
            else
                return true;

            finish();
            overridePendingTransition(0, 0);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Refreshing download list");
        if (!UserSession.isLoggedIn(this)) {
            Toast.makeText(this, "Sesi habis, silakan login kembali", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        loadDownloads();
    }
}
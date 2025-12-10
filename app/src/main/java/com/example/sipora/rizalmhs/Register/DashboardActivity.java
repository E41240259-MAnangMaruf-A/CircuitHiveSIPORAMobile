package com.example.sipora.rizalmhs.Register;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity implements DokumenAdapter.OnItemClickListener {

    private ImageView btnProfile, btnNotif;
    private ImageView btnHelp;
    private TextView tvSipora, tvWelcome, tvSubtitle;
    private Button btnUpload;
    private EditText etSearch;
    private View statTotalDokumen, statDownload;
    private TextView tvTotalDokumen, tvDownloadBulanIni;
    private TextView tvDokumen, tvLihatSemua;
    private RecyclerView recyclerDokumen;
    private DokumenAdapter adapter;
    private BottomNavigationView bottomNavigation;
    private ArrayList<DokumenModel> originalList = new ArrayList<>();

    private static final String URL_DASHBOARD =
            "http://10.46.104.1/SIPORAWEB/frontend/dashboard_mobile.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupWindow();

        setContentView(R.layout.activity_dashboard);
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

        initViews();

        bottomNavigation = findViewById(R.id.bottomNavigation);
        if (!UserSession.isLoggedIn(this)) {
            Log.w("DASHBOARD", "User not logged in, redirecting to Login");
            redirectToLogin();
            return;
        }
        UserSession.debugSession(this);

        setupProfileButton();
        setupNotificationButton();
        setupHelpButton();

        setupHeader();
        setupUploadButton();
        setupStatistikCards();
        setupRecyclerView();
        setupSearchBar();
        setupLihatSemuaButton();
        setupBottomNavigation();
        loadDashboard();
    }

    private void setupWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int flags = window.getDecorView().getSystemUiVisibility();
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                window.getDecorView().setSystemUiVisibility(flags);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int flags = window.getDecorView().getSystemUiVisibility();
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                window.getDecorView().setSystemUiVisibility(flags);
            }
        }
    }

    private void initViews() {
        try {
            btnProfile = findViewById(R.id.btnProfile);
            btnNotif = findViewById(R.id.btnNotif);
            btnHelp = findViewById(R.id.btnHelp);

            tvSipora = findViewById(R.id.tvSipora);

            tvWelcome = findViewById(R.id.tvWelcome);
            tvSubtitle = findViewById(R.id.tvSubtitle);
            btnUpload = findViewById(R.id.btnUpload);

            etSearch = findViewById(R.id.etSearch);

            statTotalDokumen = findViewById(R.id.statTotalDokumen);
            statDownload = findViewById(R.id.statDownload);

            if (statTotalDokumen != null) {
                tvTotalDokumen = statTotalDokumen.findViewById(R.id.tvValue);
            }
            if (statDownload != null) {
                tvDownloadBulanIni = statDownload.findViewById(R.id.tvValue);
            }

            tvDokumen = findViewById(R.id.tvDokumen);
            tvLihatSemua = findViewById(R.id.tvLihatSemua);

            recyclerDokumen = findViewById(R.id.recyclerDokumen);

            bottomNavigation = findViewById(R.id.bottomNavigation);

            Log.d("DASHBOARD", "All views initialized successfully");

        } catch (Exception e) {
            Log.e("DASHBOARD_INIT", "Error initializing views: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error initializing UI", Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectToLogin() {
        UserSession.clear(this);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void setupProfileButton() {
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    private void setupNotificationButton() {
        if (btnNotif != null) {
            btnNotif.setOnClickListener(v -> {
                Intent i = new Intent(DashboardActivity.this, NotificationActivity.class);
                startActivity(i);
            });
        }
    }

    private void setupHelpButton() {
        if (btnHelp != null) {
            btnHelp.setOnClickListener(v -> {
                Intent i = new Intent(DashboardActivity.this, HelpActivity.class);
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    private void setupHeader() {
        if (tvWelcome == null || tvSubtitle == null) {
            Log.e("DASHBOARD_HEADER", "Header views are null");
            return;
        }

        String userName = UserSession.getUserName(this);
        String userEmail = UserSession.getUserEmail(this);
        String userNIM = UserSession.getNim(this);

        Log.d("DASHBOARD_HEADER", "Data session - Nama: " + userName +
                ", Email: " + userEmail + ", NIM: " + userNIM);

        if (userName == null || userName.trim().isEmpty() || userName.equals("Pengguna")) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String intentName = extras.getString("user_name", "");
                String intentEmail = extras.getString("user_email", "");
                String intentNIM = extras.getString("user_nim", "");

                if (!intentName.isEmpty()) {
                    userName = intentName;
                    Log.d("DASHBOARD_HEADER", "Got name from Intent: " + userName);

                    int userId = UserSession.getUserId(this);
                    if (userId > 0) {
                        String username = UserSession.getUsername(this);
                        UserSession.saveUser(this, userId, userName, username,
                                !intentEmail.isEmpty() ? intentEmail : userEmail,
                                !intentNIM.isEmpty() ? intentNIM : userNIM);
                    }
                }
            }
        }
        if (userName != null && !userName.trim().isEmpty() && !userName.equals("Pengguna")) {
            tvWelcome.setText("Selamat Datang, " + userName + "!");
        } else {
            if (userEmail != null && !userEmail.isEmpty()) {
                String emailUsername = userEmail.split("@")[0];
                tvWelcome.setText("Selamat Datang, " + emailUsername + "!");
            } else {
                tvWelcome.setText("Selamat Datang!");
            }
        }

        String subtitle = "Portal Repository Akademik POLIJE";
        if (userNIM != null && !userNIM.isEmpty()) {
            subtitle += " | NIM: " + userNIM;
        }
        tvSubtitle.setText(subtitle);

        Log.d("DASHBOARD_HEADER", "Welcome text: " + tvWelcome.getText());
        Log.d("DASHBOARD_HEADER", "Subtitle: " + tvSubtitle.getText());
    }

    private void setupUploadButton() {
        if (btnUpload != null) {
            btnUpload.setOnClickListener(v -> {
                if (!UserSession.isLoggedIn(this)) {
                    Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                    return;
                }

                String userName = UserSession.getUserName(this);
                String userNIM = UserSession.getNim(this);

                if (userName == null || userName.isEmpty() || userName.equals("Pengguna") ||
                        userNIM == null || userNIM.isEmpty()) {

                    Toast.makeText(this, "Lengkapi profil terlebih dahulu", Toast.LENGTH_LONG).show();

                    Intent profileIntent = new Intent(this, ProfileActivity.class);
                    profileIntent.putExtra("require_complete", true);
                    startActivity(profileIntent);
                    return;
                }

                Intent intent = new Intent(this, UploadActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    private void setupStatistikCards() {
        if (statTotalDokumen != null) {
            TextView lblDokumen = statTotalDokumen.findViewById(R.id.tvLabel);
            TextView descDokumen = statTotalDokumen.findViewById(R.id.tvDesc);

            if (lblDokumen != null) lblDokumen.setText("Total Dokumen");
            if (descDokumen != null) descDokumen.setText("Dokumen publikasi di repository");
        }

        if (statDownload != null) {
            TextView lblDownload = statDownload.findViewById(R.id.tvLabel);
            TextView descDownload = statDownload.findViewById(R.id.tvDesc);

            if (lblDownload != null) lblDownload.setText("Total Download");
            if (descDownload != null) descDownload.setText("Jumlah download bulan ini");
        }

        setDefaultStatValues();
        setupStatCardClickListeners();
    }

    private void setDefaultStatValues() {
        if (tvTotalDokumen != null) tvTotalDokumen.setText("0");
        if (tvDownloadBulanIni != null) tvDownloadBulanIni.setText("0");
    }

    private void setupStatCardClickListeners() {
        if (statTotalDokumen != null) {
            statTotalDokumen.setOnClickListener(v -> {
                Intent intent = new Intent(this, BrowseActivity.class);
                startActivity(intent);
            });
        }

        if (statDownload != null) {
            statDownload.setOnClickListener(v -> {
                Intent intent = new Intent(this, DownloadActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupRecyclerView() {
        if (recyclerDokumen != null) {
            recyclerDokumen.setLayoutManager(new LinearLayoutManager(this));
            adapter = new DokumenAdapter(this, originalList, this);
            recyclerDokumen.setAdapter(adapter);
            Log.d("DASHBOARD", "RecyclerView setup completed");
        } else {
            Log.e("DASHBOARD", "RecyclerView is null!");
        }
    }

    @Override
    public void onDownloadClick(DokumenModel dokumen) {
        if (dokumen != null) {
            handleDownloadFromDashboard(dokumen);
        } else {
            Log.e("DASHBOARD", "onDownloadClick: DokumenModel is null");
        }
    }

    @Override
    public void onViewClick(DokumenModel dokumen) {
        if (dokumen != null) {
            handleViewDocument(dokumen);
        } else {
            Log.e("DASHBOARD", "onViewClick: DokumenModel is null");
        }
    }

    private void handleDownloadFromDashboard(DokumenModel dokumen) {
        if (dokumen == null) return;

        String fileName = dokumen.getJudul();

        Log.d("DASHBOARD", "Download dari dashboard: " + fileName);
        Log.d("DASHBOARD", "User ID: " + UserSession.getUserId(this));

        NotificationUtils.sendDownloadNotification(this, fileName);
        logDownloadToServer(dokumen.getId());
        openDocumentFile(dokumen.getFileUrl());
        Toast.makeText(this, "Mengunduh " + fileName, Toast.LENGTH_SHORT).show();
    }

    private void handleViewDocument(DokumenModel dokumen) {
        if (dokumen == null) return;

        Log.d("DASHBOARD", "View dokumen: " + dokumen.getJudul());
        openDocumentFile(dokumen.getFileUrl());
    }

    private void logDownloadToServer(int dokumenId) {
        int userId = UserSession.getUserId(this);

        if (userId <= 0) {
            Log.e("DASHBOARD_LOG", "Invalid user ID for logging download");
            return;
        }

        String url = "http://10.46.104.1/SIPORAWEB/frontend/log_download.php";

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> Log.d("DASHBOARD_LOG", "Log download berhasil: " + response),
                error -> Log.e("DASHBOARD_LOG", "Log download gagal: " + error.toString())
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("dokumen_id", String.valueOf(dokumenId));
                params.put("user_id", String.valueOf(userId));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    private void openDocumentFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            Toast.makeText(this, "File URL tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(fileUrl));
            startActivity(intent);
        } catch (Exception e) {
            Log.e("DASHBOARD", "Error opening file: " + e.getMessage());
            Toast.makeText(this, "Gagal membuka file", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSearchBar() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterDashboard(s.toString());
                }
            });
        }
    }

    private void filterDashboard(String query) {
        if (originalList.isEmpty()) return;

        query = query.toLowerCase().trim();
        ArrayList<DokumenModel> filtered = new ArrayList<>();

        for (DokumenModel d : originalList) {
            if (d == null) continue;

            String judul = d.getJudul() != null ? d.getJudul().toLowerCase() : "";
            String abstrak = d.getAbstrak() != null ? d.getAbstrak().toLowerCase() : "";
            String uploader = d.getUploaderName() != null ? d.getUploaderName().toLowerCase() : "";
            String jurusan = d.getJurusan() != null ? d.getJurusan().toLowerCase() : "";
            String tema = d.getTema() != null ? d.getTema().toLowerCase() : "";
            String tahun = d.getTahun() != null ? d.getTahun().toLowerCase() : "";

            if (judul.contains(query) ||
                    abstrak.contains(query) ||
                    uploader.contains(query) ||
                    jurusan.contains(query) ||
                    tema.contains(query) ||
                    tahun.contains(query)) {
                filtered.add(d);
            }
        }

        adapter = new DokumenAdapter(this, filtered, this);
        recyclerDokumen.setAdapter(adapter);
    }

    private void setupLihatSemuaButton() {
        if (tvLihatSemua != null) {
            tvLihatSemua.setOnClickListener(v -> {
                Intent intent = new Intent(this, BrowseActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);

            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    return true;
                }

                Intent intent = null;

                if (id == R.id.nav_upload) {
                    intent = new Intent(this, UploadActivity.class);
                } else if (id == R.id.nav_browse) {
                    intent = new Intent(this, BrowseActivity.class);
                } else if (id == R.id.nav_search) {
                    intent = new Intent(this, SearchActivity.class);
                } else if (id == R.id.nav_download) {
                    intent = new Intent(this, DownloadActivity.class);
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }

                return true;
            });
        }
    }

    private void loadDashboard() {
        int userId = UserSession.getUserId(this);

        if (userId <= 0) {
            Log.e("DASHBOARD_LOAD", "Invalid user ID: " + userId);
            Toast.makeText(this, "Sesi login tidak valid", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        String url = URL_DASHBOARD + "?user_id=" + userId;
        Log.d("DASHBOARD_LOAD", "Loading from URL: " + url);

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("DASHBOARD_RESPONSE", "Response received");

                    try {
                        JSONObject obj = new JSONObject(response);
                        String status = obj.optString("status", "");

                        if (!"success".equals(status)) {
                            String errorMsg = obj.optString("message", "Gagal memuat dashboard");
                            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (obj.has("summary")) {
                            updateStatistik(obj.getJSONObject("summary"));
                        }
                        if (obj.has("recent_documents")) {
                            updateDokumen(obj.getJSONArray("recent_documents"));
                        }
                        if (obj.has("user_data")) {
                            updateUserDataFromResponse(obj.getJSONObject("user_data"));
                        }

                    } catch (JSONException e) {
                        Log.e("DASHBOARD_JSON", "Parsing error: " + e.toString());
                        Toast.makeText(this, "Error memproses data", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("DASHBOARD_LOAD", "General error: " + e.getMessage());
                        Toast.makeText(this, "Terjadi kesalahan", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("DASHBOARD_NETWORK", "Network error: " + error.toString());
                    Toast.makeText(this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show();
                    showPlaceholderData();
                }
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void updateStatistik(JSONObject summary) {
        try {
            if (summary.has("totalDokumen") && tvTotalDokumen != null) {
                int total = summary.getJSONObject("totalDokumen").getInt("value");
                tvTotalDokumen.setText(String.valueOf(total));
            }

            if (summary.has("downloadBulanIni") && tvDownloadBulanIni != null) {
                int down = summary.getJSONObject("downloadBulanIni").getInt("value");
                tvDownloadBulanIni.setText(String.valueOf(down));
            }

        } catch (Exception e) {
            Log.e("DASHBOARD_STATS", "Error updating statistics: " + e.getMessage());
        }
    }

    private void updateDokumen(JSONArray documents) throws JSONException {
        if (documents == null) {
            Log.e("DASHBOARD_UPDATE", "Documents array is null");
            return;
        }

        originalList.clear();

        for (int i = 0; i < documents.length(); i++) {
            try {
                JSONObject doc = documents.getJSONObject(i);

                DokumenModel model = new DokumenModel(
                        doc.optInt("id", 0),
                        doc.optString("judul", ""),
                        doc.optString("deskripsi", ""),
                        doc.optString("tanggal", ""),
                        doc.optString("file_type", ""),
                        doc.optString("status", ""),
                        doc.optString("file_url", ""),
                        doc.optString("uploader_name", ""),
                        doc.optString("nama_tema", ""),
                        doc.optString("nama_jurusan", ""),
                        doc.optString("nama_prodi", ""),
                        doc.optInt("download_count", 0),
                        doc.optString("abstrak", ""),
                        doc.optString("tahun", "")
                );

                originalList.add(model);
            } catch (Exception e) {
                Log.e("DASHBOARD_UPDATE", "Error parsing document at index " + i + ": " + e.getMessage());
            }
        }
        if (adapter != null && recyclerDokumen != null) {
            adapter = new DokumenAdapter(this, originalList, this);
            recyclerDokumen.setAdapter(adapter);

            if (originalList.isEmpty()) {
                Toast.makeText(this, "Belum ada dokumen terbaru", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("DASHBOARD_UPDATE", "Adapter or RecyclerView is null");
        }
    }

    private void updateUserDataFromResponse(JSONObject userData) {
        try {
            int userId = userData.optInt("id_user", 0);
            String userName = userData.optString("nama_lengkap", "");
            String userEmail = userData.optString("email", "");
            String username = userData.optString("username", "");
            String userNIM = userData.optString("nim", "");

            if (userId > 0 && !userName.isEmpty()) {
                UserSession.saveUser(this, userId, userName, username, userEmail, userNIM);
                if (tvWelcome != null) {
                    tvWelcome.setText("Selamat Datang, " + userName + "!");
                }

                if (tvSubtitle != null) {
                    String subtitle = "Portal Repository Akademik POLIJE";
                    if (!userNIM.isEmpty()) {
                        subtitle += " | NIM: " + userNIM;
                    }
                    tvSubtitle.setText(subtitle);
                }
            }
        } catch (Exception e) {
            Log.e("DASHBOARD_USER_UPDATE", "Error: " + e.getMessage());
        }
    }

    private void showPlaceholderData() {
        if (tvTotalDokumen != null) tvTotalDokumen.setText("--");
        if (tvDownloadBulanIni != null) tvDownloadBulanIni.setText("--");
        Toast.makeText(this, "Mode offline - data mungkin tidak terbaru", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (UserSession.isLoggedIn(this)) {
            setupHeader();
            loadDashboard();
        } else {
            redirectToLogin();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

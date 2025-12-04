package com.example.sipora.rizalmhs.Register;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.widget.*;
import android.view.View;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import com.android.volley.toolbox.Volley;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.sipora.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {

    private static final int FILE_UTAMA_REQUEST_CODE = 100;
    private static final int FILE_TURNITIN_REQUEST_CODE = 101;

    private TextView tvNamaFileUtama, tvUkuranFileUtama;
    private ImageView iconFileUtama, btnBack, iconProfile;
    private Button btnPilihFileUtama, btnUploadDokumen;
    private Uri fileUriUtama;

    private TextView tvNamaFileTurnitin, tvUkuranFileTurnitin;
    private ImageView iconFileTurnitin;
    private Button btnPilihFileTurnitin;
    private Uri fileUriTurnitin;

    private EditText etPresentaseKemiripan;
    private LinearLayout layoutPresentaseKemiripan;

    private BottomNavigationView bottomNavigation;
    private Spinner spinnerJenis, spinnerJurusan, spinnerProdi, spinnerTahun, spinnerDivisi, spinnerTema;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 50);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomNavigation), (view, insets2) -> {
            view.setPadding(0, 0, 0, 0);
            return insets2;
        });

        bottomNavigation = findViewById(R.id.bottomNavigation);

        tvNamaFileUtama = findViewById(R.id.tvNamaFileUtama);
        tvUkuranFileUtama = findViewById(R.id.tvUkuranFileUtama);
        iconFileUtama = findViewById(R.id.iconFileUtama);
        btnPilihFileUtama = findViewById(R.id.btnPilihFileUtama);
        btnUploadDokumen = findViewById(R.id.btnUploadDokumen);
        btnBack = findViewById(R.id.btnBack);
        tvNamaFileTurnitin = findViewById(R.id.tvNamaFileTurnitin);
        tvUkuranFileTurnitin = findViewById(R.id.tvUkuranFileTurnitin);
        iconFileTurnitin = findViewById(R.id.iconFileTurnitin);
        btnPilihFileTurnitin = findViewById(R.id.btnPilihFileTurnitin);
        etPresentaseKemiripan = findViewById(R.id.etPresentaseKemiripan);
        etPresentaseKemiripan.setInputType(InputType.TYPE_CLASS_NUMBER);
        etPresentaseKemiripan.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(3)
        });
        spinnerJenis = findViewById(R.id.spinnerJenis);
        spinnerJurusan = findViewById(R.id.spinnerJurusan);
        spinnerProdi = findViewById(R.id.spinnerProdi);
        spinnerTahun = findViewById(R.id.spinnerTahun);
        spinnerDivisi = findViewById(R.id.spinnerDivisi);
        spinnerTema = findViewById(R.id.spinnerTema);

        setupSpinner(spinnerJenis, R.array.jenis_dokumen);
        setupSpinner(spinnerJurusan, R.array.jurusan_list);
        setupSpinner(spinnerProdi, R.array.prodi_list);
        setupSpinner(spinnerTahun, R.array.tahun_list);
        setupSpinner(spinnerDivisi, R.array.divisi_list);
        setupSpinner(spinnerTema, R.array.tema_list);
        spinnerJurusan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = spinnerJurusan.getSelectedItem().toString();
                filterProdiByJurusan(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        findViewById(R.id.btnBack).setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        btnPilihFileUtama.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "Pilih File Utama"), FILE_UTAMA_REQUEST_CODE);
        });
        btnPilihFileTurnitin.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "Pilih File Turnitin"), FILE_TURNITIN_REQUEST_CODE);
        });
        btnUploadDokumen.setOnClickListener(v -> uploadToServer());
        bottomNavigation.setSelectedItemId(R.id.nav_upload);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

            if (id == R.id.nav_home) intent = new Intent(this, DashboardActivity.class);
            else if (id == R.id.nav_browse) intent = new Intent(this, BrowseActivity.class);
            else if (id == R.id.nav_search) intent = new Intent(this, SearchActivity.class);
            else if (id == R.id.nav_download) intent = new Intent(this, DownloadActivity.class);

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
            return true;
        });
    }

    private void setupSpinner(Spinner spinner, int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayResId, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void filterProdiByJurusan(String jurusan) {
        String[] allProdi = getResources().getStringArray(R.array.prodi_list);
        java.util.List<String> filteredProdi = new java.util.ArrayList<>();
        filteredProdi.add("Pilih Program Studi");

        Log.d("FILTER_PRODI", "Filtering untuk jurusan: " + jurusan);
        for (String prodi : allProdi) {
            if (prodi.startsWith("Pilih")) continue;

            if ((jurusan.equals("Teknologi Informasi") &&
                    (prodi.equals("Teknik Informatika") || prodi.equals("Manajemen Informatika") ||
                            prodi.equals("Teknik Komputer") || prodi.equals("Teknologi Rekayasa Komputer"))) ||

                    (jurusan.equals("Produksi Pertanian") &&
                            (prodi.equals("Produksi Tanaman Hortikultura") || prodi.equals("Produksi Tanaman Perkebunan") ||
                                    prodi.equals("Teknik Produksi Benih") || prodi.equals("Teknologi Produksi Tanaman Pangan") ||
                                    prodi.equals("Budidaya Tanaman Perkebunan") || prodi.equals("Pengelolaan Perkebunan Kopi"))) ||

                    (jurusan.equals("Teknologi Pertanian") &&
                            (prodi.equals("Keteknikan Pertanian") || prodi.equals("Teknologi Industri Pangan") ||
                                    prodi.equals("Teknologi Rekayasa Pangan"))) ||

                    (jurusan.equals("Peternakan") &&
                            (prodi.equals("Produksi Ternak") || prodi.equals("Manajemen Bisnis Unggas") ||
                                    prodi.equals("Teknologi Pakan Ternak"))) ||

                    (jurusan.equals("Manajemen Agribisnis") &&
                            (prodi.equals("Manajemen Agribisnis") || prodi.equals("Manajemen Agroindustri") ||
                                    prodi.equals("Pascasarjana Agribisnis"))) ||

                    (jurusan.equals("Bahasa, Komunikasi dan Pariwisata") &&
                            (prodi.equals("Bahasa Inggris") || prodi.equals("Destinasi Pariwisata") ||
                                    prodi.equals("Produksi Media Kampus Bondowoso"))) ||

                    (jurusan.equals("Kesehatan") &&
                            (prodi.equals("Manajemen Informasi Kesehatan") || prodi.equals("Gizi Klinik") ||
                                    prodi.equals("Promosi Kesehatan"))) ||
                    (jurusan.equals("Teknik") &&
                            (prodi.equals("Teknik Mesin Otomotif") || prodi.equals("Teknik Energi Terbarukan") ||
                                    prodi.equals("Teknologi Rekayasa Mekatronika"))) ||
                    (jurusan.equals("Bisnis") &&
                            (prodi.equals("Akuntansi Sektor Publik") || prodi.equals("Manajemen Pemasaran Internasional") ||
                                    prodi.equals("Bisnis Digital (Kampus Bondowoso)"))) ||
                    (jurusan.equals("Kelas Internasional") &&
                            (prodi.equals("Manajemen Informatika") || prodi.equals("Teknik Informatika") ||
                                    prodi.equals("Manajemen Agroindustri")))) {

                filteredProdi.add(prodi);
                Log.d("FILTER_PRODI", "Prodi ditambahkan: " + prodi);
            }
        }
        if (filteredProdi.size() == 1) {
            Log.d("FILTER_PRODI", "Tidak ada prodi yang cocok untuk jurusan: " + jurusan);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, filteredProdi);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerProdi.setAdapter(adapter);

        Log.d("FILTER_PRODI", "Total prodi setelah filter: " + (filteredProdi.size() - 1));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedUri = data.getData();
            if (selectedUri != null) {
                getContentResolver().takePersistableUriPermission(selectedUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                String fileName = getFileName(selectedUri);
                long fileSize = getFileSize(selectedUri);
                String sizeString = formatFileSize(fileSize);

                if (requestCode == FILE_UTAMA_REQUEST_CODE) {
                    fileUriUtama = selectedUri;
                    tvNamaFileUtama.setText(fileName);
                    tvUkuranFileUtama.setText(sizeString);
                    findViewById(R.id.layoutFileInfoUtama).setVisibility(View.VISIBLE);
                } else if (requestCode == FILE_TURNITIN_REQUEST_CODE) {
                    fileUriTurnitin = selectedUri;
                    tvNamaFileTurnitin.setText(fileName);
                    tvUkuranFileTurnitin.setText(sizeString);
                    findViewById(R.id.layoutFileInfoTurnitin).setVisibility(View.VISIBLE);
                    if (layoutPresentaseKemiripan != null) {
                        layoutPresentaseKemiripan.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) result = cursor.getString(nameIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result != null ? result.replaceAll("[\\[\\]#*?%&/]", "_") : "unknown_file";
    }

    private long getFileSize(Uri uri) {
        long size = 0;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                    size = cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        return size;
    }

    private void uploadToServer() {
        final int idUser = UserSession.getUserId(this);
        Log.d("UploadActivity", "ID User untuk upload: " + idUser);

        if (idUser == -1) {
            Toast.makeText(this, "Sesi Anda habis, silakan login ulang", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        if (fileUriUtama == null) {
            Toast.makeText(this, "Pilih file utama terlebih dahulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        String judul = ((EditText) findViewById(R.id.etJudul)).getText().toString().trim();
        String abstrak = ((EditText) findViewById(R.id.etAbstrak)).getText().toString().trim();
        String kataKunci = ((EditText) findViewById(R.id.etKataKunci)).getText().toString().trim();

        if (judul.isEmpty() || abstrak.isEmpty() || kataKunci.isEmpty()) {
            Toast.makeText(this, "Isi semua kolom terlebih dahulu!", Toast.LENGTH_SHORT).show();
            return;
        }
        String turnitinPercentageStr = etPresentaseKemiripan.getText().toString().trim();
        int turnitinPercentage = 0;
        if (!turnitinPercentageStr.isEmpty()) {
            try {
                turnitinPercentage = Integer.parseInt(turnitinPercentageStr);
                if (turnitinPercentage < 0 || turnitinPercentage > 100) {
                    Toast.makeText(this, "Persentase Turnitin harus antara 0-100%", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Persentase Turnitin harus angka", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String jenisDokumen = spinnerJenis.getSelectedItem().toString();
        String tahun = spinnerTahun.getSelectedItem().toString();
        String jurusan = spinnerJurusan.getSelectedItem().toString();
        String prodi = spinnerProdi.getSelectedItem().toString();
        String divisi = spinnerDivisi.getSelectedItem().toString();
        String tema = spinnerTema.getSelectedItem().toString();

        if (jenisDokumen.contains("Pilih") || jurusan.contains("Pilih") ||
                prodi.contains("Pilih") || tahun.contains("Pilih") ||
                divisi.contains("Pilih") || tema.contains("Pilih")) {
            Toast.makeText(this, "Pilih semua opsi sebelum upload!", Toast.LENGTH_SHORT).show();
            return;
        }
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mengupload dokumen...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            byte[] fileUtamaData = readFileData(fileUriUtama);
            if (fileUtamaData == null) {
                progressDialog.dismiss();
                Toast.makeText(this, "Tidak dapat membaca file utama", Toast.LENGTH_SHORT).show();
                return;
            }
            String url = "http://192.168.0.180/SIPORAWEB/frontend/upload_mobile.php";

            VolleyMultipartRequest request = new VolleyMultipartRequest(
                    Request.Method.POST,
                    url,
                    response -> {
                        progressDialog.dismiss();
                        String result = new String(response.data);
                        Log.d("UploadActivity", "Response: " + result);

                        try {
                            org.json.JSONObject jsonResponse = new org.json.JSONObject(result);
                            String status = jsonResponse.getString("status");
                            String message = jsonResponse.getString("message");

                            if ("success".equals(status)) {

                                if (jsonResponse.has("turnitin_file")) {
                                    String fileTurnitinPath = jsonResponse.getString("turnitin_file");
                                    Log.d("UploadActivity", "File turnitin berhasil disimpan: " + fileTurnitinPath);
                                }
                                sendNotif(
                                        idUser,
                                        "Upload Dokumen Berhasil",
                                        "Dokumen '" + judul + "' berhasil diupload dan menunggu verifikasi."
                                );

                                Toast.makeText(this, "Upload berhasil!", Toast.LENGTH_LONG).show();
                                resetForm();
                        } else {
                                Toast.makeText(this, "Upload gagal: " + message, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Upload berhasil!", Toast.LENGTH_LONG).show();
                            resetForm();
                        }
                    },
                    error -> {
                        progressDialog.dismiss();
                        error.printStackTrace();
                        String errorMessage = "Gagal upload: ";
                        if (error.networkResponse != null) {
                            errorMessage += new String(error.networkResponse.data);
                        } else {
                            errorMessage += error.getMessage();
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("id_user", String.valueOf(idUser));
                    params.put("judul", judul);
                    params.put("abstrak", abstrak);
                    params.put("kata_kunci", kataKunci);
                    params.put("turnitin_percentage", turnitinPercentageStr);
                    int temaPosition = spinnerTema.getSelectedItemPosition();
                    int jurusanPosition = spinnerJurusan.getSelectedItemPosition();
                    int prodiPosition = spinnerProdi.getSelectedItemPosition();
                    int divisiPosition = spinnerDivisi.getSelectedItemPosition();
                    int tahunPosition = spinnerTahun.getSelectedItemPosition();
                    String selectedTema = spinnerTema.getSelectedItem().toString();
                    String selectedJurusan = spinnerJurusan.getSelectedItem().toString();
                    String selectedProdi = spinnerProdi.getSelectedItem().toString();
                    String selectedDivisi = spinnerDivisi.getSelectedItem().toString();
                    String selectedTahun = spinnerTahun.getSelectedItem().toString();

                    Log.d("UserSelection", "User memilih:");
                    Log.d("UserSelection", "Tema: " + selectedTema + " (Position: " + temaPosition + ")");
                    Log.d("UserSelection", "Jurusan: " + selectedJurusan + " (Position: " + jurusanPosition + ")");
                    Log.d("UserSelection", "Prodi: " + selectedProdi + " (Position: " + prodiPosition + ")");
                    Log.d("UserSelection", "Divisi: " + selectedDivisi + " (Position: " + divisiPosition + ")");
                    Log.d("UserSelection", "Tahun: " + selectedTahun + " (Position: " + tahunPosition + ")");
                    params.put("id_tema", String.valueOf(temaPosition));      // Kirim 0-based
                    params.put("id_jurusan", String.valueOf(jurusanPosition)); // Kirim 0-based
                    params.put("id_prodi", String.valueOf(prodiPosition));    // Kirim 0-based
                    params.put("id_divisi", String.valueOf(divisiPosition));  // Kirim 0-based
                    params.put("year_id", String.valueOf(tahunPosition));     // Kirim 0-based
                    params.put("turnitin", fileUriTurnitin != null ? "1" : "0");

                    Log.d("UploadParams", "Params to server: " + params.toString());
                    return params;
                }

                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    try {
                        String fileName = getFileName(fileUriUtama);
                        params.put("file_utama", new DataPart(fileName, fileUtamaData, getMimeType(fileName)));

                        if (fileUriTurnitin != null) {
                            byte[] fileTurnitinData = readFileData(fileUriTurnitin);
                            if (fileTurnitinData != null) {
                                String turnitinFileName = getFileName(fileUriTurnitin);
                                params.put("turnitin_file", new DataPart(turnitinFileName, fileTurnitinData, getMimeType(turnitinFileName)));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return params;
                }
            };

            request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                    60000, // 60 seconds timeout
                    com.android.volley.DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            Volley.newRequestQueue(this).add(request);

        } catch (Exception e) {
            progressDialog.dismiss();
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private byte[] readFileData(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream()) {

            if (inputStream == null) return null;

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getMimeType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "pdf": return "application/pdf";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "txt": return "text/plain";
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            default: return "application/octet-stream";
        }
    }
    private void sendNotif(int userId, String judul, String isi) {
        String url = "http://192.168.0.180/SIPORAWEB/frontend/insert_notifikasi.php";

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

    private void resetForm() {
        fileUriUtama = null;
        fileUriTurnitin = null;
        findViewById(R.id.layoutFileInfoUtama).setVisibility(View.GONE);
        findViewById(R.id.layoutFileInfoTurnitin).setVisibility(View.GONE);

        // Reset form persentase Turnitin
        if (layoutPresentaseKemiripan != null) {
            layoutPresentaseKemiripan.setVisibility(View.GONE);
        }
        if (etPresentaseKemiripan != null) {
            etPresentaseKemiripan.setText("");
        }

        ((EditText) findViewById(R.id.etJudul)).setText("");
        ((EditText) findViewById(R.id.etAbstrak)).setText("");
        ((EditText) findViewById(R.id.etKataKunci)).setText("");
        spinnerJenis.setSelection(0);
        spinnerTahun.setSelection(0);
        spinnerJurusan.setSelection(0);
        spinnerProdi.setSelection(0);
        spinnerDivisi.setSelection(0);
        spinnerTema.setSelection(0);

        Toast.makeText(this, "Form berhasil direset", Toast.LENGTH_SHORT).show();
    }
}
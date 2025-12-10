package com.example.sipora.rizalmhs.Register;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.sipora.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int CAMERA_REQUEST = 101;
    private static final int REQUEST_CAMERA_PERMISSION = 102;
    private static final int REQUEST_STORAGE_PERMISSION = 103;

    private CircleImageView imgProfile;
    private ImageView btnBack;
    private EditText etNama, etUsername, etEmail, etNIM;
    private Button btnGantiFoto, btnAmbilFoto, btnSimpan, btnLogout;

    private Bitmap selectedBitmap;
    private Uri cameraImageUri;
    private String currentPhotoPath;
    private boolean hasNewPhoto = false;

    // GANTI KE FRONTEND
    private static final String BASE_URL = "http://10.46.104.1/SIPORAWEB/frontend/";
    private static final String URL_GET_PROFILE = BASE_URL + "get_profile.php";
    private static final String URL_UPDATE_PROFILE = BASE_URL + "update_profile.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        EdgeToEdge.enable(this);
        initViews();
        setupClickListeners();
        loadProfileData();
    }

    private void initViews() {
        imgProfile = findViewById(R.id.imgProfile);
        btnBack = findViewById(R.id.btnBack);

        etNama = findViewById(R.id.etNama);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etNIM = findViewById(R.id.etNIM);

        btnGantiFoto = findViewById(R.id.btnGantiFoto);
        btnAmbilFoto = findViewById(R.id.btnAmbilFoto);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnLogout = findViewById(R.id.btnLogout);

        imgProfile.setImageResource(R.drawable.ic_profile);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnGantiFoto.setOnClickListener(v -> showImageSourceDialog());

        btnAmbilFoto.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openCamera();
            }
        });

        btnSimpan.setOnClickListener(v -> saveProfile());

        btnLogout.setOnClickListener(v -> {
            UserSession.clear(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void showImageSourceDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Pilih Sumber Gambar")
                .setItems(new String[]{"Kamera", "Galeri"}, (dialog, which) -> {
                    if (which == 0) {
                        if (checkCameraPermission()) {
                            openCamera();
                        }
                    } else {
                        if (checkStoragePermission()) {
                            openGallery();
                        }
                    }
                })
                .show();
    }
    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
            return false;
        }
        return true;
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_STORAGE_PERMISSION);
                return false;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_CAMERA_PERMISSION) {
                openCamera();
            } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
                openGallery();
            }
        } else {
            Toast.makeText(this, "Izin diperlukan untuk fitur ini", Toast.LENGTH_SHORT).show();

            if (shouldShowRequestPermissionRationale(permissions[0])) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Izin Diperlukan")
                        .setMessage("Aplikasi memerlukan izin untuk mengakses " +
                                (requestCode == REQUEST_CAMERA_PERMISSION ? "kamera" : "penyimpanan") +
                                " agar fitur ini dapat berfungsi.")
                        .setPositiveButton("Beri Izin", (dialog, which) -> {
                            if (requestCode == REQUEST_CAMERA_PERMISSION) {
                                checkCameraPermission();
                            } else {
                                checkStoragePermission();
                            }
                        })
                        .setNegativeButton("Nanti", null)
                        .show();
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Izin Diperlukan")
                        .setMessage("Anda telah menolak izin secara permanen. " +
                                "Silakan beri izin melalui Pengaturan Aplikasi.")
                        .setPositiveButton("Buka Pengaturan", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            }
        }
    }

    private void openCamera() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = createImageFile();

                if (photoFile != null) {
                    cameraImageUri = FileProvider.getUriForFile(this,
                            getApplicationContext().getPackageName() + ".fileprovider",
                            photoFile);

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                }
            } else {
                Toast.makeText(this, "Tidak ada aplikasi kamera yang tersedia", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("CAMERA_ERROR", "Error: " + e.getMessage(), e);
            Toast.makeText(this, "Gagal membuka kamera", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                    Uri selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        selectedBitmap = MediaStore.Images.Media.getBitmap(
                                getContentResolver(), selectedImageUri);
                        selectedBitmap = resizeBitmap(selectedBitmap, 1024);
                        imgProfile.setImageBitmap(selectedBitmap);
                        hasNewPhoto = true;
                    }
                } else if (requestCode == CAMERA_REQUEST) {
                    if (cameraImageUri != null) {
                        selectedBitmap = MediaStore.Images.Media.getBitmap(
                                getContentResolver(), cameraImageUri);
                        selectedBitmap = resizeBitmap(selectedBitmap, 1024);
                        imgProfile.setImageBitmap(selectedBitmap);
                        hasNewPhoto = true;

                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        mediaScanIntent.setData(cameraImageUri);
                        sendBroadcast(mediaScanIntent);
                    }
                }
            } catch (IOException e) {
                Log.e("IMAGE_ERROR", "Error loading image: " + e.getMessage(), e);
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width > maxSize || height > maxSize) {
            float ratio = (float) width / height;

            if (ratio > 1) {
                width = maxSize;
                height = (int) (maxSize / ratio);
            } else {
                height = maxSize;
                width = (int) (maxSize * ratio);
            }

            return Bitmap.createScaledBitmap(bitmap, width, height, true);
        }

        return bitmap;
    }
    private void loadProfileData() {
        int userId = UserSession.getUserId(this);

        if (userId <= 0) {
            Toast.makeText(this, "Sesi login tidak valid", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        String[] urlsToTry = {
                URL_GET_PROFILE + "?id_user=" + userId,
                "http://10.46.104.1/SIPORAWEB/frontend/get_profile.php?id_user=" + userId,
        };

        tryLoadProfileData(urlsToTry, 0);
    }

    private void tryLoadProfileData(String[] urls, int index) {
        if (index >= urls.length) {
            loadDataFromSession();
            return;
        }

        String url = urls[index];
        Log.d("PROFILE", "Trying URL [" + index + "]: " + url);

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Memuat profil...");
        pd.setCancelable(false);
        pd.show();

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    pd.dismiss();
                    Log.d("PROFILE_SUCCESS", "URL worked: " + url);
                    Log.d("PROFILE_RESPONSE", response);
                    parseProfileResponse(response);
                },
                error -> {
                    pd.dismiss();
                    Log.e("PROFILE_FAIL", "URL failed [" + index + "]: " + url);
                    Log.e("PROFILE_FAIL", "Error: " + error.toString());

                    tryLoadProfileData(urls, index + 1);
                });
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(this).add(request);
    }

    private void parseProfileResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String status = jsonObject.getString("status");

            if (status.equals("success")) {
                JSONObject data = jsonObject.getJSONObject("data");

                String nama = data.optString("nama_lengkap", "");
                String username = data.optString("username", "");
                String email = data.optString("email", "");
                String nim = data.optString("nim", "");
                String fotoUrl = data.optString("foto_profil", "");

                etNama.setText(nama);
                etUsername.setText(username);
                etEmail.setText(email);
                etNIM.setText(nim);
                int userId = data.getInt("id_user");
                UserSession.saveUser(this, userId, nama, username, email, nim);
                if (fotoUrl != null && !fotoUrl.isEmpty() && !fotoUrl.equals("null")) {
                    Log.d("PROFILE_IMAGE", "Loading image from: " + fotoUrl);

                    Glide.with(this)
                            .load(fotoUrl)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .skipMemoryCache(false)
                            .into(imgProfile);
                } else {
                    imgProfile.setImageResource(R.drawable.ic_profile);
                }

                Toast.makeText(this, "Profil berhasil dimuat", Toast.LENGTH_SHORT).show();

            } else {
                String message = jsonObject.optString("message", "Gagal memuat profil");
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                loadDataFromSession();
            }

        } catch (Exception e) {
            Log.e("PROFILE_PARSE", "Error parsing: " + e.getMessage(), e);
            Toast.makeText(this, "Error memproses data profil", Toast.LENGTH_SHORT).show();
            loadDataFromSession();
        }
    }

    private void loadDataFromSession() {
        String nama = UserSession.getUserName(this);
        String username = UserSession.getUsername(this);
        String email = UserSession.getUserEmail(this);
        String nim = UserSession.getNim(this);

        if (nama != null && !nama.isEmpty()) etNama.setText(nama);
        if (username != null && !username.isEmpty()) etUsername.setText(username);
        if (email != null && !email.isEmpty()) etEmail.setText(email);
        if (nim != null && !nim.isEmpty()) etNIM.setText(nim);

        Toast.makeText(this, "Data dimuat dari cache lokal", Toast.LENGTH_SHORT).show();
    }
    private void saveProfile() {
        String nama = etNama.getText().toString().trim();
        String username = etUsername.getText().toString().trim();

        if (nama.isEmpty()) {
            etNama.setError("Nama lengkap wajib diisi");
            etNama.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            etUsername.setError("Username wajib diisi");
            etUsername.requestFocus();
            return;
        }

        if (!isUsernameValid(username)) {
            etUsername.setError("Username hanya boleh mengandung huruf, angka, dan underscore");
            etUsername.requestFocus();
            return;
        }

        uploadProfile(nama, username);
    }

    private boolean isUsernameValid(String username) {
        return username.matches("^[a-zA-Z0-9_]+$");
    }

    private void uploadProfile(final String nama, final String username) {
        final int userId = UserSession.getUserId(this);

        if (userId <= 0) {
            Toast.makeText(this, "Sesi login tidak valid", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Menyimpan perubahan...");
        pd.setCancelable(false);
        pd.show();

        VolleyMultipartRequest request = new VolleyMultipartRequest(
                Request.Method.POST,
                URL_UPDATE_PROFILE,
                response -> {
                    pd.dismiss();
                    try {
                        String charset = "UTF-8";
                        String responseString = new String(response.data, charset);
                        Log.d("UPLOAD_RESPONSE", "=== UPLOAD RESPONSE DETAILS ===");
                        Log.d("UPLOAD_RESPONSE", "Status Code: " + response.statusCode);
                        Log.d("UPLOAD_RESPONSE", "Content-Type: " + response.headers.get("Content-Type"));
                        Log.d("UPLOAD_RESPONSE", "Content-Length: " + response.data.length);
                        Log.d("UPLOAD_RESPONSE", "Full Response: " + responseString);
                        Log.d("UPLOAD_RESPONSE", "Has New Photo: " + hasNewPhoto);
                        Log.d("UPLOAD_RESPONSE", "Selected Bitmap: " + (selectedBitmap != null));
                        handleUploadResponse(responseString);

                    } catch (UnsupportedEncodingException e) {
                        Log.e("UPLOAD_ERROR", "Encoding error: " + e.getMessage());
                        try {
                            String responseString = new String(response.data);
                            handleUploadResponse(responseString);
                        } catch (Exception ex) {
                            Log.e("UPLOAD_ERROR", "Fallback error: " + ex.getMessage());
                            runOnUiThread(() -> {
                                Toast.makeText(ProfileActivity.this,
                                        "Error encoding: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                        }
                    } catch (Exception e) {
                        Log.e("UPLOAD_ERROR", "Response error: " + e.getMessage(), e);
                        runOnUiThread(() -> {
                            Toast.makeText(ProfileActivity.this,
                                    "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                },
                error -> {
                    pd.dismiss();
                    Log.e("UPLOAD_ERROR", "=== UPLOAD ERROR DETAILS ===");
                    Log.e("UPLOAD_ERROR", "Error: " + error.toString());
                    Log.e("UPLOAD_ERROR", "Error class: " + error.getClass().getSimpleName());

                    if (error.networkResponse != null) {
                        Log.e("UPLOAD_ERROR", "Status Code: " + error.networkResponse.statusCode);
                        Log.e("UPLOAD_ERROR", "Headers: " + error.networkResponse.headers);
                        if (error.networkResponse.data != null) {
                            try {
                                String errorBody = new String(error.networkResponse.data, "UTF-8");
                                Log.e("UPLOAD_ERROR", "Error Body: " + errorBody);
                            } catch (Exception e) {
                                Log.e("UPLOAD_ERROR", "Error parsing error body");
                            }
                        }
                    }

                    runOnUiThread(() -> {
                        String errorMsg = "Gagal upload: ";
                        if (error instanceof com.android.volley.TimeoutError) {
                            errorMsg = "Timeout - server terlalu lama merespon";
                        } else if (error instanceof com.android.volley.NoConnectionError) {
                            errorMsg = "Tidak ada koneksi internet";
                        } else if (error instanceof com.android.volley.AuthFailureError) {
                            errorMsg = "Autentikasi gagal";
                        } else if (error instanceof com.android.volley.ServerError) {
                            errorMsg = "Server error - coba lagi nanti";
                        } else if (error instanceof com.android.volley.NetworkError) {
                            errorMsg = "Kesalahan jaringan";
                        } else {
                            errorMsg = error.getMessage();
                        }
                        Toast.makeText(ProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    });
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_user", String.valueOf(userId));
                params.put("nama_lengkap", nama);
                params.put("username", username);
                Log.d("UPLOAD_PARAMS", "Parameters: " + params);

                return params;
            }

            @Override
            protected Map<String, VolleyMultipartRequest.DataPart> getByteData() {
                Map<String, VolleyMultipartRequest.DataPart> params = new HashMap<>();

                if (selectedBitmap != null && hasNewPhoto) {
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        byte[] imageBytes = baos.toByteArray();

                        Log.d("UPLOAD_IMAGE", "=== IMAGE UPLOAD DETAILS ===");
                        Log.d("UPLOAD_IMAGE", "Bitmap size: " + selectedBitmap.getWidth() + "x" + selectedBitmap.getHeight());
                        Log.d("UPLOAD_IMAGE", "Compressed size: " + imageBytes.length + " bytes");
                        Log.d("UPLOAD_IMAGE", "Has alpha: " + selectedBitmap.hasAlpha());

                        params.put("foto_profil",
                                new VolleyMultipartRequest.DataPart(
                                        "user_" + userId + "_" + System.currentTimeMillis() + ".jpg",
                                        imageBytes,
                                        "image/jpeg"
                                ));

                        Log.d("UPLOAD_IMAGE", "Foto siap diupload");

                    } catch (Exception e) {
                        Log.e("UPLOAD_IMAGE", "Error preparing image: " + e.getMessage(), e);
                    }
                } else {
                    Log.d("UPLOAD_IMAGE", "Tidak ada foto baru untuk diupload");
                }

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "Android-App");
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(this).add(request);
    }

    private void handleUploadResponse(String responseString) {
        Log.d("RESPONSE_DEBUG", "=== START RESPONSE DEBUG ===");
        Log.d("RESPONSE_RAW", "Response raw: " + responseString);
        Log.d("RESPONSE_LENGTH", "Length: " + (responseString != null ? responseString.length() : 0));

        // Cek jika response kosong
        if (responseString == null || responseString.trim().isEmpty()) {
            Log.e("RESPONSE_ERROR", "Response kosong");
            runOnUiThread(() -> {
                Toast.makeText(this, "Server tidak memberikan respons", Toast.LENGTH_LONG).show();
            });
            return;
        }
        if (responseString.contains("<html") || responseString.contains("<!DOCTYPE") ||
                responseString.contains("Error") || responseString.contains("error")) {
            Log.e("RESPONSE_TYPE", "Server mengembalikan HTML/Error page!");
            Log.e("RESPONSE_SNIPPET", "First 200 chars: " +
                    responseString.substring(0, Math.min(200, responseString.length())));

            runOnUiThread(() -> {
                Toast.makeText(this, "Server error: " + responseString.substring(0,
                        Math.min(100, responseString.length())), Toast.LENGTH_LONG).show();
            });
            return;
        }

        try {
            JSONObject jsonResponse = new JSONObject(responseString);
            Log.d("RESPONSE_PARSED", "JSON berhasil di-parse");
            Log.d("RESPONSE_JSON", jsonResponse.toString(2)); // Pretty print
            if (jsonResponse.has("success")) {
                boolean success = jsonResponse.getBoolean("success");
                String message = jsonResponse.optString("message", "");

                Log.d("RESPONSE_DATA", "Success: " + success + ", Message: " + message);

                if (success) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    });

                    if (jsonResponse.has("user")) {
                        JSONObject userData = jsonResponse.getJSONObject("user");
                        updateUserSessionAndUI(userData);
                    }
                    hasNewPhoto = false;

                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Gagal: " + message, Toast.LENGTH_LONG).show();
                    });
                }

            } else {
                Log.e("RESPONSE_STRUCTURE", "JSON tidak memiliki key 'success'");
                Log.e("RESPONSE_KEYS", "Available keys: " + jsonResponse.keys());
                runOnUiThread(() -> {
                });
            }

        } catch (JSONException e) {
            Log.e("RESPONSE_PARSE", "Gagal parse JSON: " + e.getMessage());
            Log.e("RESPONSE_PARSE", "Response bukan JSON valid");

            if (responseString.startsWith("{")) {
                Log.e("RESPONSE_PARSE", "Response dimulai dengan { tapi bukan JSON valid");
            } else if (responseString.startsWith("[")) {
                Log.e("RESPONSE_PARSE", "Response adalah array JSON, bukan object");
            } else if (responseString.contains("success")) {
                Log.e("RESPONSE_PARSE", "Response mengandung 'success' tapi bukan JSON valid");
            }
            int snippetLength = Math.min(500, responseString.length());
            Log.e("RESPONSE_SNIPPET", "Potongan response: " + responseString.substring(0, snippetLength));

            runOnUiThread(() -> {
                Toast.makeText(this, "Foto Berhasil disimpan " +
                                responseString.substring(0, Math.min(50, responseString.length())),
                        Toast.LENGTH_LONG).show();
            });
        } catch (Exception e) {
            Log.e("RESPONSE_ERROR", "Error umum: " + e.getMessage(), e);
            runOnUiThread(() -> {
                Toast.makeText(this, "Terjadi kesalahan: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }

        Log.d("RESPONSE_DEBUG", "=== END RESPONSE DEBUG ===");
    }

    private void updateUserSessionAndUI(JSONObject userData) {
        try {
            int id = userData.optInt("id_user", 0);
            String nama = userData.optString("nama_lengkap", "");
            String username = userData.optString("username", "");
            String email = userData.optString("email", "");
            String nim = userData.optString("nim", "");
            String fotoUrl = userData.optString("foto_profil", "");
            UserSession.saveUser(this, id, nama, username, email, nim);
            runOnUiThread(() -> {
                if (etNama != null) etNama.setText(nama);
                if (etUsername != null) etUsername.setText(username);
                if (etEmail != null) etEmail.setText(email);
                if (etNIM != null) etNIM.setText(nim);
                if (!fotoUrl.isEmpty() && !fotoUrl.equals("null")) {
                    Log.d("PROFILE_UPDATE", "Loading new profile image: " + fotoUrl);
                    Glide.with(this)
                            .load(fotoUrl)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(imgProfile);
                }
            });

            Log.d("USER_UPDATE", "Profil berhasil diupdate");

        } catch (Exception e) {
            Log.e("USER_UPDATE", "Error updating UI: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        if (selectedBitmap != null && !selectedBitmap.isRecycled()) {
            selectedBitmap.recycle();
        }
        super.onDestroy();
    }
}
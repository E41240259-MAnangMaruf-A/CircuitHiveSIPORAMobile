package com.example.sipora.rizalmhs.Register;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sipora.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText etLogin, etPassword;
    private CheckBox cbShowPassword;
    private Button btnLogin;
    private TextView tvSignUp, tvForgotPassword;

    // ✅ Ganti sesuai IP server kamu
    private static final String URL_LOGIN = "http://192.168.1.45/SIPORAWEB/backend/sipora_api/login.php";
    private static final String URL_FORGOT_PASSWORD = "http://192.168.1.45/SIPORAWEB/backend/sipora_api/forgot_password.php";

    private Dialog forgotPasswordDialog;

    // ✅ Counter untuk menghitung jumlah percobaan login gagal
    private int failedLoginAttempts = 0;
    private static final int MAX_FAILED_ATTEMPTS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 🔹 Hubungkan ke layout
        etLogin = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbShowPassword = findViewById(R.id.cbShowPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // 🔹 Tampilkan / sembunyikan password
        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        // 🔹 Validasi email real-time seperti di RegisterActivity
        etLogin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString().trim();
                if (!email.isEmpty() && !email.endsWith("@student.polije.ac.id")) {
                    etLogin.setError("Wajib menggunakan email @student.polije.ac.id");
                } else {
                    etLogin.setError(null); // Hapus error jika email valid
                }
            }
        });

        // 🔹 Klik tombol login
        btnLogin.setOnClickListener(v -> loginUser());

        // 🔹 Klik teks "Daftar"
        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        // ✅ Klik teks "Lupa Password"
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void loginUser() {
        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan password wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Validasi email domain dengan notifikasi
        if (!login.endsWith("@student.polije.ac.id")) {
            showEmailDomainError();
            return; // Hentikan proses login
        }

        // 🔹 Kirim request ke server
        StringRequest request = new StringRequest(Request.Method.POST, URL_LOGIN,
                response -> {
                    Log.d("LOGIN_RESPONSE", response); // Debug respon server

                    try {
                        JSONObject obj = new JSONObject(response);
                        String status = obj.getString("status");
                        String message = obj.getString("message");

                        switch (status) {
                            case "success":
                                // ✅ Reset counter jika login berhasil
                                failedLoginAttempts = 0;

                                JSONObject userObj = obj.getJSONObject("user");
                                // ✅ PERBAIKAN: Gunakan "id_user" sesuai dengan PHP
                                int userId = userObj.getInt("id_user");
                                String userName = userObj.getString("nama_lengkap");

                                // ✅ Simpan session menggunakan UserSession yang sudah ada
                                UserSession.saveUser(this, userId);

                                new AlertDialog.Builder(this)
                                        .setTitle("Sukses")
                                        .setMessage("Selamat datang, " + userName)
                                        .setPositiveButton("OK", (d, w) -> {
                                            Intent intent = new Intent(this, DashboardActivity.class);
                                            startActivity(intent);
                                            finishAffinity();
                                        })
                                        .show();
                                break;

                            case "pending":
                                new AlertDialog.Builder(this)
                                        .setTitle("Menunggu Persetujuan")
                                        .setMessage("Akun Anda sedang menunggu persetujuan admin.")
                                        .setPositiveButton("OK", null)
                                        .show();
                                break;

                            case "rejected":
                                new AlertDialog.Builder(this)
                                        .setTitle("Akun Ditolak")
                                        .setMessage("Akun Anda telah ditolak oleh admin.")
                                        .setPositiveButton("OK", null)
                                        .show();
                                break;

                            case "not_found":
                            case "invalid":
                                // ✅ Tambahkan counter untuk login gagal
                                failedLoginAttempts++;

                                // ✅ Cek apakah sudah mencapai batas maksimal percobaan
                                if (failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
                                    showForgotPasswordAfterFailedAttempts();
                                } else {
                                    new AlertDialog.Builder(this)
                                            .setTitle("Gagal Login")
                                            .setMessage(message + "\n\nPercobaan gagal: " + failedLoginAttempts + "/" + MAX_FAILED_ATTEMPTS)
                                            .setPositiveButton("OK", null)
                                            .show();
                                }
                                break;

                            case "invalid_domain":
                                new AlertDialog.Builder(this)
                                        .setTitle("Email Tidak Valid")
                                        .setMessage("Hanya email @student.polije.ac.id yang diperbolehkan untuk login.")
                                        .setPositiveButton("OK", null)
                                        .show();
                                break;

                            default:
                                // ✅ Tambahkan counter untuk login gagal
                                failedLoginAttempts++;

                                if (failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
                                    showForgotPasswordAfterFailedAttempts();
                                } else {
                                    new AlertDialog.Builder(this)
                                            .setTitle("Gagal Login")
                                            .setMessage(message + "\n\nPercobaan gagal: " + failedLoginAttempts + "/" + MAX_FAILED_ATTEMPTS)
                                            .setPositiveButton("OK", null)
                                            .show();
                                }
                                break;
                        }

                    } catch (JSONException e) {
                        Toast.makeText(this, "Kesalahan parsing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("LOGIN_ERROR", e.toString());
                    }

                },
                error -> {
                    Log.e("VOLLEY_ERROR", error.toString());
                    Toast.makeText(this, "Gagal terhubung ke server. Periksa koneksi internet.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("login", login);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    // ✅ Method untuk menampilkan dialog lupa password setelah percobaan gagal
    private void showForgotPasswordAfterFailedAttempts() {
        new AlertDialog.Builder(this)
                .setTitle("Terlalu Banyak Percobaan Gagal")
                .setMessage("Anda telah " + MAX_FAILED_ATTEMPTS + " kali salah memasukkan sandi. Apakah Anda lupa sandi?")
                .setPositiveButton("Atur Ulang Sandi", (dialog, which) -> {
                    // Tampilkan dialog lupa password
                    showForgotPasswordDialog();
                    // Auto-isi email jika ada
                    autoFillEmailInForgotPassword();
                })
                .setNegativeButton("Coba Lagi", (dialog, which) -> {
                    // Reset counter dan beri kesempatan lagi
                    failedLoginAttempts = 0;
                    etPassword.requestFocus();
                    etPassword.selectAll();
                })
                .setCancelable(false)
                .show();
    }

    // ✅ Method untuk auto-isi email di dialog lupa password
    private void autoFillEmailInForgotPassword() {
        if (forgotPasswordDialog != null && forgotPasswordDialog.isShowing()) {
            String currentEmail = etLogin.getText().toString().trim();
            if (!currentEmail.isEmpty()) {
                // Jika dialog sudah ditampilkan, kita tidak bisa langsung akses EditText
                // Jadi kita simpan email untuk nanti di-set ketika dialog siap
                new Handler().postDelayed(() -> {
                    // Set email di dialog lupa password (jika ada field email)
                    TextView tvEmailInfo = forgotPasswordDialog.findViewById(R.id.tvEmailInfo);
                    if (tvEmailInfo != null) {
                        tvEmailInfo.setText("Reset sandi untuk: " + currentEmail);
                        tvEmailInfo.setVisibility(View.VISIBLE);
                    }

                    // Simpan email di tag untuk digunakan nanti
                    forgotPasswordDialog.setTitle(currentEmail);
                }, 100);
            }
        }
    }

    // 🔹 Method untuk menampilkan notifikasi error email domain
    private void showEmailDomainError() {
        new AlertDialog.Builder(this)
                .setTitle("Email Tidak Valid")
                .setMessage("Anda harus menggunakan email @student.polije.ac.id untuk login.")
                .setPositiveButton("OK", (dialog, which) -> {
                    etLogin.requestFocus();
                    etLogin.selectAll();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // ✅ Method untuk menampilkan dialog lupa password
    private void showForgotPasswordDialog() {
        // Inflate custom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null);

        // Initialize dialog
        forgotPasswordDialog = new Dialog(this);
        forgotPasswordDialog.setContentView(dialogView);
        forgotPasswordDialog.setCancelable(true);

        // Set dialog window properties
        Window window = forgotPasswordDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());

            // Set width to 90% of screen width dan height to 70% of screen height
            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            layoutParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.70);
            layoutParams.gravity = Gravity.CENTER;

            window.setAttributes(layoutParams);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Initialize views
        ImageButton btnClose = dialogView.findViewById(R.id.btnClose);
        Button btnChangePassword = dialogView.findViewById(R.id.btnChangePassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        TextView tvError = dialogView.findViewById(R.id.tvError);
        TextView tvSuccess = dialogView.findViewById(R.id.tvSuccess);
        TextView tvEmailInfo = dialogView.findViewById(R.id.tvEmailInfo);

        // ✅ Auto-isi info email jika tersedia
        String currentEmail = etLogin.getText().toString().trim();
        if (tvEmailInfo != null && !currentEmail.isEmpty()) {
            tvEmailInfo.setText("Reset sandi untuk: " + currentEmail);
            tvEmailInfo.setVisibility(View.VISIBLE);
        }

        // Close button click
        btnClose.setOnClickListener(v -> {
            forgotPasswordDialog.dismiss();
            // Reset counter ketika dialog ditutup
            failedLoginAttempts = 0;
        });

        // Change password button click - DENGAN DEBUG
        btnChangePassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // 🔹 DEBUG: Cek input password
            Log.d("FORGOT_PASSWORD", "New Password: " + newPassword);
            Log.d("FORGOT_PASSWORD", "Confirm Password: " + confirmPassword);
            Log.d("FORGOT_PASSWORD", "Email dari field: " + etLogin.getText().toString().trim());

            if (isValidPassword(newPassword, confirmPassword)) {
                // Valid password, process change password
                tvError.setVisibility(View.GONE);
                processChangePassword(newPassword);
            } else {
                // 🔹 DEBUG: Tampilkan error validasi
                Log.d("FORGOT_PASSWORD", "Validasi password gagal");
            }
        });

        // Real-time password validation
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswordsInRealTime();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswordsInRealTime();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Show dialog with animation
        forgotPasswordDialog.show();

        // Add enter animation
        dialogView.setAlpha(0f);
        dialogView.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
    }

    // ✅ Validasi password real-time
    private void validatePasswordsInRealTime() {
        if (forgotPasswordDialog == null || !forgotPasswordDialog.isShowing()) return;

        TextInputEditText etNewPassword = forgotPasswordDialog.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = forgotPasswordDialog.findViewById(R.id.etConfirmPassword);
        TextInputLayout inputLayoutNewPassword = forgotPasswordDialog.findViewById(R.id.inputLayoutNewPassword);
        TextInputLayout inputLayoutConfirmPassword = forgotPasswordDialog.findViewById(R.id.inputLayoutConfirmPassword);
        TextView tvError = forgotPasswordDialog.findViewById(R.id.tvError);

        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Reset errors
        inputLayoutNewPassword.setError(null);
        inputLayoutConfirmPassword.setError(null);
        tvError.setVisibility(View.GONE);

        // Validate new password
        if (!newPassword.isEmpty() && newPassword.length() < 8) {
            inputLayoutNewPassword.setError("Minimal 8 karakter");
        }

        // Validate password match
        if (!confirmPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
            inputLayoutConfirmPassword.setError("Sandi tidak cocok");
        }
    }

    // ✅ Validasi password sebelum submit - DENGAN DEBUG
    private boolean isValidPassword(String newPassword, String confirmPassword) {
        if (forgotPasswordDialog == null || !forgotPasswordDialog.isShowing()) return false;

        TextView tvError = forgotPasswordDialog.findViewById(R.id.tvError);
        TextInputLayout inputLayoutNewPassword = forgotPasswordDialog.findViewById(R.id.inputLayoutNewPassword);
        TextInputLayout inputLayoutConfirmPassword = forgotPasswordDialog.findViewById(R.id.inputLayoutConfirmPassword);

        // Reset errors
        inputLayoutNewPassword.setError(null);
        inputLayoutConfirmPassword.setError(null);
        tvError.setVisibility(View.GONE);

        // 🔹 DEBUG: Log validasi
        Log.d("PASSWORD_VALIDATION", "Validating - New: " + newPassword + ", Confirm: " + confirmPassword);

        if (newPassword.isEmpty()) {
            inputLayoutNewPassword.setError("Sandi baru wajib diisi");
            Log.d("PASSWORD_VALIDATION", "New password empty");
            return false;
        }

        if (newPassword.length() < 8) {
            inputLayoutNewPassword.setError("Minimal 8 karakter");
            Log.d("PASSWORD_VALIDATION", "New password too short");
            return false;
        }

        if (confirmPassword.isEmpty()) {
            inputLayoutConfirmPassword.setError("Konfirmasi sandi wajib diisi");
            Log.d("PASSWORD_VALIDATION", "Confirm password empty");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            inputLayoutConfirmPassword.setError("Sandi tidak cocok");
            tvError.setText("Sandi yang dimasukkan tidak cocok");
            tvError.setVisibility(View.VISIBLE);
            Log.d("PASSWORD_VALIDATION", "Passwords don't match");
            return false;
        }

        Log.d("PASSWORD_VALIDATION", "Password validation SUCCESS");
        return true;
    }

    // ✅ Process change password - PERBAIKAN DENGAN DEBUG
    private void processChangePassword(String newPassword) {
        // Show loading state
        Button btnChangePassword = forgotPasswordDialog.findViewById(R.id.btnChangePassword);
        TextView tvError = forgotPasswordDialog.findViewById(R.id.tvError);
        TextView tvSuccess = forgotPasswordDialog.findViewById(R.id.tvSuccess);

        btnChangePassword.setText("Memproses...");
        btnChangePassword.setEnabled(false);

        // 🔹 PERBAIKAN: Dapatkan email dari berbagai sumber
        String email = etLogin.getText().toString().trim();

        // Coba dapatkan email dari TextView info jika tersedia
        TextView tvEmailInfo = forgotPasswordDialog.findViewById(R.id.tvEmailInfo);
        if (tvEmailInfo != null && tvEmailInfo.getVisibility() == View.VISIBLE) {
            String emailInfo = tvEmailInfo.getText().toString();
            if (emailInfo.contains("Reset sandi untuk: ")) {
                // Extract email dari text info
                String extractedEmail = emailInfo.replace("Reset sandi untuk: ", "").trim();
                if (!extractedEmail.isEmpty() && extractedEmail.contains("@")) {
                    email = extractedEmail;
                }
            }
        }

        // 🔹 DEBUG: Log email yang digunakan
        Log.d("FORGOT_PASSWORD", "Email digunakan: " + email);
        Log.d("FORGOT_PASSWORD", "Password baru: " + newPassword);

        // Validasi email
        if (!isValidEmail(email)) {
            tvError.setText("Email tidak valid. Gunakan email @student.polije.ac.id");
            tvError.setVisibility(View.VISIBLE);
            btnChangePassword.setText("Perbarui Sandi");
            btnChangePassword.setEnabled(true);
            return;
        }

        // 🔹 Kirim request ke server untuk mengganti password
        String finalEmail = email;
        StringRequest request = new StringRequest(Request.Method.POST, URL_FORGOT_PASSWORD,
                response -> {
                    Log.d("CHANGE_PASSWORD_RESPONSE", response);

                    try {
                        JSONObject obj = new JSONObject(response);
                        String status = obj.getString("status");
                        String message = obj.getString("message");

                        if (status.equals("success")) {
                            // Success
                            tvSuccess.setText("Sandi berhasil diperbarui! Silakan login dengan sandi baru Anda.");
                            tvSuccess.setVisibility(View.VISIBLE);
                            tvError.setVisibility(View.GONE);

                            // ✅ Reset counter failed attempts
                            failedLoginAttempts = 0;

                            // Clear password fields
                            TextInputEditText etNewPassword = forgotPasswordDialog.findViewById(R.id.etNewPassword);
                            TextInputEditText etConfirmPassword = forgotPasswordDialog.findViewById(R.id.etConfirmPassword);
                            etNewPassword.setText("");
                            etConfirmPassword.setText("");

                            // Auto close after 3 seconds
                            new Handler().postDelayed(() -> {
                                if (forgotPasswordDialog != null && forgotPasswordDialog.isShowing()) {
                                    forgotPasswordDialog.dismiss();
                                }
                            }, 3000);

                        } else {
                            // Error from server
                            tvError.setText(message);
                            tvError.setVisibility(View.VISIBLE);
                            tvSuccess.setVisibility(View.GONE);

                            // Reset button state
                            btnChangePassword.setText("Perbarui Sandi");
                            btnChangePassword.setEnabled(true);
                        }

                    } catch (JSONException e) {
                        Log.e("CHANGE_PASSWORD_ERROR", "Parsing error: " + e.toString());
                        tvError.setText("Terjadi kesalahan sistem: " + e.getMessage());
                        tvError.setVisibility(View.VISIBLE);
                        tvSuccess.setVisibility(View.GONE);

                        btnChangePassword.setText("Perbarui Sandi");
                        btnChangePassword.setEnabled(true);
                    }
                },
                error -> {
                    Log.e("CHANGE_PASSWORD_ERROR", "Volley error: " + error.toString());
                    tvError.setText("Gagal terhubung ke server. Periksa koneksi internet Anda.");
                    tvError.setVisibility(View.VISIBLE);
                    tvSuccess.setVisibility(View.GONE);

                    btnChangePassword.setText("Perbarui Sandi");
                    btnChangePassword.setEnabled(true);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", finalEmail);
                params.put("new_password", newPassword);

                // 🔹 DEBUG: Log parameter yang dikirim
                Log.d("FORGOT_PASSWORD", "Params - Email: " + finalEmail);
                Log.d("FORGOT_PASSWORD", "Params - Password: " + newPassword);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    // ✅ Validasi email
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                email.endsWith("@student.polije.ac.id");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (forgotPasswordDialog != null && forgotPasswordDialog.isShowing()) {
            forgotPasswordDialog.dismiss();
        }
    }
}
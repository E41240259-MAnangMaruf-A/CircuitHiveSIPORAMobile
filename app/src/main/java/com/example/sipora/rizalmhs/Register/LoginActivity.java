package com.example.sipora.rizalmhs.Register;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

    private static final String URL_LOGIN = "http://192.168.0.180/SIPORAWEB/frontend/login.php";
    private static final String URL_FORGOT_PASSWORD = "http://192.168.0.180/SIPORAWEB/frontend/forgot_password.php";

    private Dialog forgotPasswordDialog;
    private int failedLoginAttempts = 0;
    private static final int MAX_FAILED_ATTEMPTS = 2;
    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_MAHASISWA = "mahasiswa";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EdgeToEdge.enable(this);
        if (UserSession.isLoggedIn(this)) {
            Log.d("LOGIN_ACTIVITY", "User already logged in, redirecting to Dashboard");
            String userRole = UserSession.getUserRole(this);
            if (userRole != null && userRole.equals(ROLE_MAHASISWA)) {
                if (UserSession.isCompleteData(this)) {
                    startActivity(new Intent(this, DashboardActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Sesi tidak valid, silakan login ulang", Toast.LENGTH_SHORT).show();
                    UserSession.clear(this);
                }
            } else if (userRole != null && userRole.equals(ROLE_ADMIN)) {
                showAdminNotAllowedDialog(true);
            } else {
                Toast.makeText(this, "Sesi tidak valid, silakan login ulang", Toast.LENGTH_SHORT).show();
                UserSession.clear(this);
            }
            return;
        }

        initViews();
        setupEventListeners();

        UserSession.debugSession(this);
    }

    private void initViews() {
        etLogin = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbShowPassword = findViewById(R.id.cbShowPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void setupEventListeners() {
        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etPassword.setSelection(etPassword.getText().length());
        });
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
                    etLogin.setError(null);
                }
            }
        });
        btnLogin.setOnClickListener(v -> loginUser());
        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void loginUser() {
        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(login)) {
            etLogin.setError("Email wajib diisi");
            etLogin.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password wajib diisi");
            etPassword.requestFocus();
            return;
        }
        if (!login.endsWith("@student.polije.ac.id")) {
            showEmailDomainError();
            return;
        }

        btnLogin.setText("Memproses...");
        btnLogin.setEnabled(false);

        Log.d("LOGIN_ATTEMPT", "Login: " + login);

        StringRequest request = new StringRequest(Request.Method.POST, URL_LOGIN,
                response -> {
                    Log.d("LOGIN_RESPONSE_RAW", response);

                    try {
                        JSONObject obj = new JSONObject(response);
                        String status = obj.getString("status");
                        String message = obj.getString("message");

                        switch (status) {
                            case "success":
                                handleLoginSuccess(obj);
                                break;

                            case "pending":
                                showAlertDialog("Menunggu Persetujuan",
                                        "Akun Anda sedang menunggu persetujuan admin.");
                                resetLoginButton();
                                break;

                            case "rejected":
                                showAlertDialog("Akun Ditolak",
                                        "Akun Anda telah ditolak oleh admin.");
                                resetLoginButton();
                                break;

                            case "admin_not_allowed": //
                                showAdminNotAllowedDialog(false);
                                break;

                            case "not_found":
                            case "invalid":
                                handleFailedLogin(message);
                                break;

                            case "invalid_domain":
                                showAlertDialog("Email Tidak Valid",
                                        "Hanya email @student.polije.ac.id yang diperbolehkan.");
                                resetLoginButton();
                                break;

                            default:
                                handleFailedLogin(message);
                                break;
                        }

                    } catch (JSONException e) {
                        Log.e("LOGIN_JSON_ERROR", "Error: " + e.toString());
                        Toast.makeText(this, "Kesalahan parsing data server", Toast.LENGTH_SHORT).show();
                        resetLoginButton();
                    }

                },
                error -> {
                    Log.e("LOGIN_VOLLEY_ERROR", "Error: " + error.toString());
                    Toast.makeText(this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show();
                    resetLoginButton();
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

    private void handleLoginSuccess(JSONObject obj) throws JSONException {
        failedLoginAttempts = 0; // Reset counter

        JSONObject userObj = obj.getJSONObject("user");

        int userId = userObj.getInt("id_user");
        String userName = userObj.optString("nama_lengkap", "");
        String userEmail = userObj.optString("email", "");
        String username = userObj.optString("username", "");
        String nim = userObj.optString("nim", "");
        String userRole = userObj.optString("role", ROLE_MAHASISWA); // Default ke mahasiswa

        Log.d("LOGIN_SUCCESS", "Data dari server:");
        Log.d("LOGIN_SUCCESS", "  - ID: " + userId);
        Log.d("LOGIN_SUCCESS", "  - Nama: " + userName);
        Log.d("LOGIN_SUCCESS", "  - Email: " + userEmail);
        Log.d("LOGIN_SUCCESS", "  - Username: " + username);
        Log.d("LOGIN_SUCCESS", "  - NIM: " + nim);
        Log.d("LOGIN_SUCCESS", "  - Role: " + userRole);
        if (userRole.equalsIgnoreCase(ROLE_ADMIN)) {
            showAdminNotAllowedDialog(false);
            resetLoginButton();
            return;
        }

        UserSession.saveUser(this, userId, userName, username, userEmail, nim, userRole);

        UserSession.debugSession(this);

        if (UserSession.isCompleteData(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("Login Berhasil")
                    .setMessage("Selamat datang, " + userName + "!\nNIM: " + nim)
                    .setPositiveButton("Masuk", (dialog, which) -> {
                        resetLoginButton();
                        Intent intent = new Intent(this, DashboardActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .setCancelable(false)
                    .show();
        } else {
            Toast.makeText(this, "Data user tidak lengkap, coba login ulang", Toast.LENGTH_SHORT).show();
            resetLoginButton();
        }
    }

    private void showAdminNotAllowedDialog(boolean isAlreadyLoggedIn) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Akses Dibatasi");
        builder.setMessage("Maaf, aplikasi mobile ini hanya untuk mahasiswa.\n\n" +
                "Admin diharapkan login melalui web dashboard di browser.\n\n" +
                "Silakan logout dan gunakan akun mahasiswa untuk login.");

        if (isAlreadyLoggedIn) {
            builder.setPositiveButton("Logout", (dialog, which) -> {
                UserSession.clear(this);
                resetLoginButton();
                etLogin.setText("");
                etPassword.setText("");
                etLogin.requestFocus();
            });
        } else {
            builder.setPositiveButton("OK", (dialog, which) -> {
                resetLoginButton();
                etPassword.setText("");
                etPassword.requestFocus();
            });
        }

        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void handleFailedLogin(String message) {
        failedLoginAttempts++;

        if (failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
            showForgotPasswordAfterFailedAttempts();
        } else {
            showAlertDialog("Login Gagal",
                    message + "\n\nPercobaan gagal: " + failedLoginAttempts + "/" + MAX_FAILED_ATTEMPTS);
            resetLoginButton();
        }
    }

    private void resetLoginButton() {
        runOnUiThread(() -> {
            if (btnLogin != null) {
                btnLogin.setText("Masuk");
                btnLogin.setEnabled(true);
            }
        });
    }

    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showEmailDomainError() {
        new AlertDialog.Builder(this)
                .setTitle("Email Tidak Valid")
                .setMessage("Aplikasi mobile ini hanya untuk mahasiswa.\n" +
                        "Gunakan email @student.polije.ac.id untuk login.\n\n" +
                        "Admin diharapkan login melalui web dashboard.")
                .setPositiveButton("OK", (dialog, which) -> {
                    etLogin.requestFocus();
                    etLogin.selectAll();
                    resetLoginButton();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showForgotPasswordAfterFailedAttempts() {
        new AlertDialog.Builder(this)
                .setTitle("Terlalu Banyak Percobaan")
                .setMessage("Anda telah " + MAX_FAILED_ATTEMPTS + " kali salah.\nApakah Anda lupa sandi?")
                .setPositiveButton("Atur Ulang Sandi", (dialog, which) -> {
                    resetLoginButton();
                    showForgotPasswordDialog();
                })
                .setNegativeButton("Coba Lagi", (dialog, which) -> {
                    failedLoginAttempts = 0;
                    etPassword.requestFocus();
                    resetLoginButton();
                })
                .setCancelable(false)
                .show();
    }
    private void showForgotPasswordDialog() {
        String currentEmail = etLogin.getText().toString().trim();
        if (TextUtils.isEmpty(currentEmail)) {
            Toast.makeText(this, "Silakan isi email terlebih dahulu di form login", Toast.LENGTH_LONG).show();
            etLogin.requestFocus();
            resetLoginButton();
            return;
        }

        if (!currentEmail.endsWith("@student.polije.ac.id")) {
            Toast.makeText(this, "Hanya email @student.polije.ac.id yang bisa reset password", Toast.LENGTH_LONG).show();
            etLogin.requestFocus();
            resetLoginButton();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Reset password untuk email:\n" + currentEmail + "\n\nLanjutkan?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    showResetPasswordDialog(currentEmail);
                })
                .setNegativeButton("Batal", (dialog, which) -> {
                    resetLoginButton();
                })
                .setOnCancelListener(dialog -> {
                    resetLoginButton();
                })
                .show();
    }

    private void showResetPasswordDialog(String email) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null);

        forgotPasswordDialog = new Dialog(this);
        forgotPasswordDialog.setContentView(dialogView);
        forgotPasswordDialog.setCancelable(true);

        Window window = forgotPasswordDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(window.getAttributes());
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
        }

        ImageButton btnClose = dialogView.findViewById(R.id.btnClose);
        Button btnChangePassword = dialogView.findViewById(R.id.btnChangePassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        TextView tvError = dialogView.findViewById(R.id.tvError);

        TextView tvEmailInfo = dialogView.findViewById(R.id.tvEmailInfo);
        if (tvEmailInfo != null) {
            tvEmailInfo.setText("Reset sandi untuk:\n" + email);
            tvEmailInfo.setVisibility(View.VISIBLE);
        }
        btnClose.setOnClickListener(v -> {
            forgotPasswordDialog.dismiss();
            failedLoginAttempts = 0;
            resetLoginButton();
        });
        btnChangePassword.setOnClickListener(v -> {
            String newPass = etNewPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            if (isValidPassword(newPass, confirmPass)) {
                processChangePassword(email, newPass, btnChangePassword);
            }
        });
        forgotPasswordDialog.setOnDismissListener(dialog -> {
            resetLoginButton();
            failedLoginAttempts = 0;
        });

        forgotPasswordDialog.show();
    }

    private boolean isValidPassword(String newPass, String confirmPass) {
        if (newPass.length() < 8) {
            Toast.makeText(this, "Password minimal 8 karakter", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Password tidak cocok", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void processChangePassword(String email, String newPassword, Button btnChangePassword) {
        if (TextUtils.isEmpty(email) || !email.endsWith("@student.polije.ac.id")) {
            Toast.makeText(this, "Email tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }
        btnChangePassword.setText("Memproses...");
        btnChangePassword.setEnabled(false);

        StringRequest request = new StringRequest(Request.Method.POST, URL_FORGOT_PASSWORD,
                response -> {
                    Log.d("FORGOT_PASSWORD_RESPONSE", response);

                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if (status.equals("success")) {
                            new AlertDialog.Builder(LoginActivity.this)
                                    .setTitle("Reset Password Berhasil")
                                    .setMessage(message)
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        forgotPasswordDialog.dismiss();
                                        etPassword.setText("");
                                        etPassword.requestFocus();
                                        resetLoginButton();
                                    })
                                    .setCancelable(false)
                                    .show();
                        } else {
                            showPasswordResetError(message);
                            btnChangePassword.setText("Perbarui Sandi");
                            btnChangePassword.setEnabled(true);
                        }

                    } catch (JSONException e) {
                        Log.e("FORGOT_PASSWORD_JSON", "Error: " + e.getMessage());
                        showPasswordResetError("Gagal memproses respons server");
                        btnChangePassword.setText("Perbarui Sandi");
                        btnChangePassword.setEnabled(true);
                    }
                },
                error -> {
                    Log.e("FORGOT_PASSWORD_VOLLEY", "Error: " + error.toString());
                    showPasswordResetError("Gagal terhubung ke server");
                    btnChangePassword.setText("Perbarui Sandi");
                    btnChangePassword.setEnabled(true);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("new_password", newPassword);
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

    private void showPasswordResetError(String message) {
        if (forgotPasswordDialog != null) {
            TextView tvError = forgotPasswordDialog.findViewById(R.id.tvError);
            if (tvError != null) {
                tvError.setText(message);
                tvError.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        resetLoginButton();

        if (forgotPasswordDialog != null && forgotPasswordDialog.isShowing()) {
            forgotPasswordDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetLoginButton();
    }
}
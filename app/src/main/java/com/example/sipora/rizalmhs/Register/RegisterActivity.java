package com.example.sipora.rizalmhs.Register;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sipora.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNama, etNim, etEmail, etUsername, etPassword, etConfirmPassword;
    private ImageView ivShowPassword, ivShowConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private CheckBox cbAgree;

    private static final String URL_REGISTER = "http://192.168.0.180/SIPORAWEB/frontend/register.php";

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        EdgeToEdge.enable(this);
        etNama = findViewById(R.id.etNama);
        etNim = findViewById(R.id.etNomorInduk);
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        ivShowPassword = findViewById(R.id.ivShowPassword);
        ivShowConfirmPassword = findViewById(R.id.ivShowConfirmPassword);
        cbAgree = findViewById(R.id.cbAgree);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));

        ivShowPassword.setOnClickListener(v -> togglePasswordVisibility(etPassword, ivShowPassword, true));
        ivShowConfirmPassword.setOnClickListener(v -> togglePasswordVisibility(etConfirmPassword, ivShowConfirmPassword, false));

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().endsWith("@student.polije.ac.id") && !s.toString().isEmpty()) {
                    etEmail.setError("Gunakan email @student.polije.ac.id");
                }
            }
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageView icon, boolean isMainPassword) {
        boolean visible = isMainPassword ? isPasswordVisible : isConfirmPasswordVisible;
        if (visible) {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            icon.setImageResource(R.drawable.ic_eye_off);
        } else {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            icon.setImageResource(R.drawable.ic_eye);
        }
        editText.setSelection(editText.getText().length());
        if (isMainPassword)
            isPasswordVisible = !isPasswordVisible;
        else
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
    }

    private void registerUser() {
        String nama = etNama.getText().toString().trim();
        String nim = etNim.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (nama.isEmpty() || nim.isEmpty() || email.isEmpty() ||
                username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showDialog("Semua field wajib diisi!");
            return;
        }

        if (!email.endsWith("@student.polije.ac.id")) {
            showDialog("Gunakan email domain @student.polije.ac.id");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showDialog("Password tidak cocok!");
            return;
        }

        if (!cbAgree.isChecked()) {
            showDialog("Centang persetujuan terlebih dahulu!");
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, URL_REGISTER,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        String status = obj.getString("status");
                        String message = obj.getString("message");

                        if (status.equals("success")) {
                            new AlertDialog.Builder(this)
                                    .setTitle("Sukses")
                                    .setMessage(message)
                                    .setPositiveButton("OK", (d, w) -> {
                                        startActivity(new Intent(this, LoginActivity.class));
                                        finish();
                                    })
                                    .show();
                        } else {
                            showDialog(message);
                        }
                    } catch (JSONException e) {
                        showDialog("Response error: " + e.getMessage());
                    }
                },
                error -> showDialog("Gagal koneksi server: " + error.getMessage())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("nama", nama);
                params.put("nim", nim);
                params.put("email", email);
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void showDialog(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", (d, w) -> d.dismiss())
                .show();
    }
}
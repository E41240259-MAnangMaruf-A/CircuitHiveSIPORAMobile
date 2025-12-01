package com.example.sipora.rizalmhs.Register;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sipora.R;

import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvNama, tvEmail, tvJurusan, tvProdi;
    private Button btnEdit, btnLogout;
    private ImageView btnBack;

    private static final String URL_GET_PROFILE =
            "http://192.168.1.45/SIPORAWEB/backend/sipora_api/get_profile.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvNama = findViewById(R.id.tvNama);
        tvEmail = findViewById(R.id.tvEmail);
        tvJurusan = findViewById(R.id.tvJurusan);
        tvProdi = findViewById(R.id.tvProdi);

        btnEdit = findViewById(R.id.btnEdit);
        btnLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        loadProfile();

        btnEdit.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class)));

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadProfile() {
        int userId = UserSession.getUserId(this);

        String url = URL_GET_PROFILE + "?user_id=" + userId;

        StringRequest req = new StringRequest(Request.Method.GET, url,
                res -> {
                    try {
                        JSONObject obj = new JSONObject(res);

                        if (!obj.getString("status").equals("success")) return;

                        JSONObject u = obj.getJSONObject("data");

                        tvNama.setText(u.getString("nama_lengkap"));
                        tvEmail.setText(u.getString("email"));
                        tvJurusan.setText(u.optString("jurusan", "-"));
                        tvProdi.setText(u.optString("prodi", "-"));

                    } catch (Exception ignored) {}
                },
                err -> {}
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Keluar Akun?")
                .setMessage("Apakah yakin ingin keluar?")
                .setPositiveButton("Ya", (d, w) -> {
                    UserSession.clear(this);
                    startActivity(new Intent(this, LoginActivity.class));
                    finishAffinity();
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}

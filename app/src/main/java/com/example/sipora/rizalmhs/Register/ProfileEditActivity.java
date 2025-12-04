package com.example.sipora.rizalmhs.Register;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sipora.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileEditActivity extends AppCompatActivity {

    private EditText etNama, etUsername, etEmail, etNIM;
    private Button btnSave, btnCancel;

    private static final String URL_EDIT =
            "http://192.168.0.180/SIPORAWEB/frontend/update_profile.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        etNama = findViewById(R.id.etNama);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etNIM = findViewById(R.id.etNIM);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        loadFromSession();

        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadFromSession() {
        etNama.setText(UserSession.getUserName(this));
    }

    private void saveProfile() {
        int id = UserSession.getUserId(this);

        if (id <= 0) {
            Toast.makeText(this, "User tidak valid, silakan login ulang.", Toast.LENGTH_LONG).show();
            return;
        }

        StringRequest req = new StringRequest(Request.Method.POST, URL_EDIT,
                res -> handleResponse(res),
                err -> Toast.makeText(this, "Gagal menghubungi server", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("id_user", String.valueOf(id));
                p.put("nama_lengkap", etNama.getText().toString());
                p.put("username", etUsername.getText().toString());
                p.put("email", etEmail.getText().toString());
                p.put("nim", etNIM.getText().toString());
                return p;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    private void handleResponse(String res) {
        try {
            JSONObject obj = new JSONObject(res);

            if (obj.getString("status").equals("success")) {

                int userId = UserSession.getUserId(this);
                String nama = etNama.getText().toString();
                String username = UserSession.getUsername(this);
                String email = UserSession.getUserEmail(this);
                String nim = UserSession.getNim(this);

                UserSession.saveUser(this, userId, nama, username, email, nim);

                Toast.makeText(this, "Profil diperbarui", Toast.LENGTH_SHORT).show();
                finish();

            } else {
                Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception ignored) {}
    }
}

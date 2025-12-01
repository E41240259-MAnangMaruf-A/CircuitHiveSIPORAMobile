package com.example.sipora.rizalmhs.Register;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sipora.R;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileEditActivity extends AppCompatActivity {

    EditText etNama, etEmail, etJurusan, etProdi;
    ImageView imgProfile, btnBack;
    Button btnSimpan;

    Bitmap bitmap;
    Uri filePath;

    private static final String URL_UPDATE_PROFILE =
            "http://192.168.1.45/SIPORAWEB/backend/sipora_api/update_profile.php";

    private static final String URL_UPLOAD_PHOTO =
            "http://192.168.1.45/SIPORAWEB/backend/sipora_api/upload_photo.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        etNama = findViewById(R.id.etNama);
        etEmail = findViewById(R.id.etEmail);
        etJurusan = findViewById(R.id.etJurusan);
        etProdi = findViewById(R.id.etProdi);
        imgProfile = findViewById(R.id.imgProfile);
        btnBack = findViewById(R.id.btnBack);
        btnSimpan = findViewById(R.id.btnSimpan);

        etNama.setText(UserSession.getUserName(this));

        btnBack.setOnClickListener(v -> finish());

        imgProfile.setOnClickListener(v -> pilihFoto());

        btnSimpan.setOnClickListener(v -> {
            if (bitmap != null) {
                uploadFoto(); // upload foto dulu
            }
            updateProfile(); // baru update data
        });
    }

    private void pilihFoto() {
        Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pick, 101);
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);

        if (req == 101 && res == RESULT_OK && data != null) {
            filePath = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imgProfile.setImageBitmap(bitmap);
            } catch (Exception ignored) {}
        }
    }

    private void uploadFoto() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Mengupload foto...");
        pd.show();

        StringRequest req = new StringRequest(Request.Method.POST, URL_UPLOAD_PHOTO,
                res -> {
                    pd.dismiss();
                    Toast.makeText(this, "Foto berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                },
                err -> {
                    pd.dismiss();
                    Toast.makeText(this, "Gagal upload foto!", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            public byte[] getBody() {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos);
                return bos.toByteArray();
            }

            @Override
            public String getBodyContentType() {
                return "image/jpeg";
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(UserSession.getUserId(ProfileEditActivity.this)));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    private void updateProfile() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Menyimpan perubahan...");
        pd.show();

        StringRequest req = new StringRequest(Request.Method.POST, URL_UPDATE_PROFILE,
                res -> {
                    pd.dismiss();
                    Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    finish();
                },
                err -> {
                    pd.dismiss();
                    Toast.makeText(this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("user_id", String.valueOf(UserSession.getUserId(ProfileEditActivity.this)));
                p.put("nama_lengkap", etNama.getText().toString());
                p.put("email", etEmail.getText().toString());
                p.put("jurusan", etJurusan.getText().toString());
                p.put("prodi", etProdi.getText().toString());
                return p;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }
}

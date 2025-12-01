package com.rizalmhs.registro.Register;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.rizalmhs.registro.R;
import com.rizalmhs.registro.Model.Mahasiswa;

public class LoginActivity extends AppCompatActivity {

    private EditText atrEmail, atrPassword;
    private CheckBox atrShowPassword;
    private Button atrBtnSignIn;
    private TextView atrForgotPassword, atrGoogleBtn, atrFacebookBtn, atrSignUpTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        atrEmail = findViewById(R.id.atr_email);
        atrPassword = findViewById(R.id.atr_password);
        atrShowPassword = findViewById(R.id.atr_show_password);
        atrBtnSignIn = findViewById(R.id.atr_btn_signin);
        atrForgotPassword = findViewById(R.id.atr_forgot_password);
        atrGoogleBtn = findViewById(R.id.atr_google_btn);
        atrFacebookBtn = findViewById(R.id.atr_facebook_btn);
        atrSignUpTab = findViewById(R.id.atr_sign_up_tab);

        atrShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) atrPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            else atrPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            atrPassword.setSelection(atrPassword.getText().length());
        });

        atrForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Fitur Forgot Password dipilih", Toast.LENGTH_SHORT).show()
        );

        atrGoogleBtn.setOnClickListener(v ->
                Toast.makeText(this, "Login dengan Google", Toast.LENGTH_SHORT).show()
        );
        atrFacebookBtn.setOnClickListener(v ->
                Toast.makeText(this, "Login dengan Facebook", Toast.LENGTH_SHORT).show()
        );

        atrSignUpTab.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        atrBtnSignIn.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = atrEmail.getText().toString().trim();
        String password = atrPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan Password wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String registeredEmail = prefs.getString("email", "");
        String registeredPassword = prefs.getString("password", "");
        String nama = prefs.getString("nama", "");
        String nim = prefs.getString("nim", "");
        String noHp = prefs.getString("noHp", "");
        String alamat = prefs.getString("alamat", "");
        String gender = prefs.getString("gender", "");
        String agama = prefs.getString("agama", "");

        if (email.equals(registeredEmail) && password.equals(registeredPassword)) {
            Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show();

            Mahasiswa mahasiswa = new Mahasiswa();
            mahasiswa.setNama(nama);
            mahasiswa.setEmail(registeredEmail);
            mahasiswa.setNim(nim);
            mahasiswa.setPassword(registeredPassword);
            mahasiswa.setNoHp(noHp);
            mahasiswa.setAlamat(alamat);
            mahasiswa.setGender(gender);
            mahasiswa.setAgama(agama);

        } else {
            Toast.makeText(this, "Email atau Password salah!", Toast.LENGTH_SHORT).show();
        }
    }
}

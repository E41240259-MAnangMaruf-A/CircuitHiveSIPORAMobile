package com.rizalmhs.registro.Register;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.rizalmhs.registro.R;
import com.rizalmhs.registro.Model.Mahasiswa;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtNama, edtEmail, edtNim, edtPassword, edtNoHp, edtAlamat;
    private CheckBox cbShowPassword;
    private Spinner spGender, spAgama;
    private Button btnRegister;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtNama = findViewById(R.id.atr_nama);
        edtEmail = findViewById(R.id.atr_email);
        edtNim = findViewById(R.id.atr_nim);
        edtPassword = findViewById(R.id.atr_password);
        edtNoHp = findViewById(R.id.atr_no_hp);
        edtAlamat = findViewById(R.id.atr_alamat);
        cbShowPassword = findViewById(R.id.cb_show_password);
        spGender = findViewById(R.id.atr_gender);
        spAgama = findViewById(R.id.atr_agama);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);

          spGender.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Pilih Gender", "Laki-laki", "Perempuan"}));
        spAgama.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Pilih Agama", "Islam", "Kristen", "Katolik", "Hindu", "Buddha", "Konghucu"}));
        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            else edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            edtPassword.setSelection(edtPassword.getText().length());
        });

         btnRegister.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> goToLogin());
    }

    private void registerUser() {
        Mahasiswa mhs = new Mahasiswa();
        mhs.setNama(edtNama.getText().toString().trim());
        mhs.setEmail(edtEmail.getText().toString().trim());
        mhs.setNim(edtNim.getText().toString().trim());
        mhs.setPassword(edtPassword.getText().toString().trim());
        mhs.setNoHp(edtNoHp.getText().toString().trim());
        mhs.setAlamat(edtAlamat.getText().toString().trim());
        mhs.setGender(spGender.getSelectedItem().toString());
        mhs.setAgama(spAgama.getSelectedItem().toString());

        if (mhs.getNama().isEmpty() || mhs.getEmail().isEmpty() || mhs.getNim().isEmpty() || mhs.getPassword().isEmpty()) {
            new AlertDialog.Builder(this)
                    .setMessage("Harap isi semua field wajib!")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }

        if (mhs.getGender().equals("Pilih Gender")) {
            new AlertDialog.Builder(this)
                    .setMessage("Harap pilih Gender!")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }

        if (mhs.getAgama().equals("Pilih Agama")) {
            new AlertDialog.Builder(this)
                    .setMessage("Harap pilih Agama!")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("nama", mhs.getNama());
        editor.putString("email", mhs.getEmail());
        editor.putString("nim", mhs.getNim());
        editor.putString("password", mhs.getPassword());
        editor.putString("noHp", mhs.getNoHp());
        editor.putString("alamat", mhs.getAlamat());
        editor.putString("gender", mhs.getGender());
        editor.putString("agama", mhs.getAgama());
        editor.apply();

        String data = "Nama: " + mhs.getNama() +
                "\nEmail: " + mhs.getEmail() +
                "\nNIM: " + mhs.getNim() +
                "\nPassword: " + mhs.getPassword() +
                "\nNo HP: " + mhs.getNoHp() +
                "\nAlamat: " + mhs.getAlamat() +
                "\nGender: " + mhs.getGender() +
                "\nAgama: " + mhs.getAgama();

        new AlertDialog.Builder(this)
                .setTitle("Data Registrasi")
                .setMessage(data)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    goToLogin();
                })
                .show();
    }
    private void goToLogin() {
        startActivity(new Intent(this, com.rizalmhs.registro.Register.LoginActivity.class));
        finish();
    }
}

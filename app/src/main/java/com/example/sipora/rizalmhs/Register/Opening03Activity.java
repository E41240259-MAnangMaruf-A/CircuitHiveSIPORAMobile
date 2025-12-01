package com.example.sipora.rizalmhs.Register;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sipora.R;

public class Opening03Activity extends AppCompatActivity {

    Button btnCreateAccount;
    LinearLayout btnGoogle;
    TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_opening03);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);return insets;
        });
        btnCreateAccount = findViewById(R.id.btn_create_account);
        tvLogin = findViewById(R.id.tv_login);

        btnCreateAccount.setOnClickListener(v ->
                Toast.makeText(this, "Create Account clicked", Toast.LENGTH_SHORT).show());

        tvLogin.setOnClickListener(v ->
                Toast.makeText(this, "Log in clicked", Toast.LENGTH_SHORT).show());
    }
}

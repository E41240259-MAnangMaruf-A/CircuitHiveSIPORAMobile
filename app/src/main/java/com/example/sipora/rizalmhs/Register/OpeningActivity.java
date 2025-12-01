package com.example.sipora.rizalmhs.Register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.RequestQueue;
import com.example.sipora.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OpeningActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private OpeningAdapter adapter;

    private static final String BASE_URL = "http://192.168.1.191/sipora_api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_opening_main);

        // ===== ViewPager intro =====
        viewPager = findViewById(R.id.viewPager);
        adapter = new OpeningAdapter(this);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);
            }
        });
    }

    // ===== Update indikator (titik slider) =====
    private void updateIndicators(int position) {
        for (int i = 0; i < adapter.getItemCount(); i++) {
            View page = viewPager.findViewWithTag("page_" + i);
            if (page != null) {
                LinearLayout indicatorLayout = page.findViewById(R.id.page_indicator);
                if (indicatorLayout != null) {
                    for (int j = 0; j < indicatorLayout.getChildCount(); j++) {
                        ImageView dot = (ImageView) indicatorLayout.getChildAt(j);
                        if (j == position) {
                            dot.setBackgroundResource(R.drawable.dot_active);
                        } else {
                            dot.setBackgroundResource(R.drawable.dot_inactive);
                        }
                    }
                }
            }
        }
    }

    // ===== Tombol daftar manual =====
    public void goToRegister(View view) {
        startActivity(new Intent(OpeningActivity.this, RegisterActivity.class));
    }

    // ===== Tombol login manual ke server Laragon =====
    public void loginToServer(View view) {
        String email = "example@polije.ac.id"; // contoh data (nanti bisa ambil dari EditText)
        String password = "123456";            // contoh data (nanti ganti input user)

        String url = BASE_URL + "login.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("status").equals("success")) {
                            Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(OpeningActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Login gagal: " + json.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Koneksi gagal: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}

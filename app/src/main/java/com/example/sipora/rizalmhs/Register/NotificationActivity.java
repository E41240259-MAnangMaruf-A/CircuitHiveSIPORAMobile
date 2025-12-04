package com.example.sipora.rizalmhs.Register;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sipora.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView recycler;
    TextView txtEmpty;
    ArrayList<NotifModel> list = new ArrayList<>();
    NotifAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        EdgeToEdge.enable(this);
        recycler = findViewById(R.id.recyclerNotif);
        txtEmpty = findViewById(R.id.txtEmpty);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotifAdapter(this, list);
        recycler.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadNotifikasi();
    }

    private void loadNotifikasi() {
        int userId = UserSession.getUserId(this);
        String url = "http://192.168.0.180/SIPORAWEB/frontend/get_notifikasi.php?user_id=" + userId;

        StringRequest req = new StringRequest(url,
                res -> {
                    try {
                        JSONObject obj = new JSONObject(res);
                        JSONArray arr = obj.getJSONArray("notifikasi");

                        list.clear();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);

                            list.add(new NotifModel(
                                    o.getInt("id_notif"),
                                    o.getString("judul"),
                                    o.getString("isi"),
                                    o.getString("status"),
                                    o.getString("waktu")
                            ));
                        }

                        txtEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Log.e("NOTIF_ERR", e.toString());
                        Toast.makeText(this, "Gagal parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                err -> Toast.makeText(this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(req);
    }
}

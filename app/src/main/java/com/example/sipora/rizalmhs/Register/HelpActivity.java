package com.example.sipora.rizalmhs.Register;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sipora.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HelpActivity extends AppCompatActivity {

    ExpandableListView expandableListView;
    TextView tvMudafiq, tvAlyvia;
    HelpAdapter helpAdapter;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        expandableListView = findViewById(R.id.expandHelp);
        tvMudafiq = findViewById(R.id.tvWhatsAppMudafiq);
        tvAlyvia = findViewById(R.id.tvWhatsAppAlyvia);

        prepareData();

        helpAdapter = new HelpAdapter(this, listDataHeader, listDataChild);
        expandableListView.setAdapter(helpAdapter);

        tvMudafiq.setOnClickListener(v -> openWhatsApp("+6285649565379"));
        tvAlyvia.setOnClickListener(v -> openWhatsApp("+6289527731917"));
    }

    private void openWhatsApp(String number) {
        String url = "https://wa.me/" + number.replace("+", "");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    private void prepareData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        listDataHeader.add("Dashboard");
        listDataHeader.add("Upload");
        listDataHeader.add("Browse");
        listDataHeader.add("Search");
        listDataHeader.add("Download");
        listDataHeader.add("Profile");

        List<String> dashboard = new ArrayList<>();
        dashboard.add("Menampilkan dokumen terbaru, statistik, dan menu utama.");

        List<String> upload = new ArrayList<>();
        upload.add("Digunakan untuk mengupload dokumen ke repository SIPORA.");

        List<String> browse = new ArrayList<>();
        browse.add("Digunakan untuk melihat semua dokumen berdasarkan kategori.");

        List<String> search = new ArrayList<>();
        search.add("Cari dokumen berdasarkan judul, penulis, tahun, tema, dan lainnya.");

        List<String> download = new ArrayList<>();
        download.add("Halaman berisi dokumen yang pernah Anda unduh.");

        List<String> profile = new ArrayList<>();
        profile.add("Menampilkan informasi pengguna dan mengedit profil.");

        listDataChild.put(listDataHeader.get(0), dashboard);
        listDataChild.put(listDataHeader.get(1), upload);
        listDataChild.put(listDataHeader.get(2), browse);
        listDataChild.put(listDataHeader.get(3), search);
        listDataChild.put(listDataHeader.get(4), download);
        listDataChild.put(listDataHeader.get(5), profile);
    }
}

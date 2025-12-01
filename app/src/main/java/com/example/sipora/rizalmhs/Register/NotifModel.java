package com.example.sipora.rizalmhs.Register;

public class NotifModel {

    int id;
    String judul, isi, status, waktu;

    public NotifModel(int id, String judul, String isi, String status, String waktu) {
        this.id = id;
        this.judul = judul;
        this.isi = isi;
        this.status = status;
        this.waktu = waktu;
    }

    public String getJudul() { return judul; }
    public String getIsi() { return isi; }
    public String getWaktu() { return waktu; }
}

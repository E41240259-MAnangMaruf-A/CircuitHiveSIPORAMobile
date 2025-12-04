package com.example.sipora.rizalmhs.Register;

public class NotifModel {

    private int id;
    private String judul;
    private String isi;
    private String status;
    private String waktu;

    public NotifModel(int id, String judul, String isi, String status, String waktu) {
        this.id = id;
        this.judul = judul;
        this.isi = isi;
        this.status = status;
        this.waktu = waktu;
    }

    public int getId() { return id; }
    public String getJudul() { return judul; }
    public String getIsi() { return isi; }
    public String getStatus() { return status; }
    public String getWaktu() { return waktu; }
}

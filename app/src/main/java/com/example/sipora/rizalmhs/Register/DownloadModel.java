package com.example.sipora.rizalmhs.Register;

public class DownloadModel {

    private int id;
    private String judul, deskripsi, author, tipe, tanggal, status, fileUrl;

    public DownloadModel(int id, String judul, String deskripsi, String author,
                         String tipe, String tanggal, String status, String fileUrl) {

        this.id = id;
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.author = author;
        this.tipe = tipe;
        this.tanggal = tanggal;
        this.status = status;
        this.fileUrl = fileUrl;
    }

    public int getId() { return id; }
    public String getJudul() { return judul; }
    public String getDeskripsi() { return deskripsi; }
    public String getAuthor() { return author; }
    public String getTipe() { return tipe; }
    public String getTanggal() { return tanggal; }
    public String getStatus() { return status; }
    public String getFileUrl() { return fileUrl; }
}

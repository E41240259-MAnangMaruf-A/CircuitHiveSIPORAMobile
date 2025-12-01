package com.example.sipora.rizalmhs.Register;

public class DokumenModel {
    private int id;
    private String judul;
    private String deskripsi;
    private String tanggal;
    private String fileType;
    private String status;
    private String fileUrl;
    private String uploaderName;
    private String tema;
    private String jurusan;
    private String prodi;
    private int downloadCount;
    private String abstrak;
    private String tahun;

    public DokumenModel(int id, String judul, String deskripsi, String tanggal,
                        String fileType, String status, String fileUrl) {
        this.id = id;
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.tanggal = tanggal;
        this.fileType = fileType;
        this.status = status;
        this.fileUrl = fileUrl;
        this.downloadCount = 0;
        this.tahun = extractYearFromDate(tanggal); // Auto extract tahun dari tanggal
    }

    public DokumenModel(int id, String judul, String deskripsi, String tanggal,
                        String fileType, String status, String fileUrl,
                        String uploaderName, String tema, String jurusan,
                        String prodi, int downloadCount, String abstrak) {
        this.id = id;
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.tanggal = tanggal;
        this.fileType = fileType;
        this.status = status;
        this.fileUrl = fileUrl;
        this.uploaderName = uploaderName;
        this.tema = tema;
        this.jurusan = jurusan;
        this.prodi = prodi;
        this.downloadCount = downloadCount;
        this.abstrak = abstrak;
        this.tahun = extractYearFromDate(tanggal); // Auto extract tahun dari tanggal
    }

    public DokumenModel(int id, String judul, String deskripsi, String tanggal,
                        String fileType, String status, String fileUrl,
                        String uploaderName, String tema, String jurusan,
                        String prodi, int downloadCount, String abstrak, String tahun) {
        this.id = id;
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.tanggal = tanggal;
        this.fileType = fileType;
        this.status = status;
        this.fileUrl = fileUrl;
        this.uploaderName = uploaderName;
        this.tema = tema;
        this.jurusan = jurusan;
        this.prodi = prodi;
        this.downloadCount = downloadCount;
        this.abstrak = abstrak;
        this.tahun = tahun != null ? tahun : extractYearFromDate(tanggal);
    }

    public int getId() { return id; }
    public String getJudul() { return judul; }
    public String getDeskripsi() { return deskripsi; }
    public String getTanggal() { return tanggal; }
    public String getFileType() { return fileType; }
    public String getStatus() { return status; }
    public String getFileUrl() { return fileUrl; }
    public String getUploaderName() { return uploaderName; }
    public String getTema() { return tema; }
    public String getJurusan() { return jurusan; }
    public String getProdi() { return prodi; }
    public int getDownloadCount() { return downloadCount; }
    public String getAbstrak() { return abstrak; }
    public String getTahun() { return tahun; }

    private String extractYearFromDate(String tanggal) {
        if (tanggal == null || tanggal.isEmpty()) {
            return "2025";
        }
        try {
            String[] parts = tanggal.split(" ")[0].split("-");
            if (parts.length >= 1) {
                return parts[0];
            }
            return "2025";
        } catch (Exception e) {
            return "2025";
        }
    }
}

package com.example.sipora.rizalmhs.Model;

public class Mahasiswa {
    private String nama;
    private String email; // ✅ TAMBAHKAN INI
    private String nim;
    private String password;
    private String golongan;
    private String prodi;
    private String jurusan;
    private String gender;

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getEmail() { return email; } // ✅ TAMBAHKAN INI
    public void setEmail(String email) { this.email = email; } // ✅ TAMBAHKAN INI

    public String getNim() { return nim; }
    public void setNim(String nim) { this.nim = nim; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getGolongan() { return golongan; }
    public void setGolongan(String golongan) { this.golongan = golongan; }

    public String getProdi() { return prodi; }
    public void setProdi(String prodi) { this.prodi = prodi; }

    public String getJurusan() { return jurusan; }
    public void setJurusan(String jurusan) { this.jurusan = jurusan; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}
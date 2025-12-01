package com.example.sipora.rizalmhs.Register;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class UserSession {

    private static final String PREF = "sipora_session";
    private static final String KEY_ID = "user_id";
    private static final String KEY_NAME = "nama_lengkap";

    public static void saveUser(Context ctx, int userId, String namaLengkap) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit()
                .putInt(KEY_ID, userId)
                .putString(KEY_NAME, namaLengkap)
                .apply();

        Log.d("USER_SESSION", "Saved -> id=" + userId + ", nama=" + namaLengkap);
    }

    public static void saveUser(Context ctx, int userId) {
        saveUser(ctx, userId, "");
    }

    public static int getUserId(Context ctx) {
        int id = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt(KEY_ID, 0);
        Log.d("USER_SESSION", "Loaded user_id=" + id);
        return id;
    }

    public static String getUserName(Context ctx) {
        String name = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_NAME, "");
        Log.d("USER_SESSION", "Loaded name=" + name);
        return name;
    }

    public static void clear(Context ctx) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply();
        Log.d("USER_SESSION", "Session cleared");
    }
}

package com.example.sipora.rizalmhs.Register;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class UserSession {
    private static final String PREF_NAME = "UserSessionPref";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NIM = "nim";
    private static final String KEY_ROLE = "role";

    public static void saveUser(Context context, int userId, String userName,
                                String username, String email, String nim, String role) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_NIM, nim);
        editor.putString(KEY_ROLE, role); // Simpan role

        editor.apply();

        Log.d("USER_SESSION", "User saved: " + userName + " | Role: " + role);
    }

    public static void saveUser(Context context, int userId, String userName,
                                String username, String email, String nim) {
        saveUser(context, userId, userName, username, email, nim, "mahasiswa");
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public static String getUserRole(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_ROLE, "mahasiswa");
    }

    public static boolean isAdmin(Context context) {
        String role = getUserRole(context);
        return role != null && role.equalsIgnoreCase("admin");
    }

    public static boolean isMahasiswa(Context context) {
        String role = getUserRole(context);
        return role != null && (role.equalsIgnoreCase("mahasiswa") || role.equalsIgnoreCase("user"));
    }

    public static boolean isCompleteData(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_USER_ID, 0) > 0 &&
                !pref.getString(KEY_USER_NAME, "").isEmpty() &&
                !pref.getString(KEY_EMAIL, "").isEmpty() &&
                !pref.getString(KEY_ROLE, "").isEmpty(); // Tambah validasi role
    }

    public static int getUserId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_USER_ID, 0);
    }

    public static String getUserName(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_USER_NAME, "");
    }

    public static String getUserEmail(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_EMAIL, "");
    }

    public static String getUsername(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_USERNAME, "");
    }

    public static String getNim(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_NIM, "");
    }

    public static void clear(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();

        Log.d("USER_SESSION", "Session cleared");
    }

    public static void debugSession(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Log.d("SESSION_DEBUG", "isLoggedIn: " + pref.getBoolean(KEY_IS_LOGGED_IN, false));
        Log.d("SESSION_DEBUG", "userId: " + pref.getInt(KEY_USER_ID, 0));
        Log.d("SESSION_DEBUG", "userName: " + pref.getString(KEY_USER_NAME, ""));
        Log.d("SESSION_DEBUG", "username: " + pref.getString(KEY_USERNAME, ""));
        Log.d("SESSION_DEBUG", "email: " + pref.getString(KEY_EMAIL, ""));
        Log.d("SESSION_DEBUG", "nim: " + pref.getString(KEY_NIM, ""));
        Log.d("SESSION_DEBUG", "role: " + pref.getString(KEY_ROLE, ""));
    }
}
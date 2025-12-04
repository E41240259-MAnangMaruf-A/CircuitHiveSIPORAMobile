package com.example.sipora.rizalmhs.Register;

import android.content.Context;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;

public class NotificationUtils {

    private static final String TAG = "NOTIFICATION_UTILS";
    private static final String BASE_URL = "http://192.168.0.180/SIPORAWEB/frontend/";
    public static void sendDownloadNotification(Context context, String fileName) {
        sendNotification(context, "Download Dokumen",
                "Anda telah mengunduh: " + fileName, "download");
    }

    public static void sendDeleteNotification(Context context, String fileName) {
        sendNotification(context, "Hapus Dokumen",
                "Anda telah menghapus: " + fileName, "delete");
    }
    public static void sendUploadNotification(Context context, String fileName) {
        sendNotification(context, "Upload Dokumen",
                "Anda telah mengupload: " + fileName, "upload");
    }
    private static void sendNotification(Context context, String title, String message, String type) {
        if (context == null) {
            Log.e(TAG, "Context is null for " + type);
            return;
        }
        int userId = UserSession.getUserId(context);
        if (userId <= 0) {
            Log.e(TAG, "Invalid user ID: " + userId);
            return;
        }

        Log.d(TAG, "=== SENDING " + type.toUpperCase() + " NOTIFICATION ===");
        Log.d(TAG, "User ID: " + userId);
        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "Message: " + message);

        String url = BASE_URL + "create_notifikasi.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, type.toUpperCase() + " Success: " + response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, type.toUpperCase() + " Error: " + error.toString());

                        // Debug lebih detail
                        if (error.networkResponse != null) {
                            NetworkResponse networkResponse = error.networkResponse;
                            Log.e(TAG, "Status Code: " + networkResponse.statusCode);
                            Log.e(TAG, "Response Data: " + new String(networkResponse.data));
                        }
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("judul", title);
                params.put("isi", message);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                15000, // 15 detik timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);

        Log.d(TAG, type.toUpperCase() + " notification request queued");
    }
}
package com.example.sipora.rizalmhs.Register;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final String boundary = "----SiporaBoundary" + System.currentTimeMillis();
    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";

    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            Map<String, String> params = getParams();
            if (params != null && params.size() > 0) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    bos.write((twoHyphens + boundary + lineEnd).getBytes());
                    bos.write(("Content-Disposition: form-data; name=\"" +
                            entry.getKey() + "\"" + lineEnd).getBytes());
                    bos.write(("Content-Type: text/plain; charset=UTF-8" + lineEnd).getBytes());
                    bos.write((lineEnd).getBytes());
                    bos.write(entry.getValue().getBytes("UTF-8"));
                    bos.write(lineEnd.getBytes());
                }
            }

            Map<String, DataPart> data = getByteData();
            if (data != null && data.size() > 0) {
                for (Map.Entry<String, DataPart> entry : data.entrySet()) {
                    DataPart dp = entry.getValue();

                    bos.write((twoHyphens + boundary + lineEnd).getBytes());
                    bos.write(("Content-Disposition: form-data; name=\"" +
                            entry.getKey() + "\"; filename=\"" +
                            dp.getFileName() + "\"" + lineEnd).getBytes());
                    bos.write(("Content-Type: " + dp.getType() + lineEnd).getBytes());
                    bos.write((lineEnd).getBytes());

                    InputStream inputStream = new ByteArrayInputStream(dp.getContent());
                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }

                    bos.write(lineEnd.getBytes());
                }
            }
            bos.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bos.toByteArray();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response,
                HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(com.android.volley.VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }
    protected Map<String, DataPart> getByteData() throws AuthFailureError {
        return new HashMap<>();
    }
    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String fileName, byte[] content, String type) {
            this.fileName = fileName;
            this.content = content;
            this.type = type;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public String getType() {
            return type;
        }
    }
}

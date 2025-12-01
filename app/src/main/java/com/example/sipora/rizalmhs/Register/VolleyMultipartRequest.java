package com.example.sipora.rizalmhs.Register;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String lineEnd = "\r\n";
    private final String twoHyphens = "--";
    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;
    private final Map<String, String> headers;


    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.headers = new HashMap<>();
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers != null ? headers : new HashMap<>();
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            // Parameter biasa
            Map<String, String> params = getParams();
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    bos.write((twoHyphens + boundary + lineEnd).getBytes());
                    bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + lineEnd).getBytes());
                    bos.write((lineEnd + entry.getValue() + lineEnd).getBytes());
                }
            }

            // File
            Map<String, DataPart> data = getByteData();
            if (data != null) {
                for (Map.Entry<String, DataPart> entry : data.entrySet()) {
                    buildDataPart(bos, entry.getValue(), entry.getKey());
                }
            }

            bos.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());
            return bos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private void buildDataPart(ByteArrayOutputStream bos, DataPart dataFile, String inputName) throws IOException {
        bos.write((twoHyphens + boundary + lineEnd).getBytes());
        bos.write(("Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\"" +
                dataFile.getFileName() + "\"" + lineEnd).getBytes());
        bos.write(("Content-Type: " + dataFile.getType() + lineEnd).getBytes());
        bos.write(lineEnd.getBytes());

        try (InputStream inputStream = new ByteArrayInputStream(dataFile.getContent())) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
        }

        bos.write(lineEnd.getBytes());
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
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
        return null;
    }

    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String name, byte[] data) {
            this(name, data, "application/octet-stream");
        }

        public DataPart(String name, byte[] data, String type) {
            this.fileName = name;
            this.content = data;
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


package com.game.sdk.talkdata;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * Created by lucky on 2018/4/18.
 */
public class HttpClient {
    HttpURLConnection _HttpURLConnection = null;
    URL url = null;
    private String DEFAULT_PROTOCOL = "http";
    private String SLASH = "/";
    private String COLON = ":";
    public String DEFAULT_NET_ERROR = "NetError";
    public String POST = "POST";

    public String doPost(byte[] Message) {
        String result = "";
        try {
            _HttpURLConnection = (HttpURLConnection) url.openConnection();
            _HttpURLConnection.setRequestMethod(POST);
            _HttpURLConnection.setDoOutput(true);
            _HttpURLConnection.setRequestProperty("Content-Type",
                    "application/msgpack");
            _HttpURLConnection.setRequestProperty("Content-Length",
                    String.valueOf(Message.length));
            DataOutputStream ds = new DataOutputStream(
                    _HttpURLConnection.getOutputStream());
            ds.write(Message);
            ds.flush();
            ds.close();
            result = _gzipStream2Str(_HttpURLConnection.getInputStream());
            _HttpURLConnection.disconnect();
        } catch (Exception e) {
            _HttpURLConnection.disconnect();
            e.printStackTrace();
        }
        return result;
    }

    private String _gzipStream2Str(InputStream inputStream) throws IOException {
        GZIPInputStream gzipinputStream = new GZIPInputStream(inputStream);
        byte[] buf = new byte[1024];
        int num = -1;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((num = gzipinputStream.read(buf, 0, buf.length)) != -1) {
            baos.write(buf, 0, num);
        }
        return new String(baos.toByteArray(), "utf-8");
    }

    public HttpClient(String ServerName, String ServerPort, String QuestPath) {
        try {
            String ServerURL = "";
            ServerURL += DEFAULT_PROTOCOL;
            ServerURL += COLON;
            ServerURL += SLASH;
            ServerURL += SLASH;
            ServerURL += ServerName;
            if ((ServerPort != null) && (ServerPort.trim().length() > 0)) {
                ServerURL += COLON;
                ServerURL += ServerPort.trim();
            }
            if (QuestPath.charAt(0) != '/') {
                ServerURL += SLASH;
            }
            ServerURL += QuestPath;
            url = new URL(ServerURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


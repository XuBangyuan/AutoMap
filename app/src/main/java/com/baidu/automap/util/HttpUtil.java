package com.baidu.automap.util;

import com.baidu.automap.entity.User;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {
    private static final String urlPrefix = "http://192.168.1.91:8080/AutoMap_war/user/";

    public static byte[] readUserParse(String urlPath, final User user) throws Exception {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        final byte[] data = new byte[1024];

        final URL url = new URL(urlPrefix + urlPath);

        int len = 0;

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        JSONObject userJSON = new JSONObject();

        if(user.getPhone() != null) {
            userJSON.put("phone",user.getPhone());
        }
        if(user.getPassword() != null) {
            userJSON.put("password",user.getPassword());
        }
        if(user.getUserId() != null) {
            userJSON.put("userId", user.getUserId() + "");
        }

        String content = String.valueOf(userJSON);

        conn.setConnectTimeout(5000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("User-Agent", "Fiddler");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Charset", "UTF-8");
        OutputStream os = conn.getOutputStream();
        os.write(content.getBytes());
        os.close();

        InputStream inStream = conn.getInputStream();

        while ((len = inStream.read(data)) != -1) {

            outStream.write(data, 0, len);

        }

        inStream.close();

        Thread.sleep(100);

        return outStream.toByteArray();

    }
}

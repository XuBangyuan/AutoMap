package com.baidu.automap.util;

import com.baidu.automap.entity.RouteNode;
import com.baidu.automap.entity.User;
import com.baidu.automap.entity.UserRoute;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {
    private static final String urlPrefix = "http://192.168.1.91:8081/auto_map_war_exploded/";

    public static byte[] readUserParse(String urlPath, final User user) throws Exception {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        final byte[] data = new byte[1024];

        final URL url = new URL(urlPrefix + "user/" + urlPath);

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

    public static byte[] readRouteParse(String urlPath, final UserRoute userRoute, final RouteNode routeNode) throws Exception {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        final byte[] data = new byte[1024];

        final URL url = new URL(urlPrefix + "route/" + urlPath);

        int len = 0;

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        JSONObject json = new JSONObject();

        if(userRoute != null) {
            if(userRoute.getRouteId() != null) {
                json.put("routeId", userRoute.getRouteId() + "");
            }
            if(userRoute.getUserId() != null) {
                json.put("userId", userRoute.getUserId() + "");
            }
        }

        if(routeNode != null) {
            if(routeNode.getDesId() != null) {
                json.put("desId", routeNode.getDesId());
            }
            if(routeNode.getDesName() != null) {
                json.put("desName", routeNode.getDesName());
            }
            if(routeNode.getLatitude() != null) {
                json.put("latitude", routeNode.getLatitude() + "");
            }
            if(routeNode.getlongitude() != null) {
                json.put("longitude", routeNode.getlongitude() + "");
            }
            if(routeNode.getNodeId() != null) {
                json.put("nodeId", routeNode.getNodeId() + "");
            }
            if(routeNode.getRouteId() != null) {
                json.put("routeId", routeNode.getRouteId() + "");
            }
        }

        String content = String.valueOf(json);

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

//        Thread.sleep(100);

        return outStream.toByteArray();

    }
}

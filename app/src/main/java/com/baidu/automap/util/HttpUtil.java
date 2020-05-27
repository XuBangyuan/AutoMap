package com.baidu.automap.util;

import android.util.Base64;
import android.util.Log;

import com.baidu.automap.entity.Comment;
import com.baidu.automap.entity.DesDetailIntroduction;
import com.baidu.automap.entity.ImgEntity;
import com.baidu.automap.entity.Journey;
import com.baidu.automap.entity.Mp3Entity;
import com.baidu.automap.entity.RouteNode;
import com.baidu.automap.entity.User;
import com.baidu.automap.entity.UserRoute;
import com.baidu.automap.entity.response.ImgResponse;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {
    private static final String urlPrefix = "http://192.168.1.91:8081/auto_map_war_exploded/";
    private static final String KEY = "httpUtil";

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

    public static byte[] readDesDetailParse(String urlPath, final DesDetailIntroduction introduction) throws Exception {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        final byte[] data = new byte[1024];

        final URL url = new URL(urlPrefix + "detail/" + urlPath);

        int len = 0;

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        JSONObject json = new JSONObject();

        if(introduction != null) {
            if(introduction.getDesId() != null) {
                json.put("desId", introduction.getDesId() + "");
            }
            if(introduction.getIntroduction() != null) {
                json.put("introduction", introduction.getIntroduction());
            }
            if(introduction.getuId() != null) {
                json.put("uId", introduction.getuId());
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

        return outStream.toByteArray();

    }

    public static byte[] readJourneyParse(String urlPath, final Journey journey) throws Exception {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        final byte[] data = new byte[1024];

        final URL url = new URL(urlPrefix + "journey/" + urlPath);

        int len = 0;

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        JSONObject json = new JSONObject();

        if(journey != null) {
            if(journey.getDesId() != null) {
                json.put("desId", journey.getDesId());
            }
            if(journey.getAgree() != null) {
                json.put("agree", journey.getAgree());
            }
            if(journey.getCreateTime() != null) {
                json.put("createTime", journey.getCreateTime().getTime());
            }
            if(journey.getDetail() != null) {
                json.put("detail", journey.getDetail());
            }
            if(journey.getId() != null) {
                json.put("id", journey.getId());
            }
            if(journey.getTitle() != null) {
                json.put("title", journey.getTitle());
            }
            if(journey.getUserId() != null) {
                json.put("userId", journey.getUserId());
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

        return outStream.toByteArray();

    }

    public static byte[] readCommentParse(String urlPath, final Comment comment) throws Exception {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        final byte[] data = new byte[1024];

        final URL url = new URL(urlPrefix + "comment/" + urlPath);

        int len = 0;

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        JSONObject json = new JSONObject();

        if(comment != null) {
            if(comment.getJourneyId() != null) {
                json.put("journeyId", comment.getJourneyId());
            }
            if(comment.getAgree() != null) {
                json.put("agree", comment.getAgree());
            }
            if(comment.getCreateTime() != null) {
                json.put("createTime", comment.getCreateTime().getTime());
            }
            if(comment.getDetail() != null) {
                json.put("detail", comment.getDetail());
            }
            if(comment.getId() != null) {
                json.put("id", comment.getId());
            }
            if(comment.getUserId() != null) {
                json.put("userId", comment.getUserId());
            }

            Log.d(KEY, json.toString());

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

        return outStream.toByteArray();

    }

    public static byte[] readMP3Parse(String urlPath, final Mp3Entity entity) throws Exception {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        final byte[] data = new byte[1024];

        final URL url = new URL(urlPrefix + "detail/" + urlPath);

        int len = 0;

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        JSONObject json = new JSONObject();

        if(entity != null) {
            if(entity.getDesId() != null) {
                json.put("desId", entity.getDesId());
            }
            if(entity.getFile() != null) {
                json.put("file", entity.getFile());
            }
            if(entity.getId() != null) {
                json.put("id", entity.getId());
            }
            if(entity.getName() != null) {
                json.put("name", entity.getName());
            }

            Log.d(KEY, json.toString());

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

        return outStream.toByteArray();

    }

    public static byte[] readImgParse(String urlPath, final ImgEntity entity) throws Exception {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        final byte[] data = new byte[1024];

        final URL url = new URL(urlPrefix + "img/" + urlPath);

        int len = 0;

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        JSONObject json = new JSONObject();

        if(entity != null) {
            if(entity.getId() != null) {
                json.put("id", entity.getId());
            }
            if(entity.getJourneyId() != null) {
                json.put("journeyId", entity.getJourneyId());
            }
            if(entity.getName() != null) {
                json.put("name", entity.getName());
            }
            if(entity.getData() != null) {
                String dataStr = Base64.encodeToString(entity.getData(), Base64.DEFAULT);
                json.put("data", dataStr);
            }

            Log.d(KEY, json.toString());

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

        return outStream.toByteArray();

    }

//    public static byte[] readImgListParse(String urlPath, final ImgResponse imgList) throws Exception {
//        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//
//        final byte[] data = new byte[1024];
//
//        final URL url = new URL(urlPrefix + "img/" + urlPath);
//
//        int len = 0;
//
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//        JSONObject json = new JSONObject();
//
//        if(entity != null) {
//            if(entity.getId() != null) {
//                json.put("id", entity.getId());
//            }
//            if(entity.getJourneyId() != null) {
//                json.put("journeyId", entity.getJourneyId());
//            }
//            if(entity.getName() != null) {
//                json.put("name", entity.getName());
//            }
//            if(entity.getData() != null) {
//                json.put("data", new String(entity.getData()));
//            }
//
//            Log.d(KEY, json.toString());
//
//        }
//
//        String content = String.valueOf(json);
//
//        conn.setConnectTimeout(5000);
//        conn.setRequestMethod("POST");
//        conn.setDoOutput(true);
//        conn.setRequestProperty("User-Agent", "Fiddler");
//        conn.setRequestProperty("Content-Type", "application/json");
//        conn.setRequestProperty("Charset", "UTF-8");
//        OutputStream os = conn.getOutputStream();
//        os.write(content.getBytes());
//        os.close();
//
//        InputStream inStream = conn.getInputStream();
//
//        while ((len = inStream.read(data)) != -1) {
//
//            outStream.write(data, 0, len);
//
//        }
//
//        inStream.close();
//
//        return outStream.toByteArray();
//
//    }
}

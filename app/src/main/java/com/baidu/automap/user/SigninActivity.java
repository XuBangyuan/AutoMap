package com.baidu.automap.user;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.automap.R;
import com.baidu.automap.entity.User;
import com.baidu.mapapi.http.HttpClient;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SigninActivity extends AppCompatActivity {
    private EditText userId;
    private EditText password;
    private static final String KEY = "signinActivity";

    private static String serverURL = "http://192.168.1.91:8080/AutoMap_war/user/saveUser";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signin_activity);



        User user  = new User();
        user.setName("xu");
        user.setPhone("13120016321");
        user.setPassword("123456");

        // Android 4.0 之后不能在主线程中请求HTTP请求
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    byte[] data = readParse(serverURL);
                    String str = new String(data);
                    //JSONObject jsonObject = new JSONObject(str);
                    Log.d(KEY, str);
                } catch (Exception e) {
                    Log.d(KEY, e.toString());
                }
            }
        }).start();



    }

    public static byte[] readParse(String urlPath) throws Exception {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        byte[] data = new byte[1024];

        int len = 0;

        URL url = new URL(urlPath);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        User user = new User();
        user.setPassword("1253124");
        user.setPhone("2341741325");
        user.setName("adfa");

        JSONObject userJSON = new JSONObject();
        userJSON.put("name",user.getName());
        userJSON.put("phone",user.getPhone());
        userJSON.put("password",user.getPassword());

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

        return outStream.toByteArray();

    }



    /**
     * 用户注册
     */
    public static boolean userRegister(final User user)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                try {
                    JSONObject userJSON = new JSONObject();
                    userJSON.put("name",user.getName());
                    userJSON.put("userPhone",user.getPhone());
                    userJSON.put("userPassword",user.getPassword());

                    String content = String.valueOf(userJSON);

                    //HttpURLConnection connection  =
                    /**
                     * 请求地址
                     */
                    String url = serverURL +"excel/account/findAll";

                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    //HttpURLConnection connection = (HttpURLConnection) nURL.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("User-Agent", "Fiddler");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Charset", "UTF-8");
                    OutputStream os = connection.getOutputStream();
                    os.write(content.getBytes());
                    os.close();
                    /**
                     * 服务器返回结果
                     * 继续干什么事情....待续
                     */
//                    String result = read(connection.getInputStream());

                    Log.i("success","成功注册");

                }catch (Exception e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }.start();
        return true;
    }
}

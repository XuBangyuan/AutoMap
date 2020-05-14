package com.baidu.automap.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.automap.MainActivity;
import com.baidu.automap.R;
import com.baidu.automap.entity.User;
import com.baidu.automap.entity.response.UserResponse;
import com.baidu.automap.util.HttpUtil;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SigninActivity extends AppCompatActivity {
    private EditText phone;
    private EditText password;
    private Button loginButton;
    private Button signinButton;
    private LinearLayout choiceLayout;
    private Button userButton;
    private Button administratorButton;

    private static final String KEY = "signinActivity";
    private static final int MAIN_ACTIVITY = 1;
    private static final int LOGIN_ACTIVITY = 2;

    private User curUser;

    private static String serverURL = "http://192.168.1.91:8080/AutoMap_war/user/queryUser";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signin_activity);

        phone = (EditText) findViewById(R.id.user_id);
        password = (EditText) findViewById(R.id.user_password);
        loginButton = (Button) findViewById(R.id.login_button);
        signinButton = (Button) findViewById(R.id.signin_button);
        choiceLayout = (LinearLayout) findViewById(R.id.choice_layout);
        userButton = (Button) findViewById(R.id.user_button);
        administratorButton = (Button) findViewById(R.id.administrator_button);

        choiceLayout.setVisibility(View.GONE);

        curUser = new User();

        //用户界面
        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMap(false);
            }
        });

        //管理员界面
        administratorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMap(true);
            }
        });

        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final User user  = new User();
                user.setPhone(phone.getText().toString());
                user.setPassword(password.getText().toString());

                if(user.getPassword() == null || user.getPhone() == null
                        || user.getPassword().length() == 0 || user.getPhone().length() == 0) {

                    Toast.makeText(SigninActivity.this ,"请输入账号及密码", Toast.LENGTH_SHORT).show();

                } else {
                    // Android 4.0 之后不能在主线程中请求HTTP请求
                    ThreadSignin thread = new ThreadSignin(user);

                    try {
                        thread.start();
                        thread.join();
//                        Thread.sleep(500);
                        Log.d(KEY, curUser.toString());
                    } catch (InterruptedException e) {
                        Log.d(KEY, e.toString());
                    }

                    if(thread.getIsSuccess()) {
                        signinButton.setVisibility(View.GONE);
                        loginButton.setVisibility(View.GONE);
                        if(curUser.getAdministrator()) {
                            choiceLayout.setVisibility(View.VISIBLE);
                        } else {
                            startMap(false);
                        }
                    } else {
                        Toast.makeText(SigninActivity.this, thread.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });


        //注册按钮点击
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SigninActivity.this, LoginActivity.class);
                startActivityForResult(intent, LOGIN_ACTIVITY);
            }
        });

    }

    private class ThreadSignin extends Thread {

        private boolean isSuccess = false;

        private String message;

        private User user;

        public ThreadSignin(User user) {
            this.user = user;
        }

        public boolean getIsSuccess() {
            return isSuccess;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public void run() {
            try {

                byte[] data = HttpUtil.readUserParse("queryUser", user);
                String str = new String(data);
                JSONObject jsonObject = new JSONObject(str);
                Log.d(KEY, str);

                UserResponse userResponse = new UserResponse();
                userResponse.setMessage(jsonObject.getString("message"));
                if(userResponse != null && userResponse.getMessage().compareTo("success!") == 0) {
                    Log.d(KEY, "get data from server success!");
                    isSuccess = true;
                    JSONObject jsonUser = new JSONObject(jsonObject.getString("user"));

                    curUser.setPhone(jsonUser.getString("phone"));
                    curUser.setPassword(jsonUser.getString("password"));
                    curUser.setUserId(jsonUser.getInt("userId"));
                    curUser.setAdministrator(jsonUser.getBoolean("administrator"));

                    Log.d(KEY, "after get from server curUser : " + curUser.toString());
                } else {
                    isSuccess = false;
                    message = userResponse.getMessage();
                    Log.d(KEY, userResponse.getMessage());
                }
                Log.d(KEY, str);
            } catch (Exception e) {
                Log.e(KEY, e.toString());
            }
        }

    }

    //调用activity返回
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(KEY, "request : " + requestCode + " result : " + resultCode);
        if(RESULT_OK == resultCode) {
            if(MAIN_ACTIVITY == requestCode) {
                //Bundle bundle = data.getExtras();
                Log.d(KEY, "back from mainActivity");
                finish();
            } else if(LOGIN_ACTIVITY == requestCode) {
                Log.d(KEY, "finish login");
//                Bundle bundle = data.getExtras();
                Log.e(KEY, "login finish, phone : " + data.getStringExtra("phone"));
                phone.setText(data.getStringExtra("phone"));
            }
        } else {
            if(MAIN_ACTIVITY == requestCode) {
                finish();
            }
        }
    }

    //开始进入地图
    private void startMap(boolean isAdministrator) {
        Intent intent = new Intent(SigninActivity.this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("userId", curUser.getUserId());
        bundle.putBoolean("isAdministrator", isAdministrator);
        intent.putExtras(bundle);
        startActivityForResult(intent, MAIN_ACTIVITY);
    }

//    public static byte[] readParse(String urlPath, final User user) throws Exception {
//        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//
//        final byte[] data = new byte[1024];
//
//
//
//        final URL url = new URL(urlPath);
//
//        // Android 4.0 之后不能在主线程中请求HTTP请求
//        new Thread(new Runnable(){
//            @Override
//            public void run() {
//                try {
//
//                    int len = 0;
//
//                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//                    JSONObject userJSON = new JSONObject();
//                    userJSON.put("name",user.getName());
//                    userJSON.put("phone",user.getPhone());
//                    userJSON.put("password",user.getPassword());
//
//                    String content = String.valueOf(userJSON);
//
//                    conn.setConnectTimeout(5000);
//                    conn.setRequestMethod("POST");
//                    conn.setDoOutput(true);
//                    conn.setRequestProperty("User-Agent", "Fiddler");
//                    conn.setRequestProperty("Content-Type", "application/json");
//                    conn.setRequestProperty("Charset", "UTF-8");
//                    OutputStream os = conn.getOutputStream();
//                    os.write(content.getBytes());
//                    os.close();
//
//                    InputStream inStream = conn.getInputStream();
//
//                    while ((len = inStream.read(data)) != -1) {
//
//                        outStream.write(data, 0, len);
//
//                    }
//
//                    inStream.close();
//
//
//                } catch (Exception e) {
//                    Log.d(KEY, e.toString());
//                }
//            }
//        }).start();
//
//        Thread.sleep(100);
//
//        return outStream.toByteArray();
//
//    }

    public static byte[] readParse(String urlPath, final User user) throws Exception {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        final byte[] data = new byte[1024];

        final URL url = new URL(urlPath);

        int len = 0;

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        JSONObject userJSON = new JSONObject();
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

//    /**
//     * 用户注册
//     */
//    public static boolean userRegister(final User user)
//    {
//        new Thread()
//        {
//            @Override
//            public void run()
//            {
//                try {
//                    JSONObject userJSON = new JSONObject();
//                    userJSON.put("name",user.getName());
//                    userJSON.put("userPhone",user.getPhone());
//                    userJSON.put("userPassword",user.getPassword());
//
//                    String content = String.valueOf(userJSON);
//
//                    //HttpURLConnection connection  =
//                    /**
//                     * 请求地址
//                     */
//                    String url = serverURL +"excel/account/findAll";
//
//                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
//                    //HttpURLConnection connection = (HttpURLConnection) nURL.openConnection();
//                    connection.setConnectTimeout(5000);
//                    connection.setRequestMethod("POST");
//                    connection.setDoOutput(true);
//                    connection.setRequestProperty("User-Agent", "Fiddler");
//                    connection.setRequestProperty("Content-Type", "application/json");
//                    connection.setRequestProperty("Charset", "UTF-8");
//                    OutputStream os = connection.getOutputStream();
//                    os.write(content.getBytes());
//                    os.close();
//                    /**
//                     * 服务器返回结果
//                     * 继续干什么事情....待续
//                     */
////                    String result = read(connection.getInputStream());
//
//                    Log.i("success","成功注册");
//
//                }catch (Exception e)
//                {
//                    Log.e(KEY, e.toString());
//                }
//            }
//        }.start();
//        return true;
//    }
}

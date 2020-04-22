package com.baidu.automap.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.automap.R;
import com.baidu.automap.entity.User;
import com.baidu.automap.util.HttpUtil;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText userId;
    private EditText password;
    private EditText verifyPassword;
    private Button loginButton;

    private static int debugId = 0;

    private static final String KEY = "loginActivity";

    private User curUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_activity);

        userId = (EditText) findViewById(R.id.user_id);
        password = (EditText) findViewById(R.id.user_password);
        verifyPassword = (EditText) findViewById(R.id.user_password_verify);
        loginButton = (Button) findViewById(R.id.login_button);

        curUser = new User();

        //注册
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userIdInput = userId.getText().toString();
                final String passwordInput = password.getText().toString();
                final String verifyPasswordInput = verifyPassword.getText().toString();

                if(userIdInput != null && passwordInput != null && verifyPasswordInput != null
                && userIdInput.length() != 0 && passwordInput.length() != 0 && verifyPasswordInput.length() != 0) {
                    //确认密码一致
                    if(passwordInput.compareTo(verifyPasswordInput) != 0) {
                        Toast.makeText(LoginActivity.this, "两次密码不一致", Toast.LENGTH_SHORT).show();
                    } else {

//                            //处理返回结果
//                            Thread thread = new Thread(new Runnable() {
//
//                                @Override
//                                public void run() {
//                                    User user = new User();
//
//                                    user.setPhone(userIdInput);
//                                    user.setPassword(passwordInput);
//
//                                    byte[] result = null;
//                                    try {
//                                        result = HttpUtil.readUserParse("saveUser", user);
//
//                                        JSONObject userResponse = new JSONObject(new String(result));
//                                        Log.d(KEY, "返回结果： " + new String(result));
//
//                                        if(userResponse.getString("message").compareTo("success!") == 0) {
//                                            //注册成功
//                                            JSONObject newUser = new JSONObject(userResponse.getString("user"));
//                                            Log.d(KEY, "user : " + newUser.getString("phone"));
//
//                                            curUser.setPhone(newUser.getString("phone"));
//                                        } else {
//                                            Log.d(KEY, userResponse.getString("message"));
//                                            //注册失败
//                                            Toast.makeText(LoginActivity.this, userResponse.getString("message"),
//                                                    Toast.LENGTH_SHORT).show();
//                                        }
//                                    } catch (Exception e) {
//                                        Log.e(KEY, e.toString());
//                                    }
//
//                                }
//                            });

                        ThreadLogin thread = new ThreadLogin(userIdInput, passwordInput);

                        try {
                            thread.start();
                            thread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if(thread.getIsSuccess()) {
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putString("phone", curUser.getPhone());
                            intent.putExtras(bundle);

                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, thread.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                } else {
                    Toast.makeText(LoginActivity.this, "请输入账号及密码", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class ThreadLogin extends Thread {
        private boolean isSuccess = false;
        private String message;

        private String userIdInput;
        private String passwordInput;

        public ThreadLogin(String userIdInput, String passwordInput) {
            this.userIdInput = userIdInput;
            this.passwordInput = passwordInput;
        }

        public boolean getIsSuccess() {
            return isSuccess;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public void run() {
            User user = new User();

            user.setPhone(userIdInput);
            user.setPassword(passwordInput);

            byte[] result = null;
            try {
                result = HttpUtil.readUserParse("saveUser", user);

                JSONObject userResponse = new JSONObject(new String(result));
                Log.d(KEY, "返回结果： " + new String(result));

                if(userResponse.getString("message").compareTo("success!") == 0) {
                    //注册成功
                    isSuccess = true;
                    JSONObject newUser = new JSONObject(userResponse.getString("user"));
                    Log.d(KEY, "user : " + newUser.getString("phone"));

                    curUser.setPhone(newUser.getString("phone"));
                } else {
                    Log.d(KEY, userResponse.getString("message"));
                    //注册失败
                    isSuccess = false;
                    message = userResponse.getString("message");
                }
            } catch (Exception e) {
                Log.e(KEY, e.toString());
            }

        }
    }
}

package com.baidu.automap.user;

import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.automap.R;

public class LoginActivity extends AppCompatActivity {

    private EditText userId;
    private EditText password;
    private EditText verifyPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_activity);
    }
}

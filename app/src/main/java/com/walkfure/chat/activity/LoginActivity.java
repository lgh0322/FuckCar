package com.walkfure.chat.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.PhoneUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.walkfure.chat.data.PersonalInfo;
import com.walkfure.chat.R;
import com.walkfure.chat.service.SocketService;
import com.walkfure.chat.utils.GetAndroidUniqueMark;

import io.socket.client.Socket;


public class LoginActivity extends AppCompatActivity {
    int state = 0;
    EditText editText;
    ImageView imageView;
    TextView textHint;
    Button buttonYes;
    String phone;
    BroadcastReceiver broadcastReceiver;

    public static LoginActivity loginActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, SocketService.class));

            startActivity(new Intent(this, MainActivity.class));
        SPUtils.getInstance().put(PersonalInfo.A1, GetAndroidUniqueMark.getUniqueId( this));
        setContentView(R.layout.activity_login);
        loginActivity = this;

        editText = findViewById(R.id.a1);
        imageView = findViewById(R.id.img);
        textHint = findViewById(R.id.text2);
        buttonYes = findViewById(R.id.yesX);

        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(SocketService.LoginSuccess)) {
                        SPUtils.getInstance().put(PersonalInfo.A1, phone);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        LoginActivity.this.finish();
                        LoginActivity.loginActivity = null;
                    }
                    if (action.equals(SocketService.sendPhoneSmsResult)) {
                        if (state == 0) {
                            imageView.setImageResource(R.drawable.car1);
                            editText.setHint("验证码");
                            editText.setText("");
                            editText.setBackgroundResource(R.drawable.shape_color_000002);
                            textHint.setText("验证码已发送到 +86 " + phone);
                            buttonYes.setText("完成");
                            buttonYes.setTextColor(Color.YELLOW);
                            buttonYes.setBackgroundColor(Color.RED);
                            state = 3;
                        }

                    }

                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SocketService.sendPhoneSmsResult);
            intentFilter.addAction(SocketService.LoginSuccess);
            registerReceiver(broadcastReceiver, intentFilter);
        }


    }


    public void next(View e) {
        String x = editText.getText().toString();
        boolean y = com.walkfure.chat.utils.PhoneUtils.isMobile(x);

        if (state == 0) {
            if (!y) {
                ToastUtils.showShort("请输入正确的手机号");
                return;
            }
            Intent intent = new Intent(SocketService.submitPhone);
            phone = x;
            intent.putExtra("msg", x);
            sendBroadcast(intent);
        } else if (state == 3) {
            if (x.length() != 6) {
                ToastUtils.showShort("请输入正确的验证码");
                return;
            }

            Intent intent = new Intent(SocketService.submitSMS);
            intent.putExtra("msg", x);
            intent.putExtra("msg2", phone);
            sendBroadcast(intent);
        }


    }

    public void onDestroy() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver.clearAbortBroadcast();
            broadcastReceiver = null;
        }
        super.onDestroy();
    }


}
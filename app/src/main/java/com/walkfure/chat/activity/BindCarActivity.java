package com.walkfure.chat.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.walkfure.chat.MyApplication;
import com.walkfure.chat.R;
import com.walkfure.chat.data.PersonalInfo;
import com.walkfure.chat.service.SocketService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;

import io.socket.client.Socket;


public class BindCarActivity extends AppCompatActivity {
    EditText apnName;
    EditText apnApn;
    Button apnButton;
    int buttonState = 0;
    ImageView imageView;
    AsyncPlayer asyncPlayer = new AsyncPlayer(null);
    Uri uri;
    BroadcastReceiver broadcastReceiver;

    /**
     * 生成简单二维码
     *
     * @param content                字符串内容
     * @param width                  二维码宽度
     * @param height                 二维码高度
     * @param character_set          编码方式（一般使用UTF-8）
     * @param error_correction_level 容错率 L：7% M：15% Q：25% H：35%
     * @param margin                 空白边距（二维码与边框的空白区域）
     * @param color_black            黑色色块
     * @param color_white            白色色块
     * @return BitMap
     */
    public static Bitmap createQRCodeBitmap(String content, int width, int height,
                                            String character_set, String error_correction_level,
                                            String margin, int color_black, int color_white) {
        // 字符串内容判空
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        // 宽和高>=0
        if (width < 0 || height < 0) {
            return null;
        }
        try {
            /** 1.设置二维码相关配置 */
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            // 字符转码格式设置
            if (!TextUtils.isEmpty(character_set)) {
                hints.put(EncodeHintType.CHARACTER_SET, character_set);
            }
            // 容错率设置
            if (!TextUtils.isEmpty(error_correction_level)) {
                hints.put(EncodeHintType.ERROR_CORRECTION, error_correction_level);
            }
            // 空白边距设置
            if (!TextUtils.isEmpty(margin)) {
                hints.put(EncodeHintType.MARGIN, margin);
            }
            /** 2.将配置参数传入到QRCodeWriter的encode方法生成BitMatrix(位矩阵)对象 */
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            /** 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值 */
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    //bitMatrix.get(x,y)方法返回true是黑色色块，false是白色色块
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = color_black;//黑色色块像素设置
                    } else {
                        pixels[y * width + x] = color_white;// 白色色块像素设置
                    }
                }
            }
            /** 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,并返回Bitmap对象 */
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void hideSoftInputView() {
        InputMethodManager manager = ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_car);
        apnApn = findViewById(R.id.apn_apn);
        apnName = findViewById(R.id.name_apn);
        apnButton = findViewById(R.id.apn_button);
        imageView = findViewById(R.id.qr_image);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("phone", SPUtils.getInstance().getString(PersonalInfo.A1));
            Bitmap bitmap = createQRCodeBitmap(jsonObject.toString(), 800, 800, "UTF-8", "H", "1", Color.BLACK, Color.WHITE);
            imageView.setImageBitmap(bitmap);

        } catch (JSONException e) {

        }


        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(SocketService.bindCarSuccess)) {
                        uri = Uri.parse("android.resource://" + MyApplication.getInstance().getPackageName() + "/" + R.raw.bind_success);
                        asyncPlayer.play(MyApplication.getInstance(), uri, false, AudioManager.STREAM_MUSIC);
                        BindCarActivity.this.finish();
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SocketService.bindCarSuccess);
            registerReceiver(broadcastReceiver, intentFilter);
        }


        apnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonState == 0) {
                    apnButton.setText("完成");
                    apnApn.setVisibility(View.VISIBLE);
                    apnName.setVisibility(View.VISIBLE);
                    buttonState++;
                } else if (buttonState == 1) {
                    String x1 = apnName.getText().toString();
                    String x2 = apnApn.getText().toString();
                    if (x1.length() == 0) return;
                    if (x2.length() == 0) return;
                    try {

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("x1", x1);
                        jsonObject.put("x2", x2);
                        jsonObject.put("phone", SPUtils.getInstance().getString(PersonalInfo.A1));
                        if (jsonObject.toString().length() > 50) return;
                        Bitmap bitmap = createQRCodeBitmap(jsonObject.toString(), 800, 800, "UTF-8", "H", "1", Color.BLACK, Color.WHITE);
                        imageView.setImageBitmap(bitmap);
                        buttonState = 0;
                        hideSoftInputView();
                        apnButton.setText("设置成功");
                        apnApn.setVisibility(View.INVISIBLE);
                        apnName.setVisibility(View.INVISIBLE);


                    } catch (JSONException e) {

                    }

                }
            }
        });


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
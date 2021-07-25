package com.walkfure.chat.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SPUtils;
import com.walkfure.chat.R;
import com.walkfure.chat.adapter.ChatAdapter;
import com.walkfure.chat.bean.ChatBean;
import com.walkfure.chat.bean.ContactsBean;
import com.walkfure.chat.data.PersonalInfo;
import com.walkfure.chat.service.SocketService;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ChatActivity extends AppCompatActivity {
    LinearLayout moreLinearLayout;
    PopupWindow popupWindow;
    Button btn_chat_message_send;
    RecyclerView rc;
    ChatAdapter chatAdapter;
    List<ChatBean> chatMsg = new ArrayList<>();
    EditText editText;
    TextView chatName;
    LinearLayout bottom_send;
    LinearLayout bottom_send_blank;
    LinearLayout voice_send;
    private LinearLayout chat_face_container;
    ImageButton plus;
    String from;
    BroadcastReceiver broadcastReceiver;
    String phone;
    ArrayList<ChatBean> msgHis;
    Handler handler = new Handler();

    public void hideSoftInputView() {
        InputMethodManager manager = ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new KeyboardListener());

        setContentView(R.layout.activity_chat);


        plus = findViewById(R.id.plus);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottom_send.getVisibility() == GONE) {
                    hideSoftInputView();
                    bottom_send.setVisibility(VISIBLE);
                    voice_send.setVisibility(GONE);
                    chat_face_container.setVisibility(GONE);
                } else {
                    bottom_send.setVisibility(GONE);
                }
            }
        });
        bottom_send = findViewById(R.id.send_option_container);
        bottom_send_blank = findViewById(R.id.send_option_blank);
        voice_send = findViewById(R.id.send_voice_container);
        btn_chat_message_send = (Button) findViewById(R.id.btn_chat_message_send);
        from = SPUtils.getInstance().getString(PersonalInfo.A1);
        editText = findViewById(R.id.chat_message);
        rc = findViewById(R.id.rc);
        chatAdapter = new ChatAdapter(this, rc);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rc.setLayoutManager(linearLayoutManager);
        rc.setAdapter(chatAdapter);

        chatName = findViewById(R.id.chat_view_text);
        moreLinearLayout = findViewById(R.id.more1);
        phone = getIntent().getStringExtra("phone");


        editText.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    bottom_send.setVisibility(GONE);


                }
                return false;
            }
        });


        if (phone != null) {
            ContactsBean bean = SocketService.contactsInfo.get(phone);
            msgHis = bean.getMsgList();
            if (msgHis.size() > 0) {
                chatAdapter.setList(msgHis);
            }

            if (bean != null) {
                String name = bean.getName();
                if (name != null) {
                    chatName.setText(name);
                }
            }
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (chatAdapter.getItemCount() > 0) {
                    rc.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                }

            }
        }, 100);

        btn_chat_message_send.setVisibility(GONE);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                btn_chat_message_send.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_chat_message_send.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editText.getText().toString().length() == 0) {
                    btn_chat_message_send.setVisibility(GONE);
                }

            }
        });


        chat_face_container = (LinearLayout) findViewById(R.id.chat_face_container);


        View pop = View.inflate(this, R.layout.chat_pop_windows, null);

        popupWindow = new PopupWindow(pop, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setOutsideTouchable(true);//设置点击外部区域可以取消popupWindow

        TextView a = pop.findViewById(R.id.a1);
        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog2 = new AlertDialog.Builder(ChatActivity.this)
                        .setTitle("确定要删除好友吗")
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(SocketService.deleteAFriend);
                                intent.putExtra("msg", phone);
                                sendBroadcast(intent);
                                ChatActivity.this.finish();

                            }
                        })

                        .setNeutralButton("取消", new DialogInterface.OnClickListener() {//添加取消
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create();
                alertDialog2.show();
                popupWindow.dismiss();
            }
        });

        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(SocketService.ChatReceive)) {
                        String from = intent.getStringExtra("from");
                        if (from.equals(phone)) {
                            String content = intent.getStringExtra("content");
                            ChatBean da = new ChatBean();
                            da.setChatMessage(content);
                            da.setChatType(1);
                            da.setId(from);
                            da.setImg(SocketService.contactsInfo.get(from).getAvatar());
                            chatMsg.add(da);
                            chatAdapter.setData(da);
                            rc.scrollToPosition(chatAdapter.getItemCount() - 1);
                        }
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SocketService.ChatReceive);
            registerReceiver(broadcastReceiver, intentFilter);
        }


    }

    public void quit(View e) {
        this.finish();
    }


    /**
     * 获取一个视图的宽高（软键盘）
     */
    private class KeyboardListener implements ViewTreeObserver.OnGlobalLayoutListener {

        private boolean isShow;

        private int getScreenHeight() {
            DisplayMetrics outMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(outMetrics);
            return outMetrics.heightPixels;
        }

        @Override
        public void onGlobalLayout() {
            Rect rect = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            int screenHeight = getScreenHeight();
            int keyboardHeight = screenHeight - rect.bottom;//软键盘高度

            if (Math.abs(keyboardHeight) > screenHeight / 5 && !isShow) {

                if (rc != null && chatAdapter != null && chatAdapter.getItemCount() != 0) {
                    rc.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                }
                isShow = true;
            } else {
                isShow = false;
            }
        }
    }


    public void onPause() {
        Intent intent = new Intent(SocketService.setRedTextZero);
        intent.putExtra("phone", phone);
        sendBroadcast(intent);
        super.onPause();
    }

    public void more(View e) {
        popupWindow.showAsDropDown(moreLinearLayout, 0, 0, Gravity.END);//设置popupWindow显示,并且告诉它显示在那个View下面
    }

    public void sendText(View e) {
        String x = editText.getText().toString();
        if (x.length() == 0) return;
        ChatBean da = new ChatBean();
        da.setChatMessage(x);
        da.setChatType(0);
        chatMsg.add(da);
        chatAdapter.setData(da);
        SocketService.contactsInfo.get(phone).addMsg(da);
        Intent intent = new Intent(SocketService.MsgMeSend);
        intent.putExtra("content", x);
        intent.putExtra("phone", phone);
        sendBroadcast(intent);

        rc.scrollToPosition(chatAdapter.getItemCount() - 1);

        try {
            JSONObject chat = new JSONObject();
            chat.put("content", editText.getText());
            chat.put("to", phone);
            chat.put("from", from);
            chat.put("type", 0);
            Intent send = new Intent();
            send.setAction(SocketService.ChatSend);
            send.putExtra("msg", chat.toString());
            sendBroadcast(send);
        } catch (JSONException xxex) {

        } finally {
            editText.setText("");
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

package com.walkfure.chat.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.SPUtils;
import com.walkfure.chat.R;
import com.walkfure.chat.data.PersonalInfo;
import com.walkfure.chat.service.SocketService;

public class TransferRequestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String phone = getIntent().getStringExtra("phone");
        String car = getIntent().getStringExtra("carId");
        SPUtils.getInstance().put(PersonalInfo.CarId, car);
        String name = SocketService.contactsInfo.get(phone).getName();
        AlertDialog alertDialog2 = new AlertDialog.Builder(this)
                .setTitle(name)
                .setMessage("想转移遥控车控制权给你, 接受吗")
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("接受", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(TransferRequestActivity.this, TransferControlActivity.class);
                        intent.putExtra("carId", car);
                        intent.putExtra("masterId", phone);
                        TransferRequestActivity.this.startActivity(intent);

                        TransferRequestActivity.this.finish();
                    }
                })
                .setNeutralButton("取消", new DialogInterface.OnClickListener() {//添加普通按钮
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        TransferRequestActivity.this.finish();
                    }
                })
                .create();
        alertDialog2.show();

    }
}

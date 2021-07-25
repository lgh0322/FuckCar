package com.walkfure.chat.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.walkfure.chat.MyApplication;
import com.walkfure.chat.R;
import com.walkfure.chat.activity.MainActivity;
import com.walkfure.chat.bean.CarBean;

import java.util.ArrayList;
import java.util.List;

import static android.view.Gravity.CENTER;

public class CarViewAdapter extends RecyclerView.Adapter<CarViewAdapter.ViewHolder> {
    private List<CarBean> mCarData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context mContext;

    // data is passed into the constructor
    public CarViewAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.mCarData = new ArrayList<>();
        mContext = context;
    }

    public void HideOption() {
        for (int k = 0; k < mCarData.size(); k++) {
            mCarData.get(k).setShowOption(false);
        }
        notifyDataSetChanged();
    }

    public String getCarID(int position) {
        return mCarData.get(position).getId();
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.car_view, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.myTextView.setText(mCarData.get(position).getName());
        if (mCarData.get(position).isShowOption()) {
            holder.carOption.setVisibility(View.VISIBLE);
        } else {
            holder.carOption.setVisibility(View.INVISIBLE);
        }
        if (mCarData.get(position).getName().equals("添加车辆")) {
            holder.car.setImageResource(R.drawable.add);
        } else {
            holder.car.setImageResource(R.drawable.car1);
        }
    }

    public void addCar(String id, String name) {
        CarBean carBean = new CarBean();
        carBean.setId(id);
        carBean.setName(name);
        mCarData.add(carBean);
        notifyDataSetChanged();
    }

    public void addCar(String a) {
        CarBean carBean = new CarBean();
        carBean.setId(a);
        mCarData.add(carBean);
        notifyDataSetChanged();
    }

    public void addBindCar(String a) {
        CarBean carBean = new CarBean();
        carBean.setId(a);
        mCarData.add(0, carBean);
        notifyDataSetChanged();
    }

    public void addBindCar(String id, String name) {
        CarBean carBean = new CarBean();
        carBean.setId(id);
        carBean.setName(name);
        mCarData.add(0, carBean);
        notifyDataSetChanged();
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mCarData.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;
        ImageView car;
        GridLayout carOption;
        Button delButton;
        Button shareButton;
        Button chNameButton;
        Button chIconButton;

        ViewHolder(View itemView) {
            super(itemView);
            car = itemView.findViewById(R.id.cars);
            myTextView = itemView.findViewById(R.id.info_text);
            carOption = itemView.findViewById(R.id.GridLayoutOption);
            delButton = itemView.findViewById(R.id.del);
            shareButton = itemView.findViewById(R.id.share);
            chNameButton = itemView.findViewById(R.id.chName);
            chIconButton = itemView.findViewById(R.id.chIcon);

            chNameButton.setOnClickListener(this);
            chIconButton.setOnClickListener(this);
            shareButton.setOnClickListener(this);
            delButton.setOnClickListener(this);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (getAdapterPosition() == mCarData.size() - 1) {
                        return false;
                    }
                    carOption.setVisibility(View.VISIBLE);
                    return true;
                }
            });
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.del) {
                AlertDialog alertDialog2 = new AlertDialog.Builder(mContext)
                        .setTitle("解绑并删除遥控车")
                        .setMessage("确定要解绑并删除遥控车吗？")
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mCarData.remove(getAdapterPosition());
                                notifyDataSetChanged();
                            }
                        })

                        .setNeutralButton("取消", new DialogInterface.OnClickListener() {//添加普通按钮
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create();
                alertDialog2.show();

                HideOption();
                return;
            } else if (view.getId() == R.id.chName) {

                HideOption();

                return;
            } else if (view.getId() == R.id.chIcon) {

                HideOption();
                return;
            } else if (view.getId() == R.id.share) {

                HideOption();
                return;
            }

            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    CarBean getItem(int id) {
        return mCarData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
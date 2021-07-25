package com.walkfure.chat.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.walkfure.chat.R;
import com.walkfure.chat.activity.BindCarActivity;
import com.walkfure.chat.activity.MainControlActivity;
import com.walkfure.chat.adapter.CarViewAdapter;
import com.walkfure.chat.data.PersonalInfo;
import com.walkfure.chat.service.BluetoothLeService;
import com.walkfure.chat.service.SocketService;
import com.walkfure.chat.utils.FileUtils;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileWithBitmapCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

import static com.walkfure.chat.service.BluetoothLeService.mBluetoothLeService;

public class CarFragment extends BaseFragment implements CarViewAdapter.ItemClickListener {
    //    @BindView(R.id.controller)
//    TextView Controller;
//    @BindView(R.id.main_control)
//    TextView main_control;
//    @BindView(R.id.bind_car)
//    TextView bind_car;
    public static int carNumber;
    @BindView(R.id.cars)
    RecyclerView carView;
    @BindView(R.id.network)
    CardView networkCard;
    CarViewAdapter carViewAdapter;
    @BindView(R.id.iv_right)
    ImageView mor;
    PopupWindow pwindow;
    TextView moreX;
    TextView tt;
    FileUtils fileUtils;
    public static List<String> carList = new ArrayList<>();
    BluetoothManager bluetoothManager;
    public static String HEART_RATE_MEASUREMENT = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static BluetoothGattCharacteristic target_chara = null;
    BluetoothAdapter mBluetoothAdapter;
    LeDeviceListAdapter mleDeviceListAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    private boolean mScanning;
    private boolean scan_flag;
    float scrollX, scrollY;
    private Handler mHandler;
    private Button scan_btn;
    ListView lv;
    RelativeLayout rl;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private Handler mhandler = new Handler();
    private static final int REQUEST_LOCATION = 223;
    private static final int REQUEST_ENABLE_BT = 224;

    public int getLayoutId() {
        return R.layout.fragment_car;
    }

    public static CarFragment newInstance() {

        Bundle args = new Bundle();

        CarFragment fragment = new CarFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public void onItemClick(View view, int position) {

        if (position == carViewAdapter.getItemCount() - 1) {
            mContext.startActivity(new Intent(mContext, BindCarActivity.class));
            return;
        }
        Log.e("打算离开房间", carViewAdapter.getCarID(position));
        Intent intent = new Intent(mContext, MainControlActivity.class);
        intent.putExtra("carId", carViewAdapter.getCarID(position));
        mContext.startActivity(intent);

    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {

        if (gattServices == null)
            return;
        String uuid = null;
        String unknownServiceString = "unknown_service";
        String unknownCharaString = "unknown_characteristic";


        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();


        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();


        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();


        for (BluetoothGattService gattService : gattServices) {


            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();


            gattServiceData.add(currentServiceData);

            System.out.println("Service uuid:" + uuid);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();


            List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                    .getCharacteristics();

            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();


            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();

                if (gattCharacteristic.getUuid().toString()
                        .equals(HEART_RATE_MEASUREMENT)) {

                    mhandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {

                            mBluetoothLeService
                                    .readCharacteristic(gattCharacteristic);
                        }
                    }, 200);


                    mBluetoothLeService.setCharacteristicNotification(
                            gattCharacteristic, true);
                    target_chara = gattCharacteristic;


                }
                List<BluetoothGattDescriptor> descriptors = gattCharacteristic
                        .getDescriptors();
                for (BluetoothGattDescriptor descriptor : descriptors) {
                    System.out.println("---descriptor UUID:"
                            + descriptor.getUuid());

                    mBluetoothLeService.getCharacteristicDescriptor(descriptor);


                }

                gattCharacteristicGroupData.add(currentCharaData);
            }

            mGattCharacteristics.add(charas);

            gattCharacteristicData.add(gattCharacteristicGroupData);

        }

    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals("chIcon")) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 22);
            } else if (SocketService.bindCarSuccess.equals(action)) {
                String carId = intent.getStringExtra("carId");
                carList.add(0, carId);
                carViewAdapter.addBindCar(carId,"我的遥控车" + (carNumber + 1));
                carNumber++;

            } else if (SocketService.returnCarList.equals(action)) {
                networkCard.setVisibility(View.GONE);
                String arr = intent.getStringExtra(SocketService.returnCarList);
                try {
                    JSONArray jsonArray = new JSONArray(arr);
                    carList.clear();
                    carNumber=jsonArray.length();
                    for (int k = 0; k < jsonArray.length(); k++) {
                        carList.add(0, jsonArray.get(k).toString());
                        carViewAdapter.addBindCar(jsonArray.get(k).toString(), "我的遥控车" + (k + 1));
                        Log.e("对方", "两点上课解放路口" + jsonArray.get(k).toString());
                    }
                 /*   if(jsonArray.length()==0){
                        carList.add(0, "d4a3c569fe60a325");
                        carViewAdapter.addBindCar("d4a3c569fe60a325", "我的遥控车1");
                    }*/
                    carViewAdapter.addCar("", "添加车辆");
                } catch (JSONException ee) {

                }
            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

                System.out.println("BroadcastReceiver :" + "device connected");

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
                    .equals(action)) {

                System.out.println("BroadcastReceiver :"
                        + "device disconnected");

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                displayGattServices(mBluetoothLeService
                        .getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                int l = data.length;
                int[] datax = new int[l];
                String da = "";
                for (int k = 1; k < 9; k += 2) {
                    int a = (data[k] & 0xff);
                    int b = (data[k + 1] & 0xff);
                    int c = (b - 1) * 255 + a - 1;
                    da = da + c + "  ";
                }
                System.out.println(da);
            }
        }
    };


    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            mleDeviceListAdapter.addDevice(result.getDevice(), result.getRssi());
            mleDeviceListAdapter.notifyDataSetChanged();
        }


        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
//        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
//        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
//        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(SocketService.bindCarSuccess);
        intentFilter.addAction(SocketService.returnCarList);
        intentFilter.addAction("chIcon");
        return intentFilter;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View pop = View.inflate(mContext, R.layout.car_pop_windows, null);
        PopupWindow popupWindow = new PopupWindow(pop, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);//设置点击外部区域可以取消popupWindow
        mor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.setFocusable(true);
                popupWindow.setOutsideTouchable(true);
                popupWindow.showAsDropDown(mor);
                popupWindow.update();
            }
        });
        moreX = pop.findViewById(R.id.a1);
        moreX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AskPermission();
                popupWindow.dismiss();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fileUtils = new FileUtils(mContext);
        mContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        mContext.startService(new Intent(mContext, BluetoothLeService.class));


        View contentview = LayoutInflater.from(mContext).inflate(R.layout.bind_ble, null);
        rl = contentview.findViewById(R.id.rl);
        lv = contentview.findViewById(R.id.lv);
        lv.setOnItemClickListener((arg0, v, position, id) -> {
            bluetoothLeScanner.stopScan(mLeScanCallback);
            final BluetoothDevice device = mleDeviceListAdapter
                    .getDevice(position);
            SPUtils.getInstance().put(PersonalInfo.BLEAddress, device.getAddress());
            ToastUtils.showShort("绑定遥控器成功");
            pwindow.dismiss();

        });

        mleDeviceListAdapter = new LeDeviceListAdapter();
        lv.setAdapter(mleDeviceListAdapter);
        pwindow = new PopupWindow(contentview, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        pwindow.setOutsideTouchable(true);

        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothLeScanner.stopScan(mLeScanCallback);
                pwindow.dismiss();
            }
        });


        //------------------------------------------------------------carview relative
        carView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        carViewAdapter = new CarViewAdapter(mContext);
        carView.setAdapter(carViewAdapter);
        carViewAdapter.setClickListener(this);
        carView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                scrollX = event.getX();
                scrollY = event.getY();
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (v.getId() != 0 && Math.abs(scrollX - event.getX()) <= 5 && Math.abs(scrollY - event.getY()) <= 5) {
                    carViewAdapter.HideOption();
                }
            }
            return false;
        });

    }

//    @OnClick({R.id.controller,R.id.main_control,R.id.bind_car})
//    public void onViewClicked(View view) {
//        if (view.getId() == R.id.controller) {
//            AskPermission();
//        }else if(view.getId()==R.id.main_control){
//            mContext.startActivity(new Intent(mContext, MainControlActivity.class));
//        }else if(view.getId()==R.id.bind_car){
//            mContext.startActivity(new Intent(mContext, BindCarActivity.class));
//
//        }
//    }

    //--------------------------------ble location permission
    public boolean isLocationEnabled() {
        int locationMode = 0;
        String locationProviders;
        try {
            locationMode = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.LOCATION_MODE);

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }


    //-------------------------------------------------------------BLe Permmision
    private void AskPermission() {
        List<PermissionItem> permissionItems = new ArrayList<PermissionItem>();

        permissionItems.add(new PermissionItem(Manifest.permission.BLUETOOTH, "蓝牙", R.drawable.permission_ic_sensors));
        //	permissionItems.add(new PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, "存储卡", R.drawable.permission_ic_storage));
        permissionItems.add(new PermissionItem(Manifest.permission.BLUETOOTH_ADMIN, "蓝牙管理", R.drawable.permission_ic_micro_phone));
        permissionItems.add(new PermissionItem(Manifest.permission.ACCESS_FINE_LOCATION, "定位", R.drawable.permission_ic_location));

        HiPermission.create(mContext).permissions(permissionItems)
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {

                    }

                    @Override
                    public void onFinish() {
                        if (!isLocationEnabled()) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, REQUEST_LOCATION);
                        } else {
                            bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
                            mBluetoothAdapter = bluetoothManager.getAdapter();
                            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                                Intent enableBtIntent = new Intent(
                                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                            } else {
                                pwindow.setFocusable(true);
                                pwindow.setOutsideTouchable(true);
                                pwindow.showAsDropDown(mor);
                                pwindow.update();
                                bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                                bluetoothLeScanner.startScan(mLeScanCallback);
                            }

                        }

                    }

                    @Override
                    public void onDeny(String permission, int position) {

                    }

                    @Override
                    public void onGuarantee(String permission, int position) {

                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOCATION) {
            if (!isLocationEnabled()) {
                ToastUtils.showShort("请开启位置信息， 否则蓝牙BLE无法扫描");
                return;
            }
        } else if (requestCode == REQUEST_ENABLE_BT) {

        } else if (requestCode == 22
                && resultCode == Activity.RESULT_OK && null != data) {
            System.out.println("老师的科技分类考试的缴费");
            try {
                Uri selectedImage = data.getData();
                String filePath = fileUtils.getPath(selectedImage);
                Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
                Tiny.getInstance().source(filePath).asFile().withOptions(options).compress(new FileWithBitmapCallback() {
                    @Override
                    public void callback(boolean isSuccess, Bitmap bitmap, String outfile, Throwable t) {
//                        final Intent intent=new Intent(SocketService.uploadCarAvatar);
//                        intent.putExtra("path",outfile);
//                        mContext.sendBroadcast(intent);
//                        SPUtils.getInstance().put(PersonalInfo.AvatarCarPath,outfile);
//                        mContext.sendBroadcast(new Intent(SocketService.updateCarAvatar));
                    }
                });
            } catch (Exception e) {

            }


        }

    }


    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;

        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device, int rssi) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = mInflator.inflate(R.layout.item_ble, null);

            TextView deviceAddress = (TextView) view
                    .findViewById(R.id.tv_deviceAddr);
            TextView deviceName = (TextView) view
                    .findViewById(R.id.tv_deviceName);


            BluetoothDevice device = mLeDevices.get(i);
            deviceAddress.setText(device.getAddress());
            deviceName.setText(device.getName());

            return view;
        }


    }


}

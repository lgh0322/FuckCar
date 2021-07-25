package com.walkfure.chat.activity;


import android.Manifest;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.baidu.mapapi.map.MapView;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.walkfure.chat.R;
import com.walkfure.chat.data.PersonalInfo;
import com.walkfure.chat.fragment.ContactsFragment;
import com.walkfure.chat.fragment.MyFragment;
import com.walkfure.chat.fragment.setting.SettingBatteryFragment;
import com.walkfure.chat.fragment.setting.SettingCarFragment;
import com.walkfure.chat.fragment.setting.SettingCotrolerFragment;
import com.walkfure.chat.fragment.setting.SettingMoreFragment;
import com.walkfure.chat.fragment.setting.SettingVideoFragment;
import com.walkfure.chat.service.BluetoothLeService;
import com.walkfure.chat.service.SocketService;
import com.walkfure.chat.utils.FileEncoder;
import com.walkfure.chat.utils.ImageEncoder;
import com.walkfure.chat.view.CircleImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

import static com.walkfure.chat.service.BluetoothLeService.mBluetoothLeService;

public class MainControlActivity extends AppCompatActivity implements View.OnClickListener {
    ImageEncoder phoneX;
    String savePhoto="";
    Handler fileChange=new Handler();
    String saveAs="";
    boolean startRecord=false;
    FileEncoder recording;
    public static boolean das = false;
    private static ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    public static String HEART_RATE_MEASUREMENT = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static BluetoothGattCharacteristic target_chara = null;
    private static boolean mConnected = false;
    static Handler mhandler = new Handler();
    boolean yes = false;
    boolean mapSta = false;
    RelativeLayout control_stick;
    MapView mapView;
    TextView mapButton;
    private ImageView control_setting0;
    private ImageView control_setting1;
    private ImageView control_setting2;
    private ImageView control_setting3;
    private ImageView control_setting4;
    private SettingBatteryFragment settingBatteryFragment;
    private SettingCarFragment settingCarFragment;
    private SettingCotrolerFragment settingCotrolerFragment;
    private SettingMoreFragment settingMoreFragment;
    private SettingVideoFragment settingVideoFragment;
    private View setting_return;
    ImageView[] images;
    Fragment[] settingFragments;
    Handler testHandler = new Handler();
    boolean isdada = false;
    AudioSource audioSource;
    private String carId;
    FragmentTransaction transaction;

    View controlSetting;

    FriendListAdapter friendListAdapter;
    PopupWindow newFriendWindow;
    PopupWindow settingWindow;
    ListView lv;
    RelativeLayout rl;

    FragmentManager fragmentManager;
    private LinearLayout chartTools;
    private TextView setting;
    private TextView switcCamera;
    private TextView loundSperaker;
    TextView vx2;
    TextView vx1;

    private SurfaceViewRenderer remoteView;
    private PeerConnectionFactory mPeerConnectionFactory;
    private CameraVideoCapturer mVideoCapturer;
    private VideoTrack mVideoTrack;
    private AudioTrack mAudioTrack;
    private EglBase mEglBase;
    private MediaStream mMediaStream;
    private MediaStream mMediaStreamShare;

    private MediaConstraints pcConstraints;
    private LinkedList<PeerConnection.IceServer> iceServers;
    static Peer mPeer;
    private AudioManager mAudioManager;
    private VideoTrack remoteVideoTrack;
    BroadcastReceiver broadcastReceiver;
    Handler handler = new Handler();

    private class FriendListAdapter extends BaseAdapter {
        private ArrayList<String> mLeDevices;

        private LayoutInflater mInflator;

        public FriendListAdapter() {
            super();
            mLeDevices = new ArrayList<String>();
            mInflator = getLayoutInflater();
        }

        public void addDevice(String device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public String getDevice(int position) {
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
        public String getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = mInflator.inflate(R.layout.item_transfer_friend, null);
            CircleImageView circleImageView = view.findViewById(R.id.item_iv_avatar);
            TextView textView = view.findViewById(R.id.tv_name);
            String phone = "";
            try {
                JSONObject jsonObject = new JSONObject(mLeDevices.get(i));
                textView.setText(jsonObject.getString("name"));
                phone = jsonObject.getString("id");
                String url = jsonObject.getString("avatar");

                if (url.length() != 0) {
                    Glide.with(MainControlActivity.this).load(SocketService.filePath + url)
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .centerCrop().into(circleImageView);
                }


            } catch (JSONException e) {

            }


            Button button = view.findViewById(R.id.ga);
            String finalPhone = phone;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToastUtils.showShort("请输入账号   " + finalPhone);
                    sendBroadcast(new Intent(SocketService.closeCar));
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intentX = new Intent(SocketService.wantToTransfer);
                            intentX.putExtra("phone", finalPhone);
                            intentX.putExtra("carId", carId);
                            sendBroadcast(intentX);

                            MainControlActivity.this.finish();
                        }
                    }, 1000);
                    newFriendWindow.dismiss();
                }
            });


            return view;
        }


    }

    public View findViewById(int id) {
        if (id == R.id.setting_frame_layout && controlSetting != null) {
            return controlSetting.findViewById(id);
        }
        return super.findViewById(id);
    }

    private void bottomTabClickState(int index) {
        for (int a = 0; a < 5; a++) {
            images[a].setSelected(a == index);
        }
    }


    class OnTabClickListener implements View.OnClickListener {
        int index;

        public OnTabClickListener(int index) {

            this.index = index;
        }

        @Override
        public void onClick(View v) {
            initFragment(index);
            bottomTabClickState(index);
        }
    }

    private void hideFragment(FragmentTransaction transaction) {
        for (int k = 0; k < 5; k++) {
            transaction.hide(settingFragments[k]);
        }
    }

    private void hideFragment(int index) {

        for (int k = 0; k < 5; k++) {
            if (k != index) {
                transaction.hide(settingFragments[k]);
            }

        }

    }

    private void initFragment(int k) {
        transaction = getSupportFragmentManager().beginTransaction();
        hideFragment(transaction);
        transaction.show(settingFragments[k]);
        transaction.commit();
    }

    private static void displayGattServices(List<BluetoothGattService> gattServices) {

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

    private static BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                System.out.println("BroadcastReceiver :" + "device connected");

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
                    .equals(action)) {
                mConnected = false;
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
                System.out.println(da + "sdlkfjldskf   " + l);
                if (mPeer != null) {
                    if (mPeer.mDataChannel != null) {
                        System.out.println(da + "sdlkfjldskf  说服力的客服经理但是看见分厘卡电视机 ");
                        DataChannel.Buffer buffer = new DataChannel.Buffer(ByteBuffer.wrap(data), true);
                        mPeer.mDataChannel.send(buffer);
                    }
                }
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if(SPUtils.getInstance().getString(PersonalInfo.BLEAddress).length()==0){
            ToastUtils.showShort("未绑定遥控器");
            return;
        }
        if (mBluetoothLeService != null) {
            if (!das) {
                das = true;
                mBluetoothLeService.initialize();
                final boolean result = mBluetoothLeService.connect(SPUtils.getInstance().getString(PersonalInfo.BLEAddress));
            }

        }
    }

    void initView(){
        vx2=(TextView)findViewById(R.id.videoX2);
        vx1=(TextView)findViewById(R.id.videoX);
        mapView = (MapView) findViewById(R.id.mapX);
        mapButton = (TextView) findViewById(R.id.map);
        control_stick = (RelativeLayout) findViewById(R.id.control_stick);
        setting = (TextView) findViewById(R.id.setting_tv);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_control);
        initView();
        vx1.setOnClickListener(this);
        vx2.setOnClickListener(this);
        mapButton.setOnClickListener(this);



        setting.setOnClickListener(this);
        carId = getIntent().getStringExtra("carId");
        friendListAdapter = new FriendListAdapter();
        try {
            if (ContactsFragment.contactsJsonArray.length() > 0) {
                for (int k = 0; k < ContactsFragment.contactsJsonArray.length(); k++) {
                    friendListAdapter.addDevice(ContactsFragment.contactsJsonArray.getString(k));
                }
            }
        } catch (JSONException e) {

        }


        View contentview = LayoutInflater.from(this).inflate(R.layout.transfer_friend_list, null);
        rl = contentview.findViewById(R.id.rl);
        lv = contentview.findViewById(R.id.lv);
        lv.setAdapter(friendListAdapter);
        newFriendWindow = new PopupWindow(contentview, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        newFriendWindow.setOutsideTouchable(true);
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newFriendWindow.dismiss();
            }
        });


        controlSetting = LayoutInflater.from(this).inflate(R.layout.control_setting_list, null);
        control_setting0 = controlSetting.findViewById(R.id.control_setting0);
        control_setting1 = controlSetting.findViewById(R.id.control_setting1);
        control_setting2 = controlSetting.findViewById(R.id.control_setting2);
        control_setting3 = controlSetting.findViewById(R.id.control_setting3);
        control_setting4 = controlSetting.findViewById(R.id.control_setting4);
        settingBatteryFragment = new SettingBatteryFragment();
        settingMoreFragment = new SettingMoreFragment();
        settingCarFragment = new SettingCarFragment();
        settingVideoFragment = new SettingVideoFragment();
        settingCotrolerFragment = new SettingCotrolerFragment();
        settingFragments = new Fragment[]{settingCarFragment, settingCotrolerFragment, settingVideoFragment, settingBatteryFragment, settingMoreFragment};
        images = new ImageView[]{control_setting0, control_setting1, control_setting2, control_setting3, control_setting4};
        setting_return = controlSetting.findViewById(R.id.setting_return);
        setting_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingWindow.dismiss();
            }
        });
        for (int k = 0; k < 5; k++) {
            images[k].setOnClickListener(new OnTabClickListener(k));
        }
        transaction = getSupportFragmentManager().beginTransaction();
        for (int j = 0; j < 5; j++) {
            transaction.add(R.id.setting_frame_layout, settingFragments[j]);
        }
        hideFragment(0);
        bottomTabClickState(0);
        transaction.commit();


        settingWindow = new PopupWindow(controlSetting, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        settingWindow.setOutsideTouchable(true);


        initview();
        AskPermission();
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if(action.equals("photoOk")){
                        remoteVideoTrack.removeSink(phoneX);
                        phoneX=null;
                        addPhoto(new File(savePhoto));
                        ToastUtils.showShort("相片已保存");

                    }else
                    if (action.equals("stick")) {
                        System.out.println("打算离开房间了多少会计分录开始");
                        boolean d = intent.getBooleanExtra("ga", true);
                        if (d) {
                            control_stick.setVisibility(View.VISIBLE);
                        } else {
                            control_stick.setVisibility(View.INVISIBLE);
                        }

                    }
                    String phone = intent.getStringExtra("phone");

                    if (phone == null) return;

                    if (action.equals(SocketService.receiveSDP)) {

                        String msg = intent.getStringExtra(SocketService.receiveSDP);
                        if (mPeer == null) {
                            mPeer = new Peer(false, phone, mMediaStream, true, true);
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(msg);
                            SessionDescription description = new SessionDescription
                                    (SessionDescription.Type.fromCanonicalForm(jsonObject.getString("type")),
                                            jsonObject.getString("description"));
                            mPeer.peerConnection.setRemoteDescription(mPeer, description);

                            mPeer.peerConnection.createAnswer(mPeer, mPeer.sdpConstraints);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (action.equals(SocketService.receiveICE)) {
                        try {
                            String msg = intent.getStringExtra(SocketService.receiveICE);
                            JSONObject jsonObject = new JSONObject(msg);
                            IceCandidate candidate = null;
                            candidate = new IceCandidate(
                                    jsonObject.getString("id"),
                                    jsonObject.getInt("label"),
                                    jsonObject.getString("candidate")
                            );
                            mPeer.peerConnection.addIceCandidate(candidate);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("stick");
            intentFilter.addAction("photoOk");
            intentFilter.addAction(SocketService.wantToSeeRequest);
            intentFilter.addAction(SocketService.receiveICE);
            intentFilter.addAction(SocketService.receiveSDP);
            registerReceiver(broadcastReceiver, intentFilter);

        }
    }

    private void AskPermission() {
        List<PermissionItem> permissionItems = new ArrayList<PermissionItem>();

        permissionItems.add(new PermissionItem(Manifest.permission.CAMERA, "相机", R.drawable.permission_ic_camera));
        permissionItems.add(new PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, "存储卡", R.drawable.permission_ic_storage));
        permissionItems.add(new PermissionItem(Manifest.permission.RECORD_AUDIO, "录音", R.drawable.permission_ic_micro_phone));
        permissionItems.add(new PermissionItem(Manifest.permission.READ_PHONE_STATE, "手机", R.drawable.permission_ic_phone));

        HiPermission.create(this).permissions(permissionItems)
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {

                    }

                    @Override
                    public void onFinish() {
                        init();
                        Intent intent = new Intent(SocketService.wantToSee);
                        intent.putExtra("phone", carId);
                        sendBroadcast(intent);
                    }

                    @Override
                    public void onDeny(String permission, int position) {

                    }

                    @Override
                    public void onGuarantee(String permission, int position) {

                    }
                });
    }

    private void init() {
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(getApplicationContext())
                        .createInitializationOptions());

        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;

        encoderFactory = new DefaultVideoEncoderFactory(
                mEglBase.getEglBaseContext(),
                true,
                true);
        decoderFactory = new DefaultVideoDecoderFactory(mEglBase.getEglBaseContext());

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();

        mPeerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setAudioDeviceModule(JavaAudioDeviceModule.builder(getApplicationContext()).createAudioDeviceModule())
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();


        initConstraints();

        audioSource = mPeerConnectionFactory.createAudioSource(new MediaConstraints());
        mAudioTrack = mPeerConnectionFactory.createAudioTrack("audiotrack", audioSource);
        mMediaStream = mPeerConnectionFactory.createLocalMediaStream("localstream");
        mMediaStream.addTrack(mAudioTrack);

    }

    private void initConstraints() {
        iceServers = new LinkedList<>();
        iceServers.add(PeerConnection.IceServer.builder("stun:39.105.98.7").createIceServer());
        iceServers.add(PeerConnection.IceServer.builder("turn:39.105.98.7").setUsername("vaca").setPassword("123456").createIceServer());

        pcConstraints = new MediaConstraints();


    }


    private void initview() {

        switcCamera = (TextView) findViewById(R.id.switch_camera_tv);
        loundSperaker = (TextView) findViewById(R.id.loundspeaker_tv);

        switcCamera.setOnClickListener(this);
        loundSperaker.setOnClickListener(this);


        remoteView = (SurfaceViewRenderer) findViewById(R.id.remoteVideoView);

        //创建EglBase对象
        mEglBase = EglBase.create();

        //初始化localView

        //初始化remoteView
        remoteView.init(mEglBase.getEglBaseContext(), null);
        remoteView.setMirror(false);
        remoteView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        remoteView.setEnableHardwareScaler(true);


        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        assert mAudioManager != null;
        mAudioManager.setMode(AudioManager.MODE_IN_CALL);
        mAudioManager.setSpeakerphoneOn(true);
    }

    @Override
    protected void onDestroy() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver.clearAbortBroadcast();
            broadcastReceiver = null;
        }

        unregisterReceiver(mGattUpdateReceiver);
        mGattUpdateReceiver.clearAbortBroadcast();


        testHandler.removeCallbacksAndMessages(null);


        sendBroadcast(new Intent(SocketService.closeCar));


        if (mPeer != null) {
            if (isdada) {
                mPeer.mDataChannel.close();
                mPeer.peerConnection.close();
                mPeer.peerConnection.dispose();
                mPeer.mDataChannel.dispose();
            }
            mPeer = null;


        }


        remoteView.clearImage();
        remoteView.release();
        System.out.println("鲁大师会计分录跨境电商");
        super.onDestroy();
    }

    class Peer implements PeerConnection.Observer, SdpObserver, DataChannel.Observer {
        public boolean isOffer = false;
        public String phone;
        PeerConnection peerConnection;
        MediaConstraints sdpConstraints;

        DataChannel mDataChannel, mDataChannel2;

        Peer(boolean isOffer, String phone, MediaStream mMediaStream, boolean receiveAudio, boolean receiveVideo) {
            this.isOffer = isOffer;
            this.phone = phone;

            sdpConstraints = new MediaConstraints();
            sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", String.valueOf(receiveAudio)));
            sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", String.valueOf(receiveVideo)));
            peerConnection = mPeerConnectionFactory.createPeerConnection(iceServers, pcConstraints, this);
            peerConnection.addStream(mMediaStream);

            DataChannel.Init init = new DataChannel.Init();
            init.negotiated = false;
            init.ordered = false;

            DataChannel.Init init2 = new DataChannel.Init();
            init2.negotiated = false;
            init2.ordered = true;
            mDataChannel = peerConnection.createDataChannel("fuck", init);
            mDataChannel2 = peerConnection.createDataChannel("fuck2", init2);


           /* testHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handler.postDelayed(this,3000);

                    byte[]msg= new byte[]{0, 1, 56, (byte) 130, (byte) 250};

                    DataChannel.Buffer bufferx = new DataChannel.Buffer(ByteBuffer.wrap(msg), true);
                    try {
                        mDataChannel.send(bufferx);
                    }catch (Exception e){

                    }


                }
            },3000);*/


        }


        @Override
        public void onBufferedAmountChange(long var1) {

        }

        ;

        @Override
        public void onStateChange() {

        }

        ;

        public String bytes_String16(byte[] b) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < b.length; i++) {
                sb.append(String.format("%02x", b[i]));
            }
            return sb.toString();
        }

        @Override
        public void onMessage(DataChannel.Buffer var1) {
            ByteBuffer data = var1.data;
            final byte[] bytes = new byte[data.capacity()];
            data.get(bytes);
         /*   runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showShort("请输入账号"+new String(bytes));
                }
            });*/
        }

        // PeerConnection.Observer

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {

        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
                isdada = false;
                remoteVideoTrack=null;
                MainControlActivity.this.finish();
            } else if (iceConnectionState == PeerConnection.IceConnectionState.CONNECTED) {
                isdada = true;
            }
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("label", iceCandidate.sdpMLineIndex);
                jsonObject.put("id", iceCandidate.sdpMid);
                jsonObject.put("candidate", iceCandidate.sdp);

                Intent intent = new Intent(SocketService.sendICE);
                intent.putExtra(SocketService.sendICE, jsonObject.toString());
                intent.putExtra("phone", phone);
                sendBroadcast(intent);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            System.out.println("多斯拉克富家大室神鼎飞丹砂");
            remoteVideoTrack = mediaStream.videoTracks.get(0);
            remoteVideoTrack.addSink(remoteView);
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {

        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            dataChannel.registerObserver(this);
        }

        @Override
        public void onRenegotiationNeeded() {

        }

        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

        }

        //    SdpObserver

        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            peerConnection.setLocalDescription(this, sessionDescription);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("type", sessionDescription.type.canonicalForm());
                jsonObject.put("description", sessionDescription.description);
                Intent intent = new Intent(SocketService.sendSDP);
                intent.putExtra(SocketService.sendSDP, jsonObject.toString());
                intent.putExtra("phone", this.phone);
                sendBroadcast(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onSetSuccess() {

        }

        @Override
        public void onCreateFailure(String s) {

        }

        @Override
        public void onSetFailure(String s) {

        }
    }

    //监听音量键控制视频通话音量
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                chartTools.setVisibility(View.VISIBLE);
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                chartTools.setVisibility(View.VISIBLE);
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.videoX:
                if(remoteVideoTrack!=null){
                    Vibrator vibrator = (Vibrator)this.getSystemService(this.VIBRATOR_SERVICE);
                    vibrator.vibrate(200);
                    savePhoto=SocketService.filePath+"walkFuture"+String.valueOf(System.currentTimeMillis())+".jpg";
                    phoneX=new ImageEncoder( MainControlActivity.this,savePhoto);
                    remoteVideoTrack.addSink(phoneX);
                }
                break;
            case R.id.videoX2:

                if(!startRecord){
                    if(remoteVideoTrack==null){
                        ToastUtils.showShort("视频还未连通");
                        return;
                    }

                    try {
                        Vibrator vibrator = (Vibrator)this.getSystemService(this.VIBRATOR_SERVICE);
                        vibrator.vibrate(200);
                        saveAs= SocketService.filePath+"walkFuture"+String.valueOf(System.currentTimeMillis())+".mp4";
                        recording = new FileEncoder( saveAs,mEglBase.getEglBaseContext());
                        remoteVideoTrack.addSink(recording);
                        startRecord=!startRecord;
                        vx2.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.drawable.ic_baseline_videocam_off_24_y),null,null);
                        vx2.setText("结束录像");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }



                }else{
                    vx2.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.drawable.ic_baseline_video_call_24_x),null,null);
                    vx2.setText("录像");
                    remoteVideoTrack.removeSink(recording);
                    recording.release();
                    startRecord=!startRecord;

                    if(saveAs.length()!=0){
                        fileChange.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                               addVideo(new File(saveAs));
                                ToastUtils.showShort("录像已保存");
                            }
                        },1000);

                    }

                }

                 break;
            case R.id.map:
                mapSta = !mapSta;
                if (mapSta) {
                    mapView.setVisibility(View.VISIBLE);
                } else {
                    mapView.setVisibility(View.GONE);
                }

                break;
            case R.id.setting_tv:
                settingWindow.setFocusable(true);
                settingWindow.setOutsideTouchable(true);
                settingWindow.showAsDropDown(switcCamera);
                settingWindow.update();

                break;
            case R.id.switch_camera_tv:

                MainControlActivity.this.finish();
                break;

            case R.id.loundspeaker_tv:
                newFriendWindow.setFocusable(true);
                newFriendWindow.setOutsideTouchable(true);
                newFriendWindow.showAsDropDown(switcCamera);
                newFriendWindow.update();


                break;
        }

    }

    public Uri addVideo(File videoFile) {
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.Video.Media.TITLE, "walkFuture video");
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATA, videoFile.getAbsolutePath());
        return getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }

    public Uri addPhoto(File videoFile) {
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.Images.Media.TITLE, "walkFuture photo");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        values.put(MediaStore.Images.Media.DATA, videoFile.getAbsolutePath());
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public void onPause() {
        sendBroadcast(new Intent(SocketService.closeCar));
        MainControlActivity.this.finish();
        super.onPause();
    }


}

package com.walkfure.chat.activity;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.walkfure.chat.R;
import com.walkfure.chat.data.PersonalInfo;
import com.walkfure.chat.service.SocketService;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
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
import org.webrtc.VideoTrack;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

public class ChatTransferControlActivity extends AppCompatActivity implements View.OnClickListener {

    String CarId;
    String MasterId;


    private LinearLayout chartTools;
    private TextView switcCamera;
    private TextView loundSperaker;

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
    Peer mPeer;
    private AudioManager mAudioManager;
    private VideoTrack remoteVideoTrack;
    BroadcastReceiver broadcastReceiver;
    Handler handler = new Handler();
    String phoneX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_chat_transfer_control);
        phoneX = getIntent().getStringExtra("phoneX");

        initview();
        AskPermission();
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    String phone = intent.getStringExtra("phone");
                    if (phone == null) return;
                    if (action.equals(SocketService.receiveSDP)) {
                        String msg = intent.getStringExtra(SocketService.receiveSDP);
                        try {
                            JSONObject jsonObject = new JSONObject(msg);
                            SessionDescription description = new SessionDescription
                                    (SessionDescription.Type.fromCanonicalForm(jsonObject.getString("type")),
                                            jsonObject.getString("description"));
                            mPeer.peerConnection.setRemoteDescription(mPeer, description);
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

                        mPeer = new Peer(true, phoneX, mMediaStream, true, true);

                        mPeer.peerConnection.createOffer(mPeer, mPeer.sdpConstraints);
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
        //初始化PeerConnectionFactory
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

        AudioSource audioSource = mPeerConnectionFactory.createAudioSource(new MediaConstraints());
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

        chartTools = findViewById(R.id.charttools_layout);
        switcCamera = findViewById(R.id.switch_camera_tv);
        loundSperaker = findViewById(R.id.loundspeaker_tv);

        switcCamera.setOnClickListener(this);
        loundSperaker.setOnClickListener(this);


        remoteView = findViewById(R.id.remoteVideoView);

        //创建EglBase对象
        mEglBase = EglBase.create();

        //初始化localView

        //初始化remoteView
        remoteView.init(mEglBase.getEglBaseContext(), null);
        remoteView.setMirror(false);
        remoteView.setZOrderMediaOverlay(true);
        remoteView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        remoteView.setEnableHardwareScaler(false);


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

        if (mVideoCapturer != null) {
            try {
                mVideoCapturer.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mPeer.peerConnection.close();
        mPeer.peerConnection = null;
        mPeer = null;
        if (mVideoTrack != null) {
            mVideoTrack.dispose();
        }
        if (mAudioTrack != null) {
            mAudioTrack.dispose();
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showShort("请输入账号" + new String(bytes));
                }
            });
        }

        // PeerConnection.Observer

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {

        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
                isOffer = false;
                peerConnection.close();
                remoteView.clearImage();
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
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                toggleChartTools();
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_camera_tv:
                sendBroadcast(new Intent(SocketService.closeCar));
                ChatTransferControlActivity.this.finish();
                break;

            case R.id.loundspeaker_tv:
                sendBroadcast(new Intent(SocketService.closeCar));
                ChatTransferControlActivity.this.finish();


                break;
        }

    }

    public void onPause() {
        sendBroadcast(new Intent(SocketService.closeCar));
        ChatTransferControlActivity.this.finish();
        super.onPause();
    }

    private void toggleChartTools() {
        if (chartTools.isShown()) {
            chartTools.setVisibility(View.INVISIBLE);
        } else {
            chartTools.setVisibility(View.VISIBLE);
        }
    }
}

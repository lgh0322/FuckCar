package com.walkfure.chat.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.walkfure.chat.activity.ChatTransferControlActivity;
import com.walkfure.chat.activity.LoginActivity;
import com.walkfure.chat.activity.TransferRequestActivity;
import com.walkfure.chat.bean.ChatBean;
import com.walkfure.chat.bean.ContactsBean;
import com.walkfure.chat.data.PersonalInfo;
import com.walkfure.chat.utils.FileUtils;
import com.walkfure.chat.utils.GetAndroidUniqueMark;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class SocketService extends Service {

    //--------------------------------------------------------update UI
    public static Object mLockLoginMsg = new Object();
    public static boolean Created = false;
    public static int updatePersonalInfoInt = 1;
    public static int friendInfoInt = 2;

    class BroadCastHandle extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == updatePersonalInfoInt) {

            } else if (msg.what == friendInfoInt) {

            }
        }
    }

    BroadCastHandle broadCastHandle = new BroadCastHandle();


    Handler friendInfo = new Handler();


//----------------------------------------------------------------------------------------------aliYun oss

    public interface UploadCallback {
        abstract void uCallback(String key);
    }

    public interface DownloadCallback {
        abstract void dCallback(String path);
    }


    public static String getFileMD5(String path) {
        File file = new File(path);
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }


    FileUtils fileUtils;
    public static String filePath;
    private OSS oss;
    private static final String accessKeyId = "LTAI4Fua6yDc2UAE6QtTaiM8";
    private static final String accessKeySecret = "6djk9uYpIDlhLx4kv25AF8v1Pm6aID";
    private static final String endpoint = "oss-accelerate.aliyuncs.com";
    private static final String OSS_BUCKET = "vaca";
    public static final String ImgUrl = "http://vaca.oss-accelerate.aliyuncs.com/";
    private ExecutorService executor;

    private void initOSS() {
        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(accessKeyId, accessKeySecret);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        OSSLog.enableLog();
        oss = new OSSClient(getApplicationContext(), endpoint, credentialProvider, conf);
    }

    String getPathX() {
        File[] fs = getExternalFilesDirs(null);
        String extPath = "";
        if (fs != null && fs.length >= 1) {
            extPath = fs[0].getAbsolutePath() + "/";
        }
        return extPath;
    }



    //------------------------------------------------------------------------------Download File

    class DownloadNFile {
        private final Object lock = new Object();
        private Integer n = 0;
        private Integer s = 0;
        private String info;

        void setS(Integer s) {
            synchronized (lock) {
                this.s = s;
            }

        }

        void setInfo(String st) {
            synchronized (lock) {
                this.info = st;
            }

        }

        void over() {
            synchronized (lock) {
                if (n < s) {
                    try {
                        lock.wait();
                    } catch (Exception ee) {
                        ee.printStackTrace();

                    }
                }
                final Intent intent = new Intent(UpdateContactsData);
                intent.putExtra(UpdateContactsData, info);
                sendBroadcast(intent);


            }
        }

        void add() {
            synchronized (lock) {
                n = n + 1;
                if (n >= s) {
                    lock.notify();
                }
            }
        }
    }

    DownloadNFile downloadNFile = new DownloadNFile();


    private void ossDownload(String key, DownloadCallback downloadCallback) {
        GetObjectRequest get = new GetObjectRequest(OSS_BUCKET, key);

        OSSAsyncTask task = oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
            @Override
            public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                long length = result.getContentLength();
                byte[] buffer = new byte[(int) length];
                int readCount = 0;
                while (readCount < length) {
                    try {
                        readCount += result.getObjectContent().read(buffer, readCount, (int) length - readCount);
                    } catch (Exception e) {
                        OSSLog.logInfo(e.toString());
                    }
                }

                try {
                    FileOutputStream fout = new FileOutputStream(filePath + "/" + key);
                    fout.write(buffer);
                    fout.close();
                    downloadCallback.dCallback(filePath + "/" + key);
                } catch (Exception e) {
                    OSSLog.logInfo(e.toString());
                }

            }

            @Override
            public void onFailure(GetObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                }
            }


        });
    }

    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
//-------------------------------------------------------------------------------------------Upload File;

    private void ossUpload(String outfile, UploadCallback uploadCallback) {
        if (outfile == null) {
            return;
        }
        int lastDot = outfile.lastIndexOf(".");
        String lastDotString = outfile.substring(lastDot);
        PutObjectRequest put = new PutObjectRequest(OSS_BUCKET, getRandomString(32) + lastDotString, outfile);
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
            }
        });

        oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(final PutObjectRequest request, PutObjectResult result) {
                Log.d("PutObject", "UploadSuccess");
                Log.d("ETag", result.getETag());
                Log.d("RequestId", result.getRequestId());
                Log.d("getObjectKey", request.getObjectKey());
                uploadCallback.uCallback(request.getObjectKey());

            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                }
            }
        });

    }


    public static Socket mSocket;

    //-----------------------------------------------------------------------------broadCast  IntentFiletyer

    public static final String UpdateContactsData = "com.walkfure.chat.service.SocketService.updateContactsData";
    public static final String ChangeName = "com.walkfure.chat.service.SocketService.ChangeName";
    public static final String uploadAvatar = "com.walkfure.chat.service.SocketService.uploadAvatar";
    public static final String updatePersonalAvatar = "com.walkfure.chat.service.SocketService.updatePersonalAvatar";
    public static final String uploadCarAvatar = "com.walkfure.chat.service.SocketService.uploadCarAvatar";
    public static final String updateCarAvatar = "com.walkfure.chat.service.SocketService.updateCarAvatar";
    public static final String updatePersonalName = "com.walkfure.chat.service.SocketService.updatePersonalName";
    public static final String updatePersonalInfo = "com.walkfure.chat.service.SocketService.updatePersonalInfo";
    public static final String checkObjectKey = "com.walkfure.chat.service.SocketService.checkObjectKey";
    public static final String checkContactsObjectKey = "com.walkfure.chat.service.SocketService.checkContactsObjectKey";
    public static final String downloadPersonalAvatarSucceed = "com.walkfure.chat.service.SocketService.downloadPersonalAvatarSucceed";
    public static final String deleteAFriend = "com.walkfure.chat.service.SocketService.deleteAFriend";
    public static final String searchAFriend = "com.walkfure.chat.service.SocketService.searchAFriend";
    public static final String searchAFriendResult = "com.walkfure.chat.service.SocketService.searchAFriendResult";
    public static final String addFriendEnter = "com.walkfure.chat.service.SocketService.addFriendEnter";
    public static final String addFriendRequest = "com.walkfure.chat.service.SocketService.addFriendRequest";
    public static final String addFriendPermit = "com.walkfure.chat.service.SocketService.addFriendPermit";
    public static final String ChatSend = "com.walkfure.chat.service.SocketService.ChatSend";
    public static final String ChatReceive = "com.walkfure.chat.service.SocketService.ChatReceive";
    public static final String sendSDP = "com.walkfure.chat.service.SocketService.sendSDP";
    public static final String sendICE = "com.walkfure.chat.service.SocketService.sendICE";
    public static final String receiveSDP = "com.walkfure.chat.service.SocketService.receiveSDP";
    public static final String receiveICE = "com.walkfure.chat.service.SocketService.receiveICE";
    public static final String wantToSee = "com.walkfure.chat.service.SocketService.wantToSee";
    public static final String wantToSeeShare = "com.walkfure.chat.service.SocketService.wantToSeeShare";
    public static final String wantToSeeRequest = "com.walkfure.chat.service.SocketService.wantToSeeRequest";
    public static final String wantToSeeRequestShare = "com.walkfure.chat.service.SocketService.wantToSeeRequestShare";
    public static final String updateMainActivityUnread = "com.walkfure.chat.service.SocketService.updateMainActivityUnread";
    public static final String setRedTextZero = "com.walkfure.chat.service.SocketService.setRedTextZero";
    public static final String MsgMeSend = "com.walkfure.chat.service.SocketService.MsgMeSend";
    public static final String bindCarSuccess = "com.walkfure.chat.service.SocketService.bindCarSuccess";
    public static final String closeCar = "com.walkfure.chat.service.SocketService.CloseCar";
    public static final String wantToTransfer = "com.walkfure.chat.service.SocketService.wantToTransfer";
    public static final String receiveTransferRequest = "com.walkfure.chat.service.SocketService.receiveTransferRequest";
    public static final String returnCarList = "com.walkfure.chat.service.SocketService.returnCarList";
    public static final String submitPhone = "com.walkfure.chat.service.SocketService.submitPhone";
    public static final String submitSMS = "com.walkfure.chat.service.SocketService.submitSMS";
    public static final String submitCode = "com.walkfure.chat.service.SocketService.submitCode";
    public static final String finishApp = "com.walkfure.chat.service.SocketService.finishApp";
    public static final String sendPhoneSmsResult = "com.walkfure.chat.service.SocketService.sendPhoneSmsResult";
    public static final String LoginSuccess = "com.walkfure.chat.service.SocketService.LoginSuccess";
    public static final String LoginSuccessUploadPhone = "com.walkfure.chat.service.SocketService.LoginSuccessUploadPhone";
    public static final String LoginSuccessUpdate = "com.walkfure.chat.service.SocketService.LoginSuccessUpdate";
    public static ConcurrentHashMap<String, ContactsBean> contactsInfo = new ConcurrentHashMap<>();
    public static String CarId = "";
    ServiceReceiver serviceReceiver;

    private class ServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;
            if (action.equals(LoginSuccessUploadPhone)) {
                String phone = SPUtils.getInstance().getString(PersonalInfo.A1);
                if (phone != null) {
                    if (phone.length() != 0) {
                        mSocket.emit("phoneNumber", phone);
                    }
                }
            }
            if (action.equals(submitCode)) {
                String msg = intent.getStringExtra("msg");
                mSocket.emit("submitCode", msg);
            }
            if (action.equals(submitPhone)) {
                String msg = intent.getStringExtra("msg");
                mSocket.emit("requestSMS", msg, GetAndroidUniqueMark.getUniqueId(SocketService.this));
            } else if (action.equals(submitSMS)) {
                String msg = intent.getStringExtra("msg");
                String msg2 = intent.getStringExtra("msg2");
                mSocket.emit("returnSMSResult", msg, msg2);
            } else if (action.equals(wantToTransfer)) {
                String phone = intent.getStringExtra("phone");
                String carId = intent.getStringExtra("carId");
                mSocket.emit("wantToTransfer", phone, carId);

            }
            if (action.equals(closeCar)) {
                mSocket.emit("closeCar", SPUtils.getInstance().getString(PersonalInfo.CarId));
            } else if (action.equals(sendSDP)) {
                String msg = intent.getStringExtra(sendSDP);
                String phoneX = intent.getStringExtra("phone");
                String from = SPUtils.getInstance().getString(PersonalInfo.A1);
                mSocket.emit("sendSDP", phoneX, msg, from);
            } else if (action.equals(sendICE)) {
                String msg = intent.getStringExtra(sendICE);
                String phoneX = intent.getStringExtra("phone");
                String from = SPUtils.getInstance().getString(PersonalInfo.A1);
                mSocket.emit("sendICE", phoneX, msg, from);
            } else if (action.equals(wantToSee)) {
                String phoneX = intent.getStringExtra("phone");
                String from = SPUtils.getInstance().getString(PersonalInfo.A1);
                mSocket.emit("wantToSee", phoneX, from);
            } else if (action.equals(wantToSeeShare)) {
                String phoneX = intent.getStringExtra("phone");
                String from = SPUtils.getInstance().getString(PersonalInfo.A1);
                mSocket.emit("wantToSeeShare", phoneX, from);
            } else if (action.equals(ChatSend)) {
                String msg = intent.getStringExtra("msg");
                mSocket.emit("chatSend", msg);
            } else if (action.equals(addFriendPermit)) {
                String msg = intent.getStringExtra("msg");
                mSocket.emit("addFriendPermit", msg);
            } else if (action.equals(addFriendEnter)) {
                String msg = intent.getStringExtra("msg");
                mSocket.emit("addFriendEnter", msg);
            } else if (action.equals(searchAFriend)) {
                String msg = intent.getStringExtra("msg");
                mSocket.emit("addFriendStart", msg);
            } else if (action.equals(deleteAFriend)) {
                String msg = intent.getStringExtra("msg");
                mSocket.emit("deleteAFriend", msg);
            } else if (ChangeName.equals(action)) {
                String name = intent.getStringExtra("name");
                mSocket.emit("changeName", name);
            } else if (uploadAvatar.equals(action)) {
                String path = intent.getStringExtra("path");
//                ossUpload(path);
                String md5 = getFileMD5(path);
                String intentX = uploadAvatar;
                mSocket.emit("requestFileKey", md5, path, intentX);
            } else if (checkObjectKey.equals(action)) {
                String objectKey = intent.getStringExtra(checkObjectKey);
                if (objectKey.length() == 0) return;
                String currentObject = SPUtils.getInstance().getString(PersonalInfo.Avatar);
                boolean update = false;
                if (currentObject == null) {
                    update = true;
                } else {
                    if (currentObject.length() == 0) {
                        update = true;
                    } else {
                        if (!currentObject.equals(objectKey)) {
                            update = true;
                        }
                    }
                }

                if (update) {
                    SPUtils.getInstance().put(PersonalInfo.Avatar, objectKey);
                    ossDownload(objectKey, path -> {
                        SPUtils.getInstance().put(PersonalInfo.AvatarPath, path);


                        sendBroadcast(new Intent(downloadPersonalAvatarSucceed));
                    });
                } else {
                    sendBroadcast(new Intent(updatePersonalAvatar));
                }
            } else if (action.equals(checkContactsObjectKey)) {
                String xx = intent.getStringExtra(SocketService.checkContactsObjectKey);
                try {
                    JSONArray a = new JSONArray(xx);
                    if (a.length() > 0) {
                        int s = 0;
                        for (int k = 0; k < a.length(); k++) {
                            JSONObject b = new JSONObject(a.get(k).toString());
                            String d = b.getString("avatar");
                            if (d.length() > 0) {
                                File file = new File(filePath + "/" + d);
                                if (!file.exists()) {
                                    s++;
                                }
                            }
                        }
                        downloadNFile.setS(s);
                        downloadNFile.setInfo(xx);
                        if (s > 0) {
                            for (int k = 0; k < a.length(); k++) {
                                JSONObject b = new JSONObject(a.get(k).toString());
                                String d = b.getString("avatar");
                                if (d.length() > 0) {
                                    File file = new File(filePath + "/" + d);
                                    if (!file.exists()) {
                                        ossDownload(b.getString("avatar"), new DownloadCallback() {
                                            @Override
                                            public void dCallback(String path) {
                                                downloadNFile.add();
                                            }
                                        });
                                    }

                                }
                            }
                        }
                        downloadNFile.over();


                    } else {
                        final Intent intent2 = new Intent(UpdateContactsData);
                        intent2.putExtra(UpdateContactsData, "[]");
                        sendBroadcast(intent2);
                    }
                } catch (JSONException ee) {

                }
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //------------------------------------------------------broadCastRecibre;
        serviceReceiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(wantToTransfer);
        intentFilter.addAction(ChangeName);
        intentFilter.addAction(uploadAvatar);
        intentFilter.addAction(checkObjectKey);
        intentFilter.addAction(checkContactsObjectKey);
        intentFilter.addAction(deleteAFriend);
        intentFilter.addAction(searchAFriend);
        intentFilter.addAction(addFriendEnter);
        intentFilter.addAction(addFriendPermit);
        intentFilter.addAction(ChatSend);
        intentFilter.addAction(sendSDP);
        intentFilter.addAction(sendICE);
        intentFilter.addAction(wantToSee);
        intentFilter.addAction(wantToSeeShare);
        intentFilter.addAction(closeCar);
        intentFilter.addAction(submitPhone);
        intentFilter.addAction(submitSMS);
        intentFilter.addAction(submitCode);
        intentFilter.addAction(LoginSuccessUploadPhone);
        registerReceiver(serviceReceiver, intentFilter);


        //----------------------------------------------------------aliYun  oss
        fileUtils = new FileUtils(this);
        filePath = getPathX();
        executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            initOSS();
        });


        //------------------------------------------------------------------------------------------socketSetting
        try {
            mSocket = IO.socket("http://192.168.6.103:3000/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        //----------------------------------------------------------------------------------------receive sdp
        mSocket.on("smsResult", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String a = args[0].toString();
                if (a.equals("b")) {
                    ToastUtils.showShort("请输入正确的验证码");
                } else {
                    sendBroadcast(new Intent(LoginSuccess));
                }
            }
        });

        //----------------------------------------------------------------------------------------receive sdp
        mSocket.on("smsRequestResult", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String a = args[0].toString();
                if (a.equals("b")) {
                    ToastUtils.showShort("今日超出登录次数");
                    LoginActivity.loginActivity.finish();
                } else {
                    sendBroadcast(new Intent(sendPhoneSmsResult));
                }
            }
        });
        //----------------------------------------------------------------------------------------receive sdp
        mSocket.on("receiveSDP", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intent = new Intent(receiveSDP);
                intent.putExtra(receiveSDP, args[0].toString());
                intent.putExtra("phone", args[1].toString());
                sendBroadcast(intent);
            }
        });

        //----------------------------------------------------------------------------------------receive ice
        mSocket.on("receiveICE", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intent = new Intent(receiveICE);
                intent.putExtra(receiveICE, args[0].toString());
                intent.putExtra("phone", args[1].toString());
                sendBroadcast(intent);
            }
        });


        //----------------------------------------------------------------------------------------receive ice
        mSocket.on("receiveTransferRequest", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intentx = new Intent(getBaseContext(), TransferRequestActivity.class);
                intentx.putExtra("phone", args[1].toString());
                intentx.putExtra("carId", args[0].toString());
                intentx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplication().startActivity(intentx);
            }
        });

//--------------------------------------------------------------------------------------------receive want to see request
        mSocket.on("wantToSeeRequest", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intent = new Intent(wantToSeeRequest);
                intent.putExtra("phone", args[0].toString());
                sendBroadcast(intent);
            }
        });

        //--------------------------------------------------------------------------------------------receive want to see request
        mSocket.on("wantToSeeRequestShare", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intentx = new Intent(getBaseContext(), ChatTransferControlActivity.class);
                intentx.putExtra("phoneX", args[0].toString());
                intentx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplication().startActivity(intentx);
            }
        });


        //----------------------------------------------------------------------------------------socket return login success , then upload my phone number;
        mSocket.on("loginSuccess", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                sendBroadcast(new Intent(LoginSuccessUpdate));
            }
        });

        //--------------------------------------------------------------------------------------IM chat channel
        mSocket.on("chatReceive", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject jsonObject = new JSONObject(args[0].toString());
                    int type = jsonObject.getInt("type");
                    String from = jsonObject.getString("from");
                    if (type == 0) {
                        if (contactsInfo.get(from) != null) {
                            ChatBean chatBean = new ChatBean();
                            chatBean.setImg(contactsInfo.get(from).getAvatar());
                            chatBean.setChatType(1);
                            chatBean.setId(from);
                            chatBean.setChatMessage(jsonObject.getString("content"));
                            contactsInfo.get(from).addMsg(chatBean);
                            Intent intent = new Intent(ChatReceive);
                            intent.putExtra("from", from);
                            intent.putExtra("content", jsonObject.getString("content"));
                            sendBroadcast(intent);
                        }
                    }
                } catch (JSONException dfr) {

                }
            }
        });


        //--------------------------------------------------------------------------------------return search friend result, intent to add a friend;
        mSocket.on("returnAddFriendStart", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intent = new Intent(searchAFriendResult);
                intent.putExtra(searchAFriendResult, args[0].toString());
                sendBroadcast(intent);
            }
        });


        //--------------------------------------------------------------------------------------return search friend result, intent to add a friend;
        mSocket.on("bindCarSuccess", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("多斯拉克房间数量的会计法");
                CarId = args[0].toString();
                SPUtils.getInstance().put(PersonalInfo.CarId, CarId);
                Intent intent = new Intent(bindCarSuccess);
                intent.putExtra("carId", CarId);
                sendBroadcast(intent);
            }
        });

        //--------------------------------------------------------------------------------------add Friend Request.  others want to add you;
        mSocket.on("addFriendRequest", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intent = new Intent(addFriendRequest);
                intent.putExtra(addFriendRequest, args[0].toString());
                sendBroadcast(intent);
            }
        });

        //-----------------------------------------------------------------------------------login get the friends info;
        mSocket.on("friendInfo", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                synchronized (mLockLoginMsg) {
                    if (!Created) {
                        try {
                            mLockLoginMsg.wait();
                        } catch (InterruptedException ee) {

                        }

                    }

                    //------------------------------------------------------------------will update friend avatar and name, first check if is exist;
                    final Intent intent = new Intent(checkContactsObjectKey);
                    intent.putExtra(checkContactsObjectKey, args[0].toString());
                    sendBroadcast(intent);

                    //-----------------------------------------------------------------update my avatar and name;
                    final Intent intentX = new Intent(updatePersonalInfo);
                    intentX.putExtra(updatePersonalInfo, args[1].toString());
                    sendBroadcast(intentX);
                }
            }
        });

        //-----------------------------------------------------------------------------------login get the friends info;
        mSocket.on("carList", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intent = new Intent(returnCarList);
//                ToastUtils.showShort("请输入正确的手机号" + args[0].toString());
                intent.putExtra(returnCarList, args[0].toString());
                sendBroadcast(intent);

            }
        });


//------------------------------------------------------------------file Key result
        mSocket.on("fileKeyResult", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String key = args[0].toString();
                String intent = args[3].toString();
                if (args[0].toString().equals("null")) {
                    ossUpload(args[1].toString(), (UploadCallback) (k) -> {
                        mSocket.emit("saveFileKey", args[2].toString(), k);
                        if (intent.equals(uploadAvatar)) {
                            mSocket.emit("changeAvatar", k);
                            SPUtils.getInstance().put(PersonalInfo.Avatar, k);
                        }


                    });
                } else {
                    if (intent.equals(uploadAvatar)) {
                        mSocket.emit("changeAvatar", args[0].toString());
                        SPUtils.getInstance().put(PersonalInfo.Avatar, args[0].toString());
                    }

                }
            }
        });
        mSocket.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("HUANG", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("HUANG", "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("HUANG", "onBind");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("HUANG", "onUnbind");
        return super.onUnbind(intent);
    }

}
